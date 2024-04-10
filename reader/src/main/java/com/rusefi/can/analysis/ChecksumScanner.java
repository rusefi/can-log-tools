package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChecksumScanner {

    public static final String CHECKSUM_YAML = "checksum.yaml";

    public static void scanForChecksums(String reportDestinationFolder, String simpleFileName, List<CANPacket> packets) throws IOException {
        Map<Integer, AtomicBoolean> isChecksumMap = new HashMap<>();

        J1850_SAE_crc8_Calculator c = new J1850_SAE_crc8_Calculator();

        for (CANPacket packet : packets) {
            AtomicBoolean isChecksum = isChecksumMap.computeIfAbsent(packet.getId(), integer -> new AtomicBoolean(true));
            if (!isChecksum.get())
                continue;
            byte[] data = packet.getData();
            if (data.length != 8) {
                isChecksum.set(false);
                continue;
            }
            byte checksum = c.crc8(data, 7);
            isChecksum.set(data[7] == checksum);
        }


        List<Integer> withChecksum = new ArrayList<>();

        for (Map.Entry<Integer, AtomicBoolean> e : isChecksumMap.entrySet()) {
            if (e.getValue().get()) {
                Integer sid = e.getKey();
                System.out.println("ChecksumScanner: Ends with checksum " + sid);
                withChecksum.add(sid);
            }
        }
        withChecksum.sort(Comparator.naturalOrder());
        Yaml yaml = new Yaml();
        // simpleFileName + "_" +
        String yamlCountersReportFileName = reportDestinationFolder + File.separator + CHECKSUM_YAML;
        System.out.println(new Date() + " Writing report to " + yamlCountersReportFileName);
        yaml.dump(withChecksum, new FileWriter(yamlCountersReportFileName));
    }
}
