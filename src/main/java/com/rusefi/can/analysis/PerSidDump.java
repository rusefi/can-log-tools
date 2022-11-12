package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;

import java.util.List;
import java.util.TreeSet;

public class PerSidDump {
    public static void handle(List<CANPacket> packets, String simpleFileName) {
        TreeSet<Integer> sids = new TreeSet<>();
        // todo: one day I will let streams into my heart
        for (CANPacket packet : packets)
            sids.add(packet.getId());

        // O(n*M) is not so bad
        for (int sid : sids) {
            for (CANPacket packet : packets) {
                if (packet.getId() != sid)
                    continue;
            }
        }

    }
}
