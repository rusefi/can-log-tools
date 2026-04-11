package com.rusefi.can;

public class DualSid {
    public static final String HEX_ID_FORMAT = "%03X";

    // convention: decimal first, upper case three digit hex second
    private static String dualSid(int sid, String separator) {
        return String.format("%d%s" + HEX_ID_FORMAT, sid, separator, sid);
    }

    public static String dualSid(int sid) {
        return dualSid(sid, "_");
    }
}
