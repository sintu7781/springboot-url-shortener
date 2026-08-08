package io.github.sintu7781.urlshortener.repository;

import io.github.sintu7781.urlshortener.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

    Optional<Url> findByOriginalUrl(String originalUrl);

    boolean existsByShortCode(String shortCode);

    @Query(value = "SELECT nextval('url_id_seq')", nativeQuery = true)
    Long getNextId();
}
