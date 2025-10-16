package io.atleon.imi.stream;

import io.atleon.core.AloFlux;
import io.atleon.imi.api.json.HiringIntentCreatedEvent;
import io.atleon.imi.api.json.MaterializeCommand;
import io.atleon.imi.service.HiringIntentIngestor;
import io.atleon.json.jackson.TypedJsonKafkaDeserializer;
import io.atleon.kafka.AloKafkaReceiver;
import io.atleon.spring.AutoConfigureStream;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Collections;

@AutoConfigureStream
public class HiringIntentIngestionStream extends IngestionStream<HiringIntentCreatedEvent> {

    private final HiringIntentIngestor ingestor;

    public HiringIntentIngestionStream(ApplicationContext context) {
        super(context);
        this.ingestor = context.getBean(HiringIntentIngestor.class);
    }

    @Override
    protected AloFlux<HiringIntentCreatedEvent> receive() {
        return kafkaConfigSource
            .withClientId(name())
            .withConsumerGroupId(name())
            .withKeyDeserializer(StringDeserializer.class)
            .withValueDeserializer(TypedJsonKafkaDeserializer.class)
            .with(TypedJsonKafkaDeserializer.VALUE_TYPE_CONFIG, HiringIntentCreatedEvent.class)
            .as(AloKafkaReceiver::<String, HiringIntentCreatedEvent>create)
            .receiveAloValues(getRequiredProperty("stream.kafka.hiring.intent.event.topic"));
    }

    @Override
    protected String extractEventKey(HiringIntentCreatedEvent event) {
        return event.getHiringIntentId();
    }

    @Override
    protected Collection<MaterializeCommand> ingest(HiringIntentCreatedEvent event) {
        return Collections.singletonList(ingestor.ingest(event));
    }
}
