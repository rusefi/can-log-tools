package com.rusefi.can.reader;

import com.rusefi.can.CANPacket;
import com.rusefi.can.TrcToMlq;
import com.rusefi.can.reader.impl.CANoeReader;
import com.rusefi.can.reader.impl.CanHackerReader;
import com.rusefi.can.reader.impl.PcanTrcReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface CANLineReader {
    char FLOAT_DIVIDER = new Double(0).toString().charAt(1);

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
            case CANHACKER:
                return CanHackerReader.INSTANCE;
            case PCAN:
            default:
                return new PcanTrcReader();
        }
    }

    CANPacket readLine(String line);

    default List<CANPacket> readFile(String fileName) throws IOException {
        return skipHeaderAndRead(fileName, 0);
    }

    default List<CANPacket> skipHeaderAndRead(String fileName, final int skipCount) throws IOException {
        List<CANPacket> result = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(new Consumer<String>() {
                int index = 0;

                @Override
                public void accept(String s) {
                    if (index++ < skipCount)
                        return;
                    CANPacket packet = readLine(s);
                    if (packet != null)
                        result.add(packet);
                }
            });
            return result;
        }
    }

    static String attemptToFixLocalization(String maybeInvalidString) {
        if (FLOAT_DIVIDER == '.')
            return maybeInvalidString.replace(',', '.'); // converting RU file on EN device
        return maybeInvalidString.replace('.', ',');// converting US file on RU device
    }
}
