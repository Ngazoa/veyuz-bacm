package com.akouma.veyuzwebapp.utils;

import java.util.ArrayList;
import java.util.Collection;

public class StatusTransaction {
    public static final int WAITING = 0; // En attente
    public static final String WAITING_STR = "waiting";
    public static final String TRESORERIE_STR = "treasury";
    public static final String TRADEM_STR = "tradeM";
    public static final String TRADEC_STR = "tradeC";
    public static final String TOPS1_STR = "topsM";
    public static final String TOPS2_STR = "topsC";
    public static final int MACKED = 1; // Macked
    public static final String MACKED_STR = "maker";
    public static final int CHECKED = 2; // Checked
    public static final String CHECKED_STR = "checked";
    public static final int VALIDATED = 3;
    public static final String VALIDATED_STR = "validated";
    public static final int REJECTED = -1;
    public static final String REJECTED_STR = "rejected";
    public static final String SENDBACK_CUSTOMER_STR = "send-back-to-customer";
    public static final int SENDBACK_CUSTOMER = 4;
    public static final int SENDBACK_MACKER = 5;
    public static final String SENDBACK_MACKER_STR = "send-back-to-macker";
    public static final String SENDBACK_CHECKER_STR = "send-back-to-checker";
    public static final int SENDBACK_CHECKER = 6;
    public static final int TRANSMIS_TRESORERIE = 9;
    public static final int TRANSMIS_MAKER_2 = 7;
    public static final int TRANSMIS_CHECKER_2 = 8;
    public static final String SENDBACK_MAKER2_STR = "send-back-to-Treasory OPS Maker";
    public static final String SENDBACK_CHECKER2_STR = "send-back-to- Treasory OPS checker";

    public static final int INVALID_STATUS = -2;

    // ========================================================================
    // LES TYPES DE TRANSACTIONS
    public static final String TYPE_NORMAL = "normal";
    public static final String TYPE_DOMICILIATION = "domiciliation";


    public static final String[] statuts = {WAITING_STR, MACKED_STR, CHECKED_STR, VALIDATED_STR,REJECTED_STR};

    public static final int DELAY_TRANSACTION_IMPORTATION_BIENS = 30;
    public static final int DELAY_TRANSACTION_IMPORTATION_SERVICES = 90;

    public static final String IMP_BIENS = "biens";
    public static final String IMP_SERVICES = "services";

    public static final String[] TEXT_COLORS = {"text-primary", "text-danger", "text-warning", "text-dark","text-secondary",
            "text-dribbble", "text-color1 ", "text-color2", "text-color3","text-color4",
            "text-color5", "text-color6", "text-color7", "text-color8","text-color9"};
    public static final String[] BG_COLORS = {"#0066ff", "#fa0019", "#ffb10a", "#3d3c3b", "#3d3c3b",
            "#ea4c89", "#dc4e41", "#1ab7ea", "#181717", "#ea4c89",
            "#bd081c", "#0052cc", "#3b5998", "#fff", "#17a2b8"};

}
