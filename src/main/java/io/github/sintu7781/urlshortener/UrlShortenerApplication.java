package io.github.sintu7781.urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableScheduling
public class UrlShortenerApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                UrlShortenerApplication.class,
                args
        );
    }

}
