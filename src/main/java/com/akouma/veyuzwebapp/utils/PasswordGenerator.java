package com.akouma.veyuzwebapp.utils;

import java.util.Random;

public class PasswordGenerator {
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";
    private static final String PASSWORD_ALLOW_BASE = CHAR_LOWER + CHAR_UPPER + NUMBER;
    private static final String PASSWORD_ALLOW_BASE_WITH_UPPER_AND_NUMBER = CHAR_LOWER + CHAR_UPPER + NUMBER;
    private static final Random random = new Random();

    public static String generatePassword() {
        int passwordLength = 8;
        StringBuilder password = new StringBuilder(passwordLength);

        // At least one upper case character
        int randomUpperCasePosition = random.nextInt(passwordLength);
        password.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));

        // At least one alphanumeric character
        password.append(PASSWORD_ALLOW_BASE_WITH_UPPER_AND_NUMBER.charAt(random.nextInt(PASSWORD_ALLOW_BASE_WITH_UPPER_AND_NUMBER.length())));

        for (int i = 2; i < passwordLength; i++) {
            password.append(PASSWORD_ALLOW_BASE.charAt(random.nextInt(PASSWORD_ALLOW_BASE.length())));
        }

        return password.toString();
    }
}
