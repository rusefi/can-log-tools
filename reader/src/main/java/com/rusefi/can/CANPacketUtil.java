package com.rusefi.can;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CANPacketUtil {
    public static Set<Integer> getAllIds(List<CANPacket> canPackets) {
        Set<Integer> SIDs = new HashSet<>();
        for (CANPacket packet : canPackets) {
            SIDs.add(packet.getId());
        }
        return SIDs;
    }
}
