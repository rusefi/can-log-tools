package com.rusefi.can.reader;

import com.rusefi.can.CANPacket;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CANoeReader implements CANLineReader {
    @Override
    public CANPacket readLine(String line) {
        if (line.contains("ErrorFrame"))
            return null;
        String[] tokens = line.trim().split("\\s+");

        double timeStamp = Double.parseDouble(tokens[0]);

        int sid = Integer.parseInt(tokens[2], 16);
        int counter = Integer.parseInt(tokens[5]);

        byte[] data = new byte[counter];
        for (int i = 0; i < counter; i++)
            data[i] = (byte) Integer.parseInt(tokens[6 + i], 16);
        return new CANPacket(timeStamp, sid, data);
    }

    public List<CANPacket> readFile(String fileName) throws IOException {
        List<CANPacket> result = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(new Consumer<String>() {
                int index = 0;

                @Override
                public void accept(String s) {
                    if (index++ < 5)
                        return;
                    CANPacket packet = readLine(s);
                    if (packet != null)
                        result.add(packet);
                }
            });
            return result;
        }
    }
}
