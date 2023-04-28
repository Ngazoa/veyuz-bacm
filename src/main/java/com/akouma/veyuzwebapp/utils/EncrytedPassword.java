package com.akouma.veyuzwebapp.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class EncrytedPassword {

    public class EncrytedPasswordUtils {

        // Encryte Password with BCryptPasswordEncoder
        public String encrytePassword(String password) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            return encoder.encode(password);
        }

        public void main(String[] args) {
            String password = "123";
            String encrytedPassword = encrytePassword(password);

            System.out.println("Encryted Password: " + encrytedPassword);
        }

    }

}
