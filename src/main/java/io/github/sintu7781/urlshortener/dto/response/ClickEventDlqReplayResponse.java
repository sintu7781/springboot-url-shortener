package io.github.sintu7781.urlshortener.dto.response;

import lombok.Builder;

@Builder
public record ClickEventDlqReplayResponse(
        String dlqRecordId,
        String newStreamId,
        String replayedAt
) {
}
