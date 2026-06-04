package com.rusefi.can.analysis.filter;

import com.rusefi.can.CANPacket;
import com.rusefi.can.DualSid;
import com.rusefi.can.dbc.DbcFile;
import com.rusefi.can.dbc.DbcPacket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Groups CAN packets/fields by sender/source (per DBC) and writes a per-source report.
 */
public class ReportBySenderTool {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: ReportBySource <dbcFile> <traceFile>");
            com.rusefi.can.util.ToolRepository.exitWithErrorCodeUnlessToolRegistry();
            return;
        }
        DbcFile dbc = com.rusefi.can.dbc.reader.DbcFileReader.readFromFile(args[0]);
        List<CANPacket> packets = com.rusefi.can.reader.impl.AutoFormatReader.INSTANCE.readFile(args[1]);
        handle(dbc, ".", new java.io.File(args[1]).getName(), packets);
    }

    public static void handle(DbcFile dbc, String reportDestinationFolder, String simpleFileName, List<CANPacket> canPackets) throws IOException {
        String outputFileName = reportDestinationFolder + File.separator + simpleFileName + "_by_source.txt";
        PrintWriter pw = new PrintWriter(new FileOutputStream(outputFileName));

        Map<String, List<DbcPacket>> bySource = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (DbcPacket packet : dbc.values()) {
            String source = packet.getSource();
            if (source == null || source.isEmpty()) {
                source = "Unknown";
            }
            bySource.computeIfAbsent(source, k -> new ArrayList<>()).add(packet);
        }

        Map<Integer, Integer> counts = new HashMap<>();
        for (CANPacket packet : canPackets) {
            counts.put(packet.getId(), counts.getOrDefault(packet.getId(), 0) + 1);
        }

        for (Map.Entry<String, List<DbcPacket>> entry : bySource.entrySet()) {
            pw.println("Source: " + entry.getKey());

            for (DbcPacket packet : entry.getValue()) {
                int count = counts.getOrDefault(packet.getId(), 0);
                if (count != 0)
                    pw.println("  Frame: " + DualSid.dualSid(packet.getId()) + " " + packet.getName() + ": " + count);
            }

            for (DbcPacket packet : entry.getValue()) {
                int count = counts.getOrDefault(packet.getId(), 0);
                if (count == 0)
                    pw.println("  Frame: " + DualSid.dualSid(packet.getId()) + " " + packet.getName() + " NO PACKETS");
            }

            pw.println();
        }

        pw.close();
    }
}
