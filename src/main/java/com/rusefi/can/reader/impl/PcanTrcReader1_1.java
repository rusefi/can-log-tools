package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.CANLineReader;

import static com.rusefi.can.reader.impl.PcanTrcReader2_0.FILEVERSION;

/**
 * @see PcanTrcReader2_0 for version 2.0 format
 * TODO: merge these two?
 */
public class PcanTrcReader1_1 implements CANLineReader {
    @Override
    public CANPacket readLine(String line, String fileName) {
        line = line.trim();
        if (line.startsWith(FILEVERSION) && !line.startsWith(FILEVERSION + "=1.1"))
            throw new IllegalStateException("Unexpected fileversion " + line);
        if (line.startsWith(";"))
            return null;
        String[] tokens = line.split("\\s+");
        double timeStamp = Double.parseDouble(tokens[1]);

        int sid = Integer.parseInt(tokens[3], 16);
        int size = Integer.parseInt(tokens[4]);

        byte[] data = CANLineReader.readHexArray(tokens, 5, size);


        return new CANPacket(timeStamp, sid, data);
    }
}
