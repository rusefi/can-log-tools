package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.CANLineReader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static javax.management.ObjectName.unquote;

public enum TodoReader implements CANLineReader {
    INSTANCE;

    final String FORMAT = "HH:mm:ss.SSS";
    DateFormat formatter = new SimpleDateFormat(FORMAT);

    public static final String START_TIME = "Start Time";

    @Override
    public CANPacket readLine(String line, String fileName, int lineIndex) {
        String[] tokens = line.split(",");
        if (!tokens[0].startsWith("\"USB-to-CAN V2 compact"))
            return null;
        int sid = Integer.parseInt(unquote(tokens[4]), 16);
        int size = Integer.parseInt(unquote(tokens[5]));
        byte[] data = new byte[size];
        String payload = unquote(tokens[6]);
        String[] payloadTokens = payload.split(" ");
        for (int i = 0; i < size; i++) {
            data[i] = (byte) Integer.parseInt(payloadTokens[i], 16);
        }

        double timeStampMs;
        try {
            timeStampMs = formatter.parse(unquote(tokens[2])).getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return new CANPacket(timeStampMs, sid, data);
    }
}
