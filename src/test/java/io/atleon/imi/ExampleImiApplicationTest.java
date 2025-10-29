package io.atleon.imi;

import io.atleon.aws.sqs.AloSqsSender;
import io.atleon.aws.sqs.ComposedSqsMessage;
import io.atleon.aws.sqs.SqsConfig;
import io.atleon.aws.sqs.SqsConfigSource;
import io.atleon.aws.sqs.SqsMessage;
import io.atleon.aws.testcontainers.AtleonLocalStackContainer;
import io.atleon.aws.util.AwsConfig;
import io.atleon.core.Alo;
import io.atleon.imi.api.json.CompanyCreatedEvent;
import io.atleon.imi.api.json.HiringIntentCreatedEvent;
import io.atleon.imi.api.json.JobPostingView;
import io.atleon.imi.api.json.MaterializeCommand;
import io.atleon.json.jackson.JsonKafkaSerializer;
import io.atleon.json.jackson.JsonSqsBodySerializer;
import io.atleon.json.jackson.TypedJsonKafkaDeserializer;
import io.atleon.kafka.AloKafkaReceiver;
import io.atleon.kafka.AloKafkaSender;
import io.atleon.kafka.KafkaConfigSource;
import io.atleon.kafka.embedded.EmbeddedKafka;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.time.Duration;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ContextConfiguration(initializers = ExampleImiApplicationTest.Initializer.class)
@ActiveProfiles({"integrationTest"})
public class ExampleImiApplicationTest {

    private static final String KAFKA_BOOTSTRAP_SERVERS = EmbeddedKafka.startAndGetBootstrapServersConnect();

    private static final AtleonLocalStackContainer LOCAL_STACK_CONTAINER = AtleonLocalStackContainer.createAndStart();

    private static final String HIRING_INTENT_EVENT_TOPIC = "hiring-intents";

    private static final String COMPANY_EVENT_QUEUE = "company-events";

    private static final String COMMAND_TOPIC = "commands";

    private static final String MATERIALIZATION_TOPIC = "views";

    @Autowired
    private String companyEventQueueUrl;

    @Test
    public void execute_givenProducedEvents_expectsIndexableView() {
        CompanyCreatedEvent companyCreatedEvent = new CompanyCreatedEvent("1", "Confluent");
        HiringIntentCreatedEvent hiringIntentCreatedEvent =
            new HiringIntentCreatedEvent("1", "1", "Software Engineer", "Stream all the things!");

        produceCompanyCreatedEvent(companyCreatedEvent);
        produceHiringIntentCreatedEvent(hiringIntentCreatedEvent);

        assertTrue(materializeCommandProduced(hiringIntentCreatedEvent.getHiringIntentId()));
        assertTrue(viewMaterialized(it ->
            hiringIntentCreatedEvent.getHiringIntentId().equals(it.getHiringIntentId())
                && companyCreatedEvent.getCompanyName().equals(it.getCompanyName())
                && hiringIntentCreatedEvent.getJobTitle().equals(it.getJobTitle())
                && hiringIntentCreatedEvent.getJobDescription().equals(it.getJobDescription())));
    }

    private void produceCompanyCreatedEvent(CompanyCreatedEvent event) {
        SqsConfigSource configSource = SqsConfigSource.unnamed()
            .with(AwsConfig.REGION_CONFIG, LOCAL_STACK_CONTAINER.getRegion())
            .with(AwsConfig.CREDENTIALS_PROVIDER_TYPE_CONFIG, AwsConfig.CREDENTIALS_PROVIDER_TYPE_STATIC)
            .with(AwsConfig.CREDENTIALS_ACCESS_KEY_ID_CONFIG, LOCAL_STACK_CONTAINER.getAccessKey())
            .with(AwsConfig.CREDENTIALS_SECRET_ACCESS_KEY_CONFIG, LOCAL_STACK_CONTAINER.getSecretKey())
            .with(SqsConfig.ENDPOINT_OVERRIDE_CONFIG, LOCAL_STACK_CONTAINER.getSqsEndpointOverride())
            .with(AloSqsSender.BODY_SERIALIZER_CONFIG, JsonSqsBodySerializer.class);
        try (AloSqsSender<CompanyCreatedEvent> sender = AloSqsSender.create(configSource)) {
            SqsMessage<CompanyCreatedEvent> message = ComposedSqsMessage.fromBody(event);
            sender.sendMessage(message, companyEventQueueUrl).block();
        }
    }

    private void produceHiringIntentCreatedEvent(HiringIntentCreatedEvent event) {
        KafkaConfigSource configSource = baseKafkaConfigSource()
            .withKeySerializer(StringSerializer.class)
            .withValueSerializer(JsonKafkaSerializer.class);
        try (AloKafkaSender<String, HiringIntentCreatedEvent> sender = AloKafkaSender.create(configSource)) {
            ProducerRecord<String, HiringIntentCreatedEvent> record =
                new ProducerRecord<>(HIRING_INTENT_EVENT_TOPIC, event.getHiringIntentId(), event);
            sender.sendRecord(record).block();
        }
    }

    private static Boolean materializeCommandProduced(String key) {
        AloKafkaReceiver<String, MaterializeCommand> commandReceiver = newImiConsumerConfigSource()
            .with(TypedJsonKafkaDeserializer.VALUE_TYPE_CONFIG, MaterializeCommand.class)
            .as(AloKafkaReceiver::create);
        return commandReceiver.receiveAloRecords(COMMAND_TOPIC)
            .consumeAloAndGet(Alo::acknowledge)
            .any(it -> key.equals(it.key()))
            .block(Duration.ofSeconds(30));
    }

    private static Boolean viewMaterialized(Predicate<JobPostingView> matcher) {
        AloKafkaReceiver<String, JobPostingView> commandReceiver = newImiConsumerConfigSource()
            .with(TypedJsonKafkaDeserializer.VALUE_TYPE_CONFIG, JobPostingView.class)
            .as(AloKafkaReceiver::create);
        return commandReceiver.receiveAloRecords(MATERIALIZATION_TOPIC)
            .consumeAloAndGet(Alo::acknowledge)
            .any(it -> matcher.test(it.value()))
            .block(Duration.ofSeconds(30));
    }

    private static KafkaConfigSource newImiConsumerConfigSource() {
        return baseKafkaConfigSource()
            .withConsumerGroupId("integration-test")
            .with(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            .withKeyDeserializer(StringDeserializer.class)
            .withValueDeserializer(TypedJsonKafkaDeserializer.class);
    }

    private static KafkaConfigSource baseKafkaConfigSource() {
        return KafkaConfigSource.useClientIdAsName()
            .withClientId("integration-test")
            .withBootstrapServers(KAFKA_BOOTSTRAP_SERVERS);
    }

    public static final class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                applicationContext,
                "vars.kafka.bootstrap.servers=" + KAFKA_BOOTSTRAP_SERVERS,
                "vars.aws.region=" + LOCAL_STACK_CONTAINER.getRegion(),
                "vars.aws.credentials.provider.type=" + AwsConfig.CREDENTIALS_PROVIDER_TYPE_STATIC,
                "vars.aws.credentials.access.key.id=" + LOCAL_STACK_CONTAINER.getAccessKey(),
                "vars.aws.credentials.secret.access.key=" + LOCAL_STACK_CONTAINER.getSecretKey(),
                "vars.sqs.endpoint.override=" + LOCAL_STACK_CONTAINER.getSqsEndpointOverride(),
                "stream.kafka.hiring.intent.event.topic=" + HIRING_INTENT_EVENT_TOPIC,
                "stream.sqs.company.event.queue.name=" + COMPANY_EVENT_QUEUE,
                "stream.imi.command.topic=" + COMMAND_TOPIC,
                "stream.imi.materialization.topic=" + MATERIALIZATION_TOPIC
            );
        }
    }

    @TestConfiguration
    public static class Configuration implements BeanPostProcessor {

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) {
            if (bean instanceof KafkaConfigSource) {
                // Ensure all Kafka consumers are configured to consume from earliest such that
                // messages produced by this integration test are consumed even if the test
                // produces those messages before the consumers have set their positions for the
                // first time.
                return KafkaConfigSource.class.cast(bean).with(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            } else {
                return bean;
            }
        }
    }
}