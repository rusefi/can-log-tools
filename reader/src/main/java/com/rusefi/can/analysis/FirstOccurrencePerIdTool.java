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

/**
 * Reports the first (earliest) occurrence of each packet ID in a CAN trace.
 */
public class FirstOccurrencePerIdTool {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: FirstPacket <dbcFile> <traceFile>");
            com.rusefi.can.util.ToolRepository.exitWithErrorCodeUnlessToolRegistry();
            return;
        }
        DbcFile dbc = com.rusefi.can.dbc.reader.DbcFileReader.readFromFile(args[0]);
        List<CANPacket> packets = com.rusefi.can.reader.impl.AutoFormatReader.INSTANCE.readFile(args[1]);
        write(dbc, ".", packets, new java.io.File(args[1]).getName());
    }

    public static void write(DbcFile dbc, String reportDestinationFolder, List<CANPacket> logFileContent, String simpleFileName) throws IOException {
        if (logFileContent.isEmpty())
            return;
        CANPacket firstPacket = logFileContent.get(0);

        Map<Integer, CANPacket> firstPacketById = new TreeMap<>();
        Map<Double, CANPacket> sorterByFirstPacket = new TreeMap<>();

        for (CANPacket packet : logFileContent) {
            if (!firstPacketById.containsKey(packet.getId())) {
                firstPacketById.put(packet.getId(), packet);
                sorterByFirstPacket.put(packet.getTimeStampMs() - firstPacket.getTimeStampMs(), packet);
            }
        }


        Writer w = new FileWriter(reportDestinationFolder + File.separator + "start_" + simpleFileName + ".txt");

        for (CANPacket packet : firstPacketById.values()) {
            writeLine(dbc, packet, w, firstPacket);
        }

        w.write("***************************************************\n");

        for (CANPacket packet : sorterByFirstPacket.values()) {
            writeLine(dbc, packet, w, firstPacket);
        }

        w.close();
    }

    private static void writeLine(DbcFile dbc, CANPacket packet, Writer w, CANPacket firstPacket) throws IOException {
        int sid = packet.getId();
        String key = DbcFile.getPacketName(dbc, sid);
        w.write(key + ": " + (packet.getTimeStampMs() - firstPacket.getTimeStampMs()) + "\n");
    }
}
