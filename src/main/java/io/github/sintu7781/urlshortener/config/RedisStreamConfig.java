package io.github.sintu7781.urlshortener.config;

import io.github.sintu7781.urlshortener.service.analytics.ClickEventPublisher;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(RedisStreamProperties.class)
public class RedisStreamConfig {

    private final StringRedisTemplate redisTemplate;

    private final RedisStreamProperties properties;

    @PostConstruct
    public void initialize() {

        String stream =
                ClickEventPublisher.CLICK_EVENT_STREAM;

        String group =
                ClickEventPublisher.CLICK_EVENT_GROUP;

        try {

            redisTemplate.opsForStream()
                    .createGroup(
                            stream,
                            ReadOffset.latest(),
                            group
                    );

        } catch (Exception ex) {

            if(!isGroupAlreadyExistsException(ex)) {

                throw new IllegalStateException(
                        "Failed to initialize Redis click-event stream.",
                        ex
                );
            }
        }
    }

    public String consumerName() {

        return "click-worker-" +
                properties.getInstanceId();
    }

    private boolean isGroupAlreadyExistsException(
            Exception ex
    ) {

        Throwable cause = ex;

        while(cause != null) {

            if(cause.getMessage() != null &&
                cause.getMessage()
                        .contains("BUSYGROUP")) {

                return true;
            }

            cause = cause.getCause();
        }

        return false;
    }
}
