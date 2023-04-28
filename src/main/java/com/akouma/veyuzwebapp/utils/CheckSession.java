package com.akouma.veyuzwebapp.utils;

import com.akouma.veyuzwebapp.model.Banque;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

public class CheckSession {



    public static final boolean checkSessionData (HttpSession session) {

        Banque banque = (Banque) session.getAttribute("banque");
        System.out.println(banque);

        return banque != null;
    }

}
