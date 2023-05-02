package com.akouma.veyuzwebapp.utils;

import java.time.LocalDate;
import java.util.Date;
import java.util.Random;

public class ReferenceGenerator {
    private static final String PREFIX_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int PREFIX_LENGTH = 3;
    private static final int NUMBER_LENGTH = 8;

    private static final Random random = new Random();

    private static String generateRandomPrefix() {
        StringBuilder sb = new StringBuilder(PREFIX_LENGTH);
        for (int i = 0; i < PREFIX_LENGTH; i++) {
            int index = random.nextInt(PREFIX_CHARS.length());
            sb.append(PREFIX_CHARS.charAt(index));
        }
        return sb.toString();
    }

    private static String generateRandomNumber() {
        Date date = new Date();
        int number = dateToInt(date);
        return String.format("%04d", number);
    }

    public static int dateToInt(Date date) {
        long timeInMillis = date.getTime();
        int timeInSeconds = (int) (timeInMillis / 1000);
        return timeInSeconds;
    }

    public String generateReference() {
        LocalDate date = LocalDate.now();
        int year = date.getYear();
        String prefix = generateRandomPrefix();
        String number = generateRandomNumber();
        return "TT" + number + "" + year + "BA";
    }
}
