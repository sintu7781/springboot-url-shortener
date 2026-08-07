package io.github.sintu7781.urlshortener.repository;

import io.github.sintu7781.urlshortener.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

    Optional<Url> findByOriginalUrl(String originalUrl);

    boolean existsByShortCode(String shortCode);
}
