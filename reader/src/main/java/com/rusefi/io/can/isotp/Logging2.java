package com.rusefi.io.can.isotp;

public class Logging2 {
    private boolean isDebugEnabled;

    public static Logging2 getLogging(Class<?> parent) {
        return new Logging2();
    }

    public void configureDebugEnabled(boolean debugEnabled) {
        this.isDebugEnabled = debugEnabled;
    }

    public boolean debugEnabled() {
        return isDebugEnabled;
    }

    public void debug(String s) {
        System.out.println(s);
    }
}
