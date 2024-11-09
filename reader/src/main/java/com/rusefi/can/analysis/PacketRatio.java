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
import java.util.concurrent.atomic.AtomicInteger;

public class PacketRatio {

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
            DbcPacket dbcPacket = dbc == null ? null : dbc.packets.get(sid);
            String key = dbcPacket == null ? Integer.toString(sid) : dbcPacket.getName();
            w.write(key + " " + ratio + " " + countOfThisSid + "\n");
        }
        w.close();
    }
}
