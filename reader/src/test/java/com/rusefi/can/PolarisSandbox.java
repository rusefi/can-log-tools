package com.rusefi.can;

import com.rusefi.can.reader.CANLineReader;
import com.rusefi.can.reader.impl.PcanTrcReader2_0;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PolarisSandbox {
    public static void main(String[] args) throws IOException {
        CANLineReader reader = PcanTrcReader2_0.INSTANCE;

        String noEcuFile = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\Polaris\\2021-can\\no-ecu.trc";
        List<CANPacket> noEcuPackets = reader.readFile(noEcuFile);
        Set<Integer> noEcuIds = printStats(noEcuPackets, "NO ECU");

        String idleFile = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\Polaris\\2021-can\\parking-revving-1500-3000.trc";
        List<CANPacket> idling = reader.readFile(idleFile);
        printStats(idling, "IDLING");

        Set<Integer> replayed = new HashSet<>();

        StringBuilder replayLua = new StringBuilder();

        for (CANPacket packet : idling) {
            if (noEcuIds.contains(packet.getId()))
                continue;
            if (replayed.contains(packet.getId()))
                continue;
            replayed.add(packet.getId());

            String arrayName = "payload" + packet.getId();
            replayLua.append(packet.asLua(arrayName));

            replayLua.append(String.format("    txCan(1, 0x%x, 1, " + arrayName + ")\n", packet.getId()));
        }

        System.out.printf(replayLua.toString());


    }

    private static Set<Integer> printStats(List<CANPacket> packets, String msg) {
        Set<Integer> unique = extracted(packets);
        System.out.println("Got " + unique.size() + " " + msg + " unique IDs: " + unique);
        for (Integer noEcuId : unique) {
            System.out.println(String.format(msg + " %x", noEcuId));
        }
        return unique;
    }

    private static Set<Integer> extracted(List<CANPacket> noEcuPackets) {
        System.out.println("Got " + noEcuPackets.size() + " packets");
        Set<Integer> uniqueIds = new HashSet<>();
        for (CANPacket packet : noEcuPackets)
            uniqueIds.add(packet.getId());
        return uniqueIds;
    }
}
