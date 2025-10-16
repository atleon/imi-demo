package io.atleon.imi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;

@SpringBootApplication
public class ExampleImiApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(ExampleImiApplication.class) {
            @Override
            protected void configureProfiles(ConfigurableEnvironment environment, String[] args) {
                environment.setActiveProfiles("imi");
            }
        };
        springApplication.run(args);
    }
}
