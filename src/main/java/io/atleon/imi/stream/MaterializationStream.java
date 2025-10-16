package io.atleon.imi.stream;

import io.atleon.core.AcknowledgementQueueMode;
import io.atleon.core.DefaultAloSenderResultSubscriber;
import io.atleon.imi.api.json.MaterializeCommand;
import io.atleon.imi.service.JobPostingViewMaterializer;
import io.atleon.json.jackson.JsonKafkaSerializer;
import io.atleon.json.jackson.TypedJsonKafkaDeserializer;
import io.atleon.kafka.AloKafkaReceiver;
import io.atleon.kafka.AloKafkaSender;
import io.atleon.spring.AutoConfigureStream;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.ApplicationContext;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;

@AutoConfigureStream
public class MaterializationStream extends ImiStream {

    private final JobPostingViewMaterializer materializer;

    public MaterializationStream(ApplicationContext context) {
        super(context);
        this.materializer = context.getBean(JobPostingViewMaterializer.class);
    }

    @Override
    protected Disposable startDisposable() {
        AloKafkaSender<String, Object> sender = buildPojoSender();
        Scheduler scheduler = newBoundedElasticScheduler(concurrency());
        String jobPostingViewTopic = materializationTopic();

        return buildCommandReceiver()
            .receiveAloRecords(commandTopic())
            .groupByStringHash(ConsumerRecord::key, concurrency(), ConsumerRecord::value)
            .innerBufferTimeout(batchSize(), batchDuration(), true)
            .innerPublishOn(scheduler) // Best practice to support subsequent blocking operations
            .innerFlatMapIterable(materializer::materialize)
            .innerMap(it -> new ProducerRecord<String, Object>(jobPostingViewTopic, it.getHiringIntentId(), it))
            .flatMapAlo(sender::sendAloRecords)
            .resubscribeOnError(name())
            .doAllFinally(__ -> scheduler.dispose(), sender::close)
            .subscribeWith(new DefaultAloSenderResultSubscriber<>());
    }

    private AloKafkaSender<String, Object> buildPojoSender() {
        return kafkaConfigSource
            .withClientId(name())
            .withKeySerializer(StringSerializer.class)
            .withValueSerializer(JsonKafkaSerializer.class)
            .as(AloKafkaSender::create);
    }

    private AloKafkaReceiver<String, MaterializeCommand> buildCommandReceiver() {
        return kafkaConfigSource
            .withClientId(name())
            .withConsumerGroupId(name())
            .withKeyDeserializer(StringDeserializer.class)
            .withValueDeserializer(TypedJsonKafkaDeserializer.class)
            .with(TypedJsonKafkaDeserializer.VALUE_TYPE_CONFIG, MaterializeCommand.class)
            .with(AloKafkaReceiver.ACKNOWLEDGEMENT_QUEUE_MODE_CONFIG, AcknowledgementQueueMode.COMPACT) // Optimization
            .as(AloKafkaReceiver::create);
    }
}
