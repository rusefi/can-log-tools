package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.CANLineReader;

public enum SomethingLinuxReader implements CANLineReader {
    INSTANCE;

    public static final CharSequence HEADER = ") can0 ";


    @Override
    public CANPacket readLine(String line, String fileName, int lineIndex) {
        String trimmed = line.trim();
        if (trimmed.isEmpty())
            return null;
        String[] tokens = trimmed.split("\\s+");
        if (tokens.length != 3)
            throw new IllegalStateException("Three tokens expected [" + trimmed + "]");

        String time = tokens[0].substring(1, tokens[0].length() - 2);

        String mainToken = tokens[2];
        int poundIndex = mainToken.indexOf('#');
        int sid = Integer.parseInt(mainToken.substring(0, poundIndex), 16);

        String hex = mainToken.substring(poundIndex + 1);
        if (hex.length() % 2 != 0)
            throw new IllegalStateException("Even length expected " + hex);
        byte[] data = new byte[hex.length() / 2];
        for (int i = 0 ;i < data.length; i++) {
            String twoSymbols = hex.substring(2 * i, 2 * i + 2);
            data[i] = (byte) Integer.parseInt(twoSymbols, 16);
        }

        return new CANPacket(Double.parseDouble(time), sid, data);
    }
}
