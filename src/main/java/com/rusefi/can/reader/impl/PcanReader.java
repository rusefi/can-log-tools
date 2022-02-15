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

public enum PcanReader implements CANLineReader {
    INSTANCE;

    @Override
    public CANPacket readLine(String line) {
        line = line.trim();
        if (line.startsWith(";"))
            return null;
        String[] tokens = line.split("\\s+");
        double timeStamp = Double.parseDouble(tokens[1]);
        int sid = Integer.parseInt(tokens[3], 16);
        int size = Integer.parseInt(tokens[5]);

        byte[] data = CANLineReader.readHexArray(tokens, 6, size);


        return new CANPacket(timeStamp, sid, data);
    }

    @Override
    public List<CANPacket> readFile(String fileName) throws IOException {
        List<CANPacket> result = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(new Consumer<String>() {
                @Override
                public void accept(String s) {
                    CANPacket packet = readLine(s);
                    if (packet != null)
                        result.add(packet);
                }
            });
            return result;
        }
    }

}
