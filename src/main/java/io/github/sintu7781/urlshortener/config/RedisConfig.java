package io.github.sintu7781.urlshortener.config;

import io.lettuce.core.MaintNotificationsConfig;
import org.springframework.boot.data.redis.autoconfigure.LettuceClientOptionsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceClientOptionsBuilderCustomizer lettuceClientOptionsCustomizer() {

        return clientOptionsBuilder ->
                clientOptionsBuilder.maintNotificationsConfig(
                MaintNotificationsConfig.disabled()
        );
    }
}
