package com.rusefi.can;

import com.rusefi.io.can.CanSender;

import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

public class CanPacketSender {
    public static void sendMessagesOut(List<CANPacket> packets, CanSender sender) {
        if (packets.isEmpty()) {
            throw new IllegalArgumentException("No CAN packets to send");
        }

        int okCounter = 0;
        double firstTimeMs = packets.get(0).getTimeStampMs();
        long startNs = System.nanoTime();

        for (CANPacket packet : packets) {
            long offsetNs = Math.round((packet.getTimeStampMs() - firstTimeMs) * 1_000_000);
            if (offsetNs < 0) {
                throw new IllegalArgumentException("CAN packet timestamps must be ordered");
            }
            long waitNs;
            while ((waitNs = startNs + offsetNs - System.nanoTime()) > 0) {
                LockSupport.parkNanos(waitNs);
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
            }

            boolean wasSendOk = sender.send(packet.getId(), packet.getData());

            if (!wasSendOk) {
                throw new IllegalStateException("CAN send failed after " + okCounter + " packets");
            }
            okCounter++;

            if (okCounter % 1000 == 0 || okCounter == packets.size()) {
                System.out.println(new Date() + ": Total " + okCounter + " OK messages");
            }
        }

        // Leave a small gap before the caller starts the next replay cycle.
        LockSupport.parkNanos(10_000_000);
    }
}
