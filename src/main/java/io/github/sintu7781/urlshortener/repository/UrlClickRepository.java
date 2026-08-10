package io.github.sintu7781.urlshortener.repository;

import io.github.sintu7781.urlshortener.entity.UrlClick;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlClickRepository extends JpaRepository<UrlClick, Long> {
}
