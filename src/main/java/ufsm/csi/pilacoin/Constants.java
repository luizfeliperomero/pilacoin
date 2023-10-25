package ufsm.csi.pilacoin;

import org.springframework.beans.factory.annotation.Value;

public class Constants {
    private Constants() {}
    public static final String PUBLICKEY = "[48, -127, -97, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -127, -115, 0, 48, -127, -119, 2, -127, -127, 0, -84, 114, 108, 116, 6, -68, -105, 113, 49, 15, 106, -6, 45, -57, -41, -24, -80, 18, -46, 95, 29, 31, 45, -47, 81, 107, 21, -30, -16, -26, 89, -66, 109, -49, -82, -116, -86, 9, -46, 44, -7, -65, 91, -21, -35, 37, 23, 21, 55, 55, -125, -85, -117, -61, 112, 32, -80, -92, -104, 30, 76, -97, 127, 77, -103, 119, -44, 72, 16, 100, 116, 39, 65, -17, 88, -108, -24, -71, -114, 29, 64, -128, 74, 38, -34, 54, -106, 77, 60, -18, 5, 69, -90, -107, 87, 8, -119, -126, -60, 59, -73, 21, 27, 74, 33, 73, -117, -1, -74, 124, 97, -53, 1, 85, -51, -1, 71, -125, -34, 72, -17, -11, 127, -51, -82, -92, -87, -83, 2, 3, 1, 0, 1]";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\033[0;91m";
    public static final String ANSI_GREEN = "\033[0;92m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE ="\u001B[37m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String YELLOW_BOLD_BRIGHT = "\033[1;93m";
    public static final String WHITE_BOLD_BRIGHT = "\033[1;97m";
    public static final String BLACK_BACKGROUND = "\033[40m";
    public static final String PURPLE_BOLD_BRIGHT = "\033[1;95m";
    public static final int MINING_THREADS_NUMBER = 4;
}
