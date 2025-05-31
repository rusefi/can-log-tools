package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.dbc.DbcPacket;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FirstPacket {
    public static void write(DbcFile dbc, String reportDestinationFolder, List<CANPacket> logFileContent, String simpleFileName) throws IOException {
        if (logFileContent.isEmpty())
            return;
        CANPacket firstPacket = logFileContent.get(0);

        Map<Integer, CANPacket> firstPacketById = new TreeMap<>();
        Map<Double, CANPacket> sorterByFirstPacket = new TreeMap<>();

        for (CANPacket packet : logFileContent) {
            if (!firstPacketById.containsKey(packet.getId())) {
                firstPacketById.put(packet.getId(), packet);
                sorterByFirstPacket.put(packet.getTimeStamp() - firstPacket.getTimeStamp(), packet);
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
        DbcPacket dbcPacket = dbc.getPacket(sid);
        String key = dbcPacket == null ? Integer.toString(sid) : dbcPacket.getName();
        w.write(key + ": " + (packet.getTimeStamp() - firstPacket.getTimeStamp()) + "\n");
    }
}
