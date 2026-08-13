package io.github.sintu7781.urlshortener.service.analytics;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Service;

@Service
public class UserAgentParserService {

    private final UserAgentAnalyzer analyzer;

    public UserAgentParserService() {

        this.analyzer =
                UserAgentAnalyzer
                        .newBuilder()
                        .hideMatcherLoadStats()
                        .withCache(10_000)
                        .withField("AgentName")
                        .withField("OperatingSystemName")
                        .withField("DeviceClass")
                        .build();

    }

    public UserAgentInfo parse(String userAgent) {

        if(userAgent == null || userAgent.isEmpty()) {

            return new UserAgentInfo(
                    "Unknown",
                    "Unknown",
                    "Unknown"
            );
        }

        UserAgent parsed =
                analyzer.parse(userAgent);

        return new UserAgentInfo(
                normalize(
                        parsed.getValue("AgentName")
                ),
                normalize(
                        parsed.getValue("OperatingSystemName")
                ),
                normalize(
                        parsed.getValue("DeviceClass")
                )
        );
    }

    private String normalize(String value) {

        if(value == null || value.isBlank()) {
            return "Unknown";
        }

        return value;
    }
}
