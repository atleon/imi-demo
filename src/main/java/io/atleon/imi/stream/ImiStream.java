package io.atleon.imi.stream;

import io.atleon.kafka.KafkaConfigSource;
import io.atleon.spring.SpringAloStream;
import org.springframework.context.ApplicationContext;

import java.time.Duration;

public abstract class ImiStream extends SpringAloStream {

    protected final KafkaConfigSource kafkaConfigSource;

    protected ImiStream(ApplicationContext context) {
        super(context);
        this.kafkaConfigSource = context.getBean("kafkaConfigSource", KafkaConfigSource.class);
    }

    protected final int concurrency() {
        return getStreamProperty("concurrency", Integer.class).orElse(1);
    }

    protected final int batchSize() {
        return getStreamProperty("batch.size", Integer.class).orElse(1);
    }

    protected final Duration batchDuration() {
        return getStreamProperty("batch.duration", Duration.class).orElse(Duration.ofSeconds(1));
    }

    protected String commandTopic() {
        return getRequiredProperty("stream.imi.command.topic");
    }

    protected String materializationTopic() {
        return getRequiredProperty("stream.imi.materialization.topic");
    }
}
