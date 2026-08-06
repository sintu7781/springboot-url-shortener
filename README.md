url-shortener/
│
├── gradle/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── io/
│   │   │       └── github/
│   │   │           └── sintu7781/
│   │   │               └── urlshortener/
│   │   │
│   │   │                   ├── UrlShortenerApplication.java
│   │   │                   │
│   │   │                   ├── config/
│   │   │                   │   ├── RedisConfig.java
│   │   │                   │   ├── SecurityConfig.java
│   │   │                   │   ├── JacksonConfig.java
│   │   │                   │   ├── CacheConfig.java
│   │   │                   │   └── WebConfig.java
│   │   │                   │
│   │   │                   ├── controller/
│   │   │                   │   ├── UrlController.java
│   │   │                   │   ├── RedirectController.java
│   │   │                   │   └── HealthController.java
│   │   │                   │
│   │   │                   ├── service/
│   │   │                   │   ├── UrlService.java
│   │   │                   │   ├── UrlServiceImpl.java
│   │   │                   │   ├── ShortCodeGenerator.java
│   │   │                   │   ├── RedirectService.java
│   │   │                   │   └── AnalyticsService.java
│   │   │                   │
│   │   │                   ├── repository/
│   │   │                   │   ├── UrlRepository.java
│   │   │                   │   └── AnalyticsRepository.java
│   │   │                   │
│   │   │                   ├── entity/
│   │   │                   │   ├── Url.java
│   │   │                   │   └── ClickAnalytics.java
│   │   │                   │
│   │   │                   ├── dto/
│   │   │                   │   ├── request/
│   │   │                   │   │   ├── CreateShortUrlRequest.java
│   │   │                   │   │   └── UpdateUrlRequest.java
│   │   │                   │   │
│   │   │                   │   └── response/
│   │   │                   │       ├── UrlResponse.java
│   │   │                   │       ├── AnalyticsResponse.java
│   │   │                   │       └── ErrorResponse.java
│   │   │                   │
│   │   │                   ├── mapper/
│   │   │                   │   └── UrlMapper.java
│   │   │                   │
│   │   │                   ├── exception/
│   │   │                   │   ├── GlobalExceptionHandler.java
│   │   │                   │   ├── UrlNotFoundException.java
│   │   │                   │   ├── DuplicateKeyException.java
│   │   │                   │   └── InvalidUrlException.java
│   │   │                   │
│   │   │                   ├── util/
│   │   │                   │   ├── Base62Encoder.java
│   │   │                   │   ├── UrlValidator.java
│   │   │                   │   ├── IpUtil.java
│   │   │                   │   ├── TimeUtil.java
│   │   │                   │   └── Constants.java
│   │   │                   │
│   │   │                   ├── cache/
│   │   │                   │   └── UrlCacheService.java
│   │   │                   │
│   │   │                   ├── scheduler/
│   │   │                   │   └── ExpiredUrlCleanupJob.java
│   │   │                   │
│   │   │                   ├── security/
│   │   │                   │   ├── JwtFilter.java
│   │   │                   │   ├── JwtService.java
│   │   │                   │   └── UserDetailsServiceImpl.java
│   │   │                   │
│   │   │                   └── validation/
│   │   │                       ├── UrlConstraint.java
│   │   │                       └── UrlValidatorImpl.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── logback-spring.xml
│   │       └── db/
│   │           └── migration/
│   │               ├── V1__Create_Url_Table.sql
│   │               ├── V2__Create_Analytics_Table.sql
│   │               └── ...
│   │
│   └── test/
│       └── java/
│           └── io/
│               └── github/
│                   └── sintu7781/
│                       └── urlshortener/
│                           ├── controller/
│                           ├── service/
│                           ├── repository/
│                           └── integration/
│
├── .gitignore
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── docker-compose.yml
├── Dockerfile
├── README.md
└── LICENSE