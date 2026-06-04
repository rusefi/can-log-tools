package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import com.rusefi.can.dbc.DbcFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reports the per-ID packet count distribution (ratios) across a CAN trace.
 */
public class PacketCountDistributionTool {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: PacketRatio <dbcFile> <traceFile>");
            com.rusefi.can.util.ToolRepository.exitWithErrorCodeUnlessToolRegistry();
            return;
        }
        DbcFile dbc = com.rusefi.can.dbc.reader.DbcFileReader.readFromFile(args[0]);
        List<CANPacket> packets = com.rusefi.can.reader.impl.AutoFormatReader.INSTANCE.readFile(args[1]);
        write(dbc, ".", packets, new java.io.File(args[1]).getName());
    }

    public static void write(DbcFile dbc, String reportDestinationFolder, List<CANPacket> logFileContent, String simpleFileName) throws IOException {

        Map<Integer, AtomicInteger> countBySID = new TreeMap<>();

        for (CANPacket packet : logFileContent) {
            AtomicInteger counter = countBySID.computeIfAbsent(packet.getId(), integer -> new AtomicInteger());
            counter.incrementAndGet();
        }

        Writer w = new FileWriter(reportDestinationFolder + File.separator + "distribution_" + simpleFileName + ".txt");

        for (Map.Entry<Integer, AtomicInteger> e : countBySID.entrySet()) {
            int countOfThisSid = e.getValue().get();
            double ratio = 100.0 * countOfThisSid / logFileContent.size();
            Integer sid = e.getKey();
            String key = DbcFile.getPacketName(dbc, sid);
            w.write(key + " ratio=" + ratio + " count=" + countOfThisSid + "\n");
        }
        w.close();
    }
}
