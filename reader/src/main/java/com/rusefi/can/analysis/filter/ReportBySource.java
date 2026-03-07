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

public class ReportBySource {
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
                    pw.println("  Frame: " + DualSid.dualSid(packet.getId(), "_") + " " + packet.getName() + ": " + count);
            }

            for (DbcPacket packet : entry.getValue()) {
                int count = counts.getOrDefault(packet.getId(), 0);
                if (count == 0)
                    pw.println("  Frame: " + DualSid.dualSid(packet.getId(), "_") + " " + packet.getName() + " NO PACKETS");
            }

            pw.println();
        }

        pw.close();
    }
}
