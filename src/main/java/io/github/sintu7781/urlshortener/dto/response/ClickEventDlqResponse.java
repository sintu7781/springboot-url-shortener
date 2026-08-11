package io.github.sintu7781.urlshortener.dto.response;

import lombok.Builder;

@Builder
public record ClickEventDlqResponse(
        String recordId,
        String event,
        String originalId,
        String deliveryCount,
        String reason
) {
}
