package io.atleon.imi.stream;

import io.atleon.imi.api.json.JobPostingView;
import io.atleon.json.jackson.TypedJsonKafkaDeserializer;
import io.atleon.kafka.AloKafkaReceiver;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.ApplicationContext;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;

public abstract class IndexingStream extends ImiStream {

    public IndexingStream(ApplicationContext context) {
        super(context);
    }

    @Override
    protected Disposable startDisposable() {
        Scheduler scheduler = newBoundedElasticScheduler(concurrency());

        return buildReceiver()
            .receiveAloRecords(materializationTopic())
            .groupByStringHash(ConsumerRecord::key, concurrency(), ConsumerRecord::value)
            .innerPublishOn(scheduler)
            .innerConsume(this::index)
            .flatMapAlo()
            .resubscribeOnError(name())
            .doFinally(__ -> scheduler.dispose())
            .subscribe();
    }

    protected abstract void index(JobPostingView view);

    private AloKafkaReceiver<String, JobPostingView> buildReceiver() {
        return kafkaConfigSource
            .withClientId(name())
            .withConsumerGroupId(name())
            .withKeyDeserializer(StringDeserializer.class)
            .withValueDeserializer(TypedJsonKafkaDeserializer.class)
            .with(TypedJsonKafkaDeserializer.VALUE_TYPE_CONFIG, JobPostingView.class)
            .as(AloKafkaReceiver::create);
    }
}
