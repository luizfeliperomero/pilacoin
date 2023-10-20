package ufsm.csi.pilacoin.service;

public class MessageFormatterService {
    public static String surroundMessage(String surround, String message) {
        String surrounds = surround.repeat(message.length());
        return surrounds + "\n" + message + "\n" + surrounds;
    }

    public static String threadIdentifierMessage(Thread thread) {
        return thread.getName() + ": ";
    }
}
