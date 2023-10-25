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

    public static String formattedTimeMessage(Long hours, Long minutes, Long seconds) {
        String message = "";

        if (hours > 0) {
            message += hours + (hours == 1 ? " hour" : " hours");

            if (minutes > 0) {
                message += " and " + minutes + (minutes == 1 ? " minute" : " minutes");
            }

            if (seconds > 0) {
                message += " and " + seconds + (seconds == 1 ? " second" : " seconds");
            }
        } else if (minutes > 0) {
            message += minutes + (minutes == 1 ? " minute" : " minutes");

            if (seconds > 0) {
                message += " and " + seconds + (seconds == 1 ? " second" : " seconds");
            }
        } else {
            message = seconds + (seconds == 1 ? " second" : " seconds");
        }

        return message;
    }
}
