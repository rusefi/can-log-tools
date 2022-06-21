package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.CANLineReader;

public class PcanTrcReader implements CANLineReader {
    @Override
    public CANPacket readLine(String line) {
        line = line.trim();
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
