package io.atleon.imi.rest;

import io.atleon.aws.sqs.AloSqsSender;
import io.atleon.aws.sqs.ComposedSqsMessage;
import io.atleon.aws.sqs.SqsConfigSource;
import io.atleon.aws.sqs.SqsMessage;
import io.atleon.imi.api.json.CompanyCreatedEvent;
import io.atleon.imi.api.json.HiringIntentCreatedEvent;
import io.atleon.json.jackson.JsonKafkaSerializer;
import io.atleon.json.jackson.JsonSqsBodySerializer;
import io.atleon.kafka.AloKafkaSender;
import io.atleon.kafka.KafkaConfigSource;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/")
public class DemoController {

    private final AloSqsSender<CompanyCreatedEvent> companyEventSender;

    private final AloKafkaSender<String, HiringIntentCreatedEvent> hiringIntentSender;

    private final String companyEventQueueUrl;

    private final String hiringIntentEventTopic;

    public DemoController(
        @Qualifier("sqsConfigSource") SqsConfigSource sqsConfigSource,
        @Qualifier("kafkaConfigSource") KafkaConfigSource kafkaConfigSource,
        @Qualifier("companyEventQueueUrl") String companyEventQueueUrl,
        @Value("${stream.kafka.hiring.intent.event.topic}") String hiringIntentEventTopic
    ) {
        this.companyEventSender = sqsConfigSource
            .with(AloSqsSender.BODY_SERIALIZER_CONFIG, JsonSqsBodySerializer.class)
            .as(AloSqsSender::create);
        this.hiringIntentSender = kafkaConfigSource
            .withClientId(DemoController.class.getSimpleName())
            .withKeySerializer(StringSerializer.class)
            .withValueSerializer(JsonKafkaSerializer.class)
            .as(AloKafkaSender::create);
        this.companyEventQueueUrl = companyEventQueueUrl;
        this.hiringIntentEventTopic = hiringIntentEventTopic;
    }

    @PostMapping(path = "companyCreatedEvents", consumes = "application/json")
    public ResponseEntity<String> produceCompanyCreatedEvent(@RequestBody CompanyCreatedEvent event) {
        SqsMessage<CompanyCreatedEvent> message = ComposedSqsMessage.fromBody(event);

        companyEventSender.sendMessage(message, companyEventQueueUrl).block();

        return ResponseEntity.ok("Produced Company Created event for id=" + event.getCompanyId());
    }

    @PostMapping(path = "hiringIntentCreatedEvents", consumes = "application/json")
    public ResponseEntity<String> produceHiringIntentCreatedEvent(@RequestBody HiringIntentCreatedEvent event) {
        ProducerRecord<String, HiringIntentCreatedEvent> producerRecord =
            new ProducerRecord<>(hiringIntentEventTopic, event.getHiringIntentId(), event);

        hiringIntentSender.sendRecord(producerRecord).block();

        return ResponseEntity.ok("Produced Hiring Intent Created event for id=" + event.getHiringIntentId());
    }
}
