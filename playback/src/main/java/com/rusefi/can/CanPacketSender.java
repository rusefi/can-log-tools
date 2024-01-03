package com.rusefi.can;

import com.rusefi.io.can.PCanHelper;
import peak.can.basic.PCANBasic;
import peak.can.basic.TPCANStatus;

import java.util.Date;
import java.util.List;

public class CanPacketSender {
    public static void sendMessagesOut(List<CANPacket> logFileContent, PCANBasic pcan) throws InterruptedException {
        int okCounter = 0;

        for (CANPacket packet : logFileContent) {
            TPCANStatus status = PCanHelper.send(pcan, packet.getId(), packet.getData());
            if (status == TPCANStatus.PCAN_ERROR_XMTFULL || status == TPCANStatus.PCAN_ERROR_QXMTFULL) {
                Thread.sleep(10);
//                System.out.println(String.format("Let's retry ID=%x", packet.getId()) + " OK=" + okCounter);
                status = PCanHelper.send(pcan, packet.getId(), packet.getData());
            }

            if (status == TPCANStatus.PCAN_ERROR_OK) {
                okCounter++;
            } else {
                System.out.println("Error sending " + status);
            }

            if (okCounter % 1000 == 0) {
                System.out.println(new Date() + ": Total " + okCounter + " OK messages");
            }

        }
    }
}
