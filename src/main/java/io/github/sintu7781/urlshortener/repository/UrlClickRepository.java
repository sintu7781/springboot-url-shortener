package io.github.sintu7781.urlshortener.repository;

import io.github.sintu7781.urlshortener.entity.UrlClick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface UrlClickRepository
        extends JpaRepository<UrlClick, Long> {

    boolean existsByEventId(String eventId);

    long countByUrlId(Long urlId);

    long countByUrlIdAndClickedAtGreaterThanEqual(
            Long urlId,
            Instant from
    );

    long countByUrlIdAndClickedAtBetween(
            Long urlId,
            Instant from,
            Instant to
    );

    @Query("""
            SELECT MAX(c.clickedAt)
            FROM UrlClick c
            WHERE c.url.id = :urlId
    """)
    Instant findLastClickedAt(
            @Param("urlId") Long urlId
    );
}
