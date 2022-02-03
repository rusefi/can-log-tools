package com.rusefi.can.reader;

import com.rusefi.can.CANPacket;

public class CANoeReader {
    public CANPacket readLine(String line) {
        String[] tokens = line.split("\\s+");

        double timeStamp = Double.parseDouble(tokens[0]);

        int sid = Integer.parseInt(tokens[2], 16);
        int counter = Integer.parseInt(tokens[5]);

        byte[] data = new byte[counter];
        for (int i = 0; i < counter; i++)
            data[i] = (byte) Integer.parseInt(tokens[6 + i], 16);
        return new CANPacket(timeStamp, sid, data);


    }
}
