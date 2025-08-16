package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.CANLineReader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum IxxatReader implements CANLineReader {
    INSTANCE;

    final String FORMAT = "HH:mm:ss.SSS";
    DateFormat formatter = new SimpleDateFormat(FORMAT);
    static Pattern pattern = Pattern.compile("\"([^\"]*(?:\"\"[^\"]*)*)\"|([^,]+)");

    public static final String START_TIME = "Start Time";

    public static List<String> tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        // This regex matches either:
        // 1. A sequence of characters inside double quotes (including escaped quotes)
        // 2. Or, a sequence of characters that are not commas
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            // Group 1 is the quoted string (without the quotes)
            if (matcher.group(1) != null) {
                // Handle escaped quotes (e.g., "" -> ")
                tokens.add(matcher.group(1).replace("\"\"", "\""));
            }
            // Group 2 is the unquoted string
            else if (matcher.group(2) != null) {
                tokens.add(matcher.group(2));
            }
        }
        return tokens;
    }

    @Override
    public CANPacket readLine(String line, String fileName, int lineIndex) {
        List<String> tokensList = tokenize(line);
        String[] tokens = tokensList.toArray(new String[0]);
        if (!tokens[0].startsWith("USB-to-CAN V2 compact"))
            return null;
        int sid;
        int size;
        try {
            sid = Integer.parseInt((tokens[4]), 16);
            size = Integer.parseInt((tokens[5]));
        } catch (NumberFormatException e) {
            throw new RuntimeException("While parting " + line, e);
        }
        byte[] data = new byte[size];
        String payload = (tokens[6]);
        String[] payloadTokens = payload.split(" ");
        for (int i = 0; i < size; i++) {
            data[i] = (byte) Integer.parseInt(payloadTokens[i], 16);
        }

        double timeStampMs;
        try {
            timeStampMs = formatter.parse((tokens[2])).getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return new CANPacket(timeStampMs, sid, data);
    }
}
