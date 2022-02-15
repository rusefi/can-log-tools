package com.rusefi.can.reader;

import com.rusefi.can.CANPacket;

import java.io.IOException;
import java.util.List;

public interface CANLineReader {
    static byte[] readHexArray(String[] tokens, int start, int size) {
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++)
            data[i] = (byte) Integer.parseInt(tokens[start + i], 16);
        return data;
    }

    CANPacket readLine(String line);

    List<CANPacket> readFile(String fileName) throws IOException;
}
