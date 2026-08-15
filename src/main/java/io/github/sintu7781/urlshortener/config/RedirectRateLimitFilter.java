package io.github.sintu7781.urlshortener.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedirectRateLimitFilter
        extends OncePerRequestFilter {

    private static final String KEY_PREFIX =
            "rate-limit:redirect:";

    private final StringRedisTemplate redisTemplate;

    @Value("${rate-limit.redirect.max-requests:1000}")
    private long maxRequests;

    @Value("${rate-limit.redirect.window-seconds:60}")
    private long windowSeconds;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if(!isRedirectRequest(request)) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String clientIp =
                getClientIp(request);

        String key =
                KEY_PREFIX + clientIp;

        try {

            Long count =
                    redisTemplate.opsForValue()
                            .increment(key);

            if (count != null && count == 1L) {

                redisTemplate.expire(
                        key,
                        Duration.ofSeconds(windowSeconds)
                );
            }

            if (count != null &&
                    count > maxRequests) {

                response.setStatus(
                        HttpStatus.TOO_MANY_REQUESTS.value()
                );

                response.setHeader(
                        "Retry-After",
                        String.valueOf(
                                windowSeconds
                        )
                );

                return;
            }
        } catch (Exception ex) {

            log.warn(
                    "Rate limiter unavailable; allowing redirect. ip={}",
                    clientIp,
                    ex
            );
        }

        filterChain.doFilter(
                request,
                response
        );
    }

    private boolean isRedirectRequest(
            HttpServletRequest request
    ) {

        return "GET".equalsIgnoreCase(
                request.getMethod()
        )
                && request.getRequestURI()
                .matches(
                        "^/[A-Za-z0-9_-]{1,30}$"
        );
    }

    private String getClientIp(
            HttpServletRequest request
    ) {

        return request.getRemoteAddr();
    }
}
