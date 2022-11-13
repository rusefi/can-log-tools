package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.CANLineReader;

/**
 * @see PcanTrcReader1_1 for version 1.1 format
 * TODO: merge these two?
 */
public enum PcanTrcReader2_0 implements CANLineReader {
    INSTANCE;

    public static final String FILEVERSION = ";$FILEVERSION";

    @Override
    public CANPacket readLine(String line) {
        line = line.trim();
        if (line.startsWith(FILEVERSION) && !line.startsWith(FILEVERSION + "=2.0"))
            throw new IllegalStateException("Unexpected fileversion " + line);
        if (line.startsWith(";"))
            return null;
        String[] tokens = line.split("\\s+");
        double timeStamp = Double.parseDouble(tokens[1]);
        int sid = Integer.parseInt(tokens[3], 16);
        int size = Integer.parseInt(tokens[5]);

        byte[] data = CANLineReader.readHexArray(tokens, 6, size);


        return new CANPacket(timeStamp, sid, data);
    }
}
