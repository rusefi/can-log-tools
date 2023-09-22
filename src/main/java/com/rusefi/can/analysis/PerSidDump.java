package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import com.rusefi.can.writer.SteveWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static com.rusefi.can.analysis.ByteRateOfChange.dualSid;

/**
 * Write a separate file for each unique packet ID
 */
public class PerSidDump {
    public static void handle(String reportDestinationFolder, String simpleFileName, List<CANPacket> packets) throws IOException {
        String filteredDestinationFolder = reportDestinationFolder + File.separator + "filtered";
        new File(filteredDestinationFolder).mkdirs();

        TreeSet<Integer> sids = new TreeSet<>();
        // todo: one day I will let streams into my heart
        for (CANPacket packet : packets)
            sids.add(packet.getId());

        // O(n*M) is not so bad
        for (int sid : sids) {

            String outputFileName = filteredDestinationFolder + File.separator + simpleFileName + "_filtered_" + dualSid(sid, "_") + ".txt";
            PrintWriter pw = new PrintWriter(new FileOutputStream(outputFileName));

            List<CANPacket> filteredPackets = new ArrayList<>();

            for (CANPacket packet : packets) {
                if (packet.getId() != sid)
                    continue;

                // no specific reason to use SteveWriter just need any human-readable writer here
                SteveWriter.append(pw, packet);
                filteredPackets.add(packet);
            }
            pw.close();

            int middleIndex = filteredPackets.size() / 2;
            CANPacket middlePacket = filteredPackets.get(middleIndex);

            String middleOutputFileName = filteredDestinationFolder + File.separator + simpleFileName + "_filtered_" + dualSid(sid, "_") + "_middle.txt";
            PrintWriter middle = new PrintWriter(new FileOutputStream(middleOutputFileName));

            middle.println(middlePacket.asLua("payload" + middlePacket.getId())
            + "\n"
                    + middlePacket.getBytesAsString());
            middle.close();
        }
    }
}
