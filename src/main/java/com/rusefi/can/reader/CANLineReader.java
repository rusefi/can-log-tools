package com.rusefi.can.reader;

import com.rusefi.can.CANPacket;
import com.rusefi.can.TrcToMlq;
import com.rusefi.can.reader.impl.CANoeReader;
import com.rusefi.can.reader.impl.PcanTrcReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public interface CANLineReader {
    static byte[] readHexArray(String[] tokens, int start, int size) {
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++)
            data[i] = (byte) Integer.parseInt(tokens[start + i], 16);
        return data;
    }

    static CANLineReader getReader() {
        switch (TrcToMlq.getCurrentReaderType()) {
            case CANOE:
                return CANoeReader.INSTANCE;
            case PCAN:
            default:
                return new PcanTrcReader();
        }
    }

    CANPacket readLine(String line);

    default List<CANPacket> readFile(String fileName) throws IOException {
        List<CANPacket> result = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(s -> {
                CANPacket packet = readLine(s);
                if (packet != null)
                    result.add(packet);
            });
            System.out.println("Got " + result.size() + " CAN packets from " + fileName);
            return result;
        }
    }
}
