package ufsm.csi.pilacoin.service;

import ufsm.csi.pilacoin.Constants;

public class MessageFormatterService {
    public static String surroundMessage(String surround, String message) {
        String surrounds = surround.repeat(message.length());
        return surrounds + "\n" + message + "\n" + surrounds;
    }

    public static String threadIdentifierMessage(Thread thread) {
        return Constants.YELLOW_BOLD_BRIGHT + thread.getName() + Constants.ANSI_RESET  + ": ";
    }
}
