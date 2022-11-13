package com.rusefi.can.deprecated;

import com.rusefi.can.CANPacket;
import com.rusefi.can.deprecated.decoders.bmw.Bmw0BA;
import com.rusefi.can.deprecated.decoders.bmw.Bmw192;
import com.rusefi.can.reader.CANLineReader;
import com.rusefi.can.reader.impl.CANoeReader;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

/**
 * this code looks to validate BMW logs against hard-coded BMW packet logic? dead stuff since we've moved to DBC approach?
 */
public class CANoeCanValidator {
    public static void main(String[] args) throws IOException {
        CANLineReader reader = CANoeReader.INSTANCE;
        validate("C:\\stuff\\rusefi_documentation\\OEM-Docs\\Bmw\\2003_7_Series_e65\\HeinrichG-V12-E65_ReverseEngineering\\Log1.log", reader);
        validate("C:\\stuff\\rusefi_documentation\\OEM-Docs\\Bmw\\2003_7_Series_e65\\HeinrichG-V12-E65_ReverseEngineering\\Log2.log", reader);

    }

    public static void validate(String inputFileName, CANLineReader reader) throws IOException {
        List<CANPacket> packetList = reader.readFile(inputFileName);

        TreeMap<Integer, Object> allIds = new TreeMap<>();

        for (CANPacket packet : packetList) {
            allIds.put(packet.getId(), packet.getId());

            if (packet.getId() == Bmw192.ID)
                Bmw192.INSTANCE.decode(packet);

            if (packet.getId() == Bmw0BA.ID)
                Bmw0BA.INSTANCE.decode(packet);

        }

        String outputFileName = "all_ids.txt";
        System.out.println("Writing to " + outputFileName);
        try (FileWriter fw = new FileWriter(outputFileName)) {
            for (Integer id : allIds.keySet()) {
                fw.write(Integer.toHexString(id) + "\r\n");
            }
        }
    }
}
