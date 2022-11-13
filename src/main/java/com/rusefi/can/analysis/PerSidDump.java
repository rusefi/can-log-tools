package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import com.rusefi.can.writer.SteveWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.TreeSet;

import static com.rusefi.can.analysis.ByteRateOfChange.dualSid;

public class PerSidDump {
    public static void handle(List<CANPacket> packets, String simpleFileName) throws IOException {
        TreeSet<Integer> sids = new TreeSet<>();
        // todo: one day I will let streams into my heart
        for (CANPacket packet : packets)
            sids.add(packet.getId());

        // O(n*M) is not so bad
        for (int sid : sids) {

            PrintWriter pw = new PrintWriter(new FileOutputStream(simpleFileName + "_filtered_" + dualSid(sid, "_") + ".txt"));

            for (CANPacket packet : packets) {
                if (packet.getId() != sid)
                    continue;

                // no reason to use SteveWriter just need any human-readable writer here
                SteveWriter.append(pw, packet);


            }
            pw.close();
        }

    }
}
