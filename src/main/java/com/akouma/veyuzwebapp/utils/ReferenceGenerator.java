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
        // Get the current instance date
        Date date = new Date();

        // Set the seed of the random number generator to the milliseconds of the current date
        Random random = new Random(date.getTime());

        // Generate a random number
        int randomNumber = random.nextInt(10000);

        // Get the first 4 digits of the random number
        String firstFourDigits = String.format("%04d", randomNumber).substring(0, 4);

        return firstFourDigits;
    }

    public static int dateToInt(Date date) {
        long timeInMillis = date.getTime();
        int timeInSeconds = (int) (timeInMillis / 1000);
        return timeInSeconds;
    }

    public String generateReference() {
        LocalDate date = LocalDate.now();
        int year = date.getYear();
        String year2 = String.valueOf(year).substring(2);
        String number = generateRandomNumber();
        return "TT" + number + "" + year2 + "BA";
    }
}
