package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.CANLineReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public enum CANoeReader implements CANLineReader {
    INSTANCE;

    @Override
    public CANPacket readLine(String line) {
        if (line.contains("ErrorFrame"))
            return null;
        String[] tokens = line.trim().split("\\s+");

        double timeStamp = Double.parseDouble(tokens[0]);
        int sid = Integer.parseInt(tokens[2], 16);
        int size = Integer.parseInt(tokens[5]);

        byte[] data = CANLineReader.readHexArray(tokens, 6, size);
        return new CANPacket(timeStamp, sid, data);
    }

    @Override
    public List<CANPacket> readFile(String fileName) throws IOException {
        return skipHeaderAndRead(fileName, 5);
    }
}
