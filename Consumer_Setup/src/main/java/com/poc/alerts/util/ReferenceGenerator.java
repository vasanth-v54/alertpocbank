package com.poc.alerts.util;

import java.security.SecureRandom;

public class ReferenceGenerator {

    private static final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();

    public static String generateReferenceId() {

        StringBuilder ref = new StringBuilder(12);

        for (int i = 0; i < 12; i++) {
            ref.append(ALPHA_NUMERIC.charAt(random.nextInt(ALPHA_NUMERIC.length())));
        }

        return ref.toString();
    }
}