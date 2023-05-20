package com.akouma.veyuzwebapp.utils;

import com.akouma.veyuzwebapp.model.Apurement;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class JoursRestant {
    public static Integer getJoursRestant(Apurement apurement) {
        if (apurement.getDateExpiration() == null) {
            return null;
        }
        if (apurement.getIsApured()) {
            return null;
        }
        Calendar dateJour = Calendar.getInstance();
        Long diff = apurement.getDateExpiration().getTime() - dateJour.getTime().getTime();

        return Math.toIntExact((TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1));
    }

}
