package io.atleon.imi.config;

import io.atleon.aws.sqs.SqsConfigSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class SqsConfiguration {

    @Bean("companyEventQueueUrl")
    public String companyEventQueueUrl(
        SqsConfigSource sqsConfigSource,
        @Value("${stream.sqs.company.event.queue.name}") String queueName
    ) {
        return createQueueUrl(sqsConfigSource, queueName);
    }

    private static String createQueueUrl(SqsConfigSource configSource, String queueName) {
        try (SqsAsyncClient client = configSource.create().block().buildClient()) {
            return client.createQueue(builder -> builder.queueName(queueName)).join().queueUrl();
        }
    }
}
