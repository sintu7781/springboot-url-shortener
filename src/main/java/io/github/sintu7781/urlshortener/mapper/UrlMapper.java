package io.github.sintu7781.urlshortener.mapper;

import io.github.sintu7781.urlshortener.dto.response.UrlResponse;
import io.github.sintu7781.urlshortener.entity.Url;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UrlMapper {

    @Mapping(
            target = "shortUrl",
            expression =
                    "java(\"http://localhost:8080/\" + url.getShortCode())"
    )
    UrlResponse toResponse(Url url);
}
