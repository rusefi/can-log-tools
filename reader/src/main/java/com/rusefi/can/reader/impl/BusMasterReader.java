package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.CANLineReader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public enum BusMasterReader implements CANLineReader {
    INSTANCE;

    public static final CharSequence HEADER = "BUSMASTER Ver 3";
    final String FORMAT = "HH:mm:ss:SSS";
    DateFormat formatter = new SimpleDateFormat(FORMAT);

    @Override
    public CANPacket readLine(String line, String fileName, int lineIndex) {
        if (line.startsWith("***"))
            return null;
        String trimmed = line.trim();
        if (trimmed.isEmpty())
            return null;
        String[] tokens = trimmed.split("\\s+");
        if (tokens.length < 7)
            throw new IllegalStateException("Unexpected " + Arrays.toString(tokens));
        String hexId = tokens[3];
        String lenghtString = tokens[5];

        int sid = Integer.parseInt(hexId.substring(2), 16);
        int size = Integer.parseInt(lenghtString);

        long timeStamp;
        byte[] data = CANLineReader.readHexArray(tokens, 6, size);
        try {
            Date date = formatter.parse(tokens[0]);
            timeStamp = date.getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return new CANPacket(timeStamp, sid, data);
    }
}
