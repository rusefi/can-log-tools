package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.CANLineReader;

public enum EcuMasterDongleReader implements CANLineReader {
    INSTANCE;

    public static final String HEADER = ";";

    @Override
    public CANPacket readLine(String line, String fileName, int lineIndex) {
        if (line.startsWith(";"))
            return null;
        String trimmed = line.trim();
        if (trimmed.isEmpty())
            return null;
        String[] tokens = trimmed.split("\\s+");
        double timeStamp = Double.parseDouble(tokens[1]);
        int sid = Integer.parseInt(tokens[3], 16);

        int size = Integer.parseInt(tokens[4]);
        byte[] data = CANLineReader.readHexArray(tokens, 5, size);
        return new CANPacket(timeStamp, sid, data);
    }
}
