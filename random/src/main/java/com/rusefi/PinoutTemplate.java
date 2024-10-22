package com.rusefi;

public class PinoutTemplate {
    private static final String PIN_PREFIX = "X1-";
    private static final int TO = 73;

    public static void main(String[] args) {


        for (int i = 1; i <= TO; i++) {
            System.out.println("  - pin: " + PIN_PREFIX + i);
            System.out.println("    function: ");
            System.out.println("    type: ");

            if (i % 10 == 0)
                System.out.println("\n");
        }
    }
}
