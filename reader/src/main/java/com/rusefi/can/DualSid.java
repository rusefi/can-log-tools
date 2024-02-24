package com.rusefi.can;

public class DualSid {
    public static String dualSid(int sid, String separator) {
        return String.format("%d%s0x%x", sid, separator, sid);
    }

    public static String dualSid(int sid) {
        return dualSid(sid, "/");
    }
}
