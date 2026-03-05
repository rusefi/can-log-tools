package com.rusefi.can;

import com.rusefi.io.can.CanSender;

import java.util.Date;
import java.util.List;

public class CanPacketSender {
    public static void sendMessagesOut(List<CANPacket> packets, CanSender sender) {
        int okCounter = 0;

        for (CANPacket packet : packets) {
            boolean wasSendOk = sender.send(packet.getId(), packet.getData());

            if (wasSendOk) {
                okCounter++;
            }

            if (okCounter % Math.min(1000, packets.size() - 1) == 0) {
                System.out.println(new Date() + ": Total " + okCounter + " OK messages");
            }
        }
    }
}
