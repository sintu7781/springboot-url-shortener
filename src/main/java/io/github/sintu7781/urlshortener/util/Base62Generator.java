package io.github.sintu7781.urlshortener.util;

import java.security.SecureRandom;

public final class Base62Generator {

    private static final String CHARACTERS =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrst";

    private static final SecureRandom RANDOM = new SecureRandom();

    private Base62Generator() {
    }

    public static String generate(int length) {

        StringBuilder builder = new StringBuilder(length);

        for(int i=0;i<length;i++) {
            builder.append(
                    CHARACTERS.charAt(
                            RANDOM.nextInt(CHARACTERS.length())
                    )
            );
        }

        return builder.toString();
    }
}
