package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import com.rusefi.can.dbc.DbcFile;
import com.rusefi.can.dbc.DbcPacket;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * prints report of packet frequency
 * one way to calculate is: for each packet ID, divide time between first and last occurance, and divide by count of packets of this specific ID
 */
public class PacketFrequency {
    public static void write(DbcFile dbc, String reportDestinationFolder, List<CANPacket> logFileContent, String simpleFileName) throws IOException {
        Map<Integer, List<Double>> timestampsBySID = new TreeMap<>();

        for (CANPacket packet : logFileContent) {
            List<Double> timestamps = timestampsBySID.computeIfAbsent(packet.getId(), id -> new ArrayList<>());
            timestamps.add(packet.getTimeStampMs());
        }

        try (FileWriter fw = new FileWriter(new File(reportDestinationFolder, "frequency_" + simpleFileName + ".txt"))) {
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
