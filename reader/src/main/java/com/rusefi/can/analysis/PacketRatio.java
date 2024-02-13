package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PacketRatio {

    public static void write(String reportDestinationFolder, List<CANPacket> logFileContent, String simpleFileName) throws IOException {

        Map<Integer, AtomicInteger> countBySID = new TreeMap<>();

        for (CANPacket packet : logFileContent) {
            AtomicInteger counter = countBySID.computeIfAbsent(packet.getId(), integer -> new AtomicInteger());
            counter.incrementAndGet();
        }

        Writer w = new FileWriter(reportDestinationFolder + File.separator + "distribution_" + simpleFileName + ".txt");

        for (Map.Entry<Integer, AtomicInteger> e : countBySID.entrySet()) {
            double ratio = 100.0 * e.getValue().get() / logFileContent.size();
            w.write(ByteRateOfChange.dualSid(e.getKey()) + ": " + ratio + "\n");
        }
        w.close();
    }
}
