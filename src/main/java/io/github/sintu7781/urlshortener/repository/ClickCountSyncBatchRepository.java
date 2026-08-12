package io.github.sintu7781.urlshortener.repository;

import io.github.sintu7781.urlshortener.entity.ClickCountSyncBatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClickCountSyncBatchRepository
        extends JpaRepository<ClickCountSyncBatch, String> {
}
