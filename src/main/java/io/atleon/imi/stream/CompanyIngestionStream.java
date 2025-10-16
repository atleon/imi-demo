package io.atleon.imi.stream;

import io.atleon.aws.sqs.AloSqsReceiver;
import io.atleon.aws.sqs.SqsConfigSource;
import io.atleon.core.AloFlux;
import io.atleon.imi.api.json.CompanyCreatedEvent;
import io.atleon.imi.api.json.MaterializeCommand;
import io.atleon.imi.service.CompanyIngestor;
import io.atleon.json.jackson.TypedJsonSqsBodyDeserializer;
import io.atleon.spring.AutoConfigureStream;
import org.springframework.context.ApplicationContext;

import java.util.Collection;

@AutoConfigureStream
public class CompanyIngestionStream extends IngestionStream<CompanyCreatedEvent> {

    private final SqsConfigSource sqsConfigSource;

    private final String queueUrl;

    private final CompanyIngestor ingestor;

    public CompanyIngestionStream(ApplicationContext context) {
        super(context);
        this.sqsConfigSource = context.getBean("sqsConfigSource", SqsConfigSource.class);
        this.queueUrl = context.getBean("companyEventQueueUrl", String.class);
        this.ingestor = context.getBean(CompanyIngestor.class);
    }

    @Override
    protected AloFlux<CompanyCreatedEvent> receive() {
        return sqsConfigSource
            .with(AloSqsReceiver.BODY_DESERIALIZER_CONFIG, TypedJsonSqsBodyDeserializer.class)
            .with(TypedJsonSqsBodyDeserializer.TYPE_CONFIG, CompanyCreatedEvent.class)
            .as(AloSqsReceiver::<CompanyCreatedEvent>create)
            .receiveAloBodies(queueUrl);
    }

    @Override
    protected String extractEventKey(CompanyCreatedEvent event) {
        return event.getCompanyId();
    }

    @Override
    protected Collection<MaterializeCommand> ingest(CompanyCreatedEvent event) {
        return ingestor.ingest(event);
    }
}
