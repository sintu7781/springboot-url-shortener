package io.github.sintu7781.urlshortener.dto.response;

public record ReplayAudit(
        String newStreamId,
        String replayedAt
) {
}
