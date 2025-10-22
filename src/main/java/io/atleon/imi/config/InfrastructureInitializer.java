package io.atleon.imi.config;

import io.atleon.aws.testcontainers.AtleonLocalStackContainer;
import io.atleon.aws.util.AwsConfig;
import io.atleon.kafka.embedded.EmbeddedKafka;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InfrastructureInitializer implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Set<String> activeProfiles = Stream.of(environment.getActiveProfiles()).collect(Collectors.toSet());
        if (activeProfiles.contains("imi") && !activeProfiles.contains("integrationTest")) {
            String bootstrapServers = EmbeddedKafka.startAndGetBootstrapServersConnect();
            AtleonLocalStackContainer container = AtleonLocalStackContainer.createAndStart();
            environment.getPropertySources()
                .addFirst(new MapPropertySource("infrastructure", createProperties(bootstrapServers, container)));
        }
    }

    private static Map<String, Object> createProperties(
        String kafkaBootstrapServers,
        AtleonLocalStackContainer localStackContainer
    ) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("vars.kafka.bootstrap.servers", kafkaBootstrapServers);
        properties.put("vars.aws.region", localStackContainer.getRegion());
        properties.put("vars.aws.credentials.provider.type", AwsConfig.CREDENTIALS_PROVIDER_TYPE_STATIC);
        properties.put("vars.aws.credentials.access.key.id", localStackContainer.getAccessKey());
        properties.put("vars.aws.credentials.secret.access.key", localStackContainer.getSecretKey());
        properties.put("vars.sqs.endpoint.override", localStackContainer.getSqsEndpointOverride().toString());
        return properties;
    }
}
