package io.atleon.imi.stream;

import io.atleon.core.AloFlux;
import io.atleon.core.DefaultAloSenderResultSubscriber;
import io.atleon.imi.api.json.MaterializeCommand;
import io.atleon.json.jackson.JsonKafkaSerializer;
import io.atleon.kafka.AloKafkaSender;
import io.atleon.kafka.KafkaConfigSource;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.ApplicationContext;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;

import java.util.Collection;

public abstract class IngestionStream<T> extends ImiStream {

    protected IngestionStream(ApplicationContext context) {
        super(context);
    }

    @Override
    protected Disposable startDisposable() {
        AloKafkaSender<String, MaterializeCommand> sender = senderConfigSource().as(AloKafkaSender::create);
        Scheduler scheduler = newBoundedElasticScheduler(concurrency());

        return receive()
            .groupByStringHash(this::extractEventKey, concurrency())
            .innerPublishOn(scheduler) // Best practice to support subsequent blocking operations
            .innerFlatMapIterable(this::ingest)
            .flatMapAlo(sender.sendAloValues(commandTopic(), MaterializeCommand::getHiringIntentId))
            .resubscribeOnError(name())
            .doAllFinally(__ -> scheduler.dispose(), sender::close)
            .subscribeWith(new DefaultAloSenderResultSubscriber<>());
    }

    protected KafkaConfigSource senderConfigSource() {
        return kafkaConfigSource
            .withClientId(name())
            .withKeySerializer(StringSerializer.class)
            .withValueSerializer(JsonKafkaSerializer.class);
    }

    protected abstract AloFlux<T> receive();

    protected abstract String extractEventKey(T event);

    protected abstract Collection<MaterializeCommand> ingest(T event);
}
