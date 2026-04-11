package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import com.rusefi.can.dbc.DbcFile;
import com.rusefi.can.dbc.DbcPacket;

import com.rusefi.can.reader.impl.AutoFormatReader;
import com.rusefi.can.dbc.reader.DbcFileReader;
import com.rusefi.can.util.ToolRepository;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * prints report of packet frequency
 * one way to calculate is: for each packet ID, divide time between first and last occurance, and divide by count of packets of this specific ID
 */
public class PacketFrequency {
    private static final String OUTPUT_FILE_PREFIX = "frequency_";

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: PacketFrequency <dbcFile> <traceFile>");
            ToolRepository.exitWithErrorCodeUnlessToolRegistry();
            return;
        }

        String dbcPath = args[0];
        String tracePath = args[1];

        DbcFile dbc = DbcFileReader.readFromFile(dbcPath);
        List<CANPacket> packets = AutoFormatReader.INSTANCE.readFile(tracePath);

        write(dbc, ".", packets, new File(tracePath).getName());
    }

    public static void write(DbcFile dbc, String reportDestinationFolder, List<CANPacket> logFileContent, String simpleFileName) throws IOException {
        Map<Integer, List<Double>> timestampsBySID = new TreeMap<>();

        for (CANPacket packet : logFileContent) {
            List<Double> timestamps = timestampsBySID.computeIfAbsent(packet.getId(), id -> new ArrayList<>());
            timestamps.add(packet.getTimeStampMs());
        }

        try (FileWriter fw = new FileWriter(new File(reportDestinationFolder, OUTPUT_FILE_PREFIX + simpleFileName + ".txt"))) {
            for (Map.Entry<Integer, List<Double>> entry : timestampsBySID.entrySet()) {
                int sid = entry.getKey();
                List<Double> timestamps = entry.getValue();

                if (timestamps.size() < 2) {
                    continue;
                }

                double first = timestamps.get(0);
                double last = timestamps.get(timestamps.size() - 1);
                double duration = last - first;
                double frequencyMs = duration / (timestamps.size() - 1);

                String name = DbcFile.getPacketName(dbc, sid);

                fw.write(name + " id=" + sid + " frequencyMs=" + String.format("%.2f", frequencyMs) + " count=" + timestamps.size() + "\n");
            }
        }
    }
}
