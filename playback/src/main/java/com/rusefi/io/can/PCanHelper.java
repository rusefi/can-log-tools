package com.rusefi.io.can;

import com.rusefi.hex_util.HexBinary;
import org.jetbrains.annotations.NotNull;
import peak.can.basic.*;

import static peak.can.basic.TPCANMessageType.PCAN_MESSAGE_STANDARD;

public class PCanHelper {
    public static final TPCANHandle CHANNEL = getTpcanHandle();
    public static boolean verbose;

    private static @NotNull TPCANHandle getTpcanHandle() {
        int index = Integer.parseInt(System.getProperty("PCAN_INDEX", "0"));
        if (index == 1) {
            System.out.println("Going with 2nd PCAN");
            return TPCANHandle.PCAN_USBBUS2;
        }
        if (index == 2) {
            System.out.println("Going with 3rd PCAN");
            return TPCANHandle.PCAN_USBBUS3;
        }
        return TPCANHandle.PCAN_USBBUS1;
    }

    //    @NotNull
    public static PCANBasic createPCAN() {
        PCANBasic can = new PCANBasic();
        can.initializeAPI();
        return can;
    }

    public static TPCANStatus init(PCANBasic can) {
        return can.Initialize(CHANNEL, TPCANBaudrate.PCAN_BAUD_500K, TPCANType.PCAN_TYPE_NONE, 0, (short) 0);
    }

    public static TPCANStatus send(PCANBasic can, int id, byte[] payLoad) {
        if (verbose) {
            System.out.println(String.format("Sending id=%x %s", id, HexBinary.printByteArray(payLoad)));
        }
        TPCANMsg msg = new TPCANMsg(id, PCAN_MESSAGE_STANDARD.getValue(),
                (byte) payLoad.length, payLoad);
        return can.Write(CHANNEL, msg);
    }

    public static PCANBasic createAndInit() {
        PCANBasic pcan = createPCAN();
        TPCANStatus initStatus = init(pcan);
        if (initStatus != TPCANStatus.PCAN_ERROR_OK) {
            System.out.println("createAndInit: *** ERROR *** TPCANStatus " + initStatus);
            System.exit(-1);
        }
        return pcan;
    }

    public static CanSender createSender() {
        PCANBasic pcan = createAndInit();
        System.out.println("Created " + pcan);
        return createSender(pcan);
    }

    public static @NotNull CanSender createSender(PCANBasic pcan) {
        return (id, payload) -> {
            TPCANStatus status = send(pcan, id, payload);

            if (status == TPCANStatus.PCAN_ERROR_XMTFULL || status == TPCANStatus.PCAN_ERROR_QXMTFULL) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
//                System.out.println(String.format("Let's retry ID=%x", packet.getId()) + " OK=" + okCounter);
                status = send(pcan, id, payload);
            }

            boolean isHappy = status == TPCANStatus.PCAN_ERROR_OK;
            if (!isHappy) {
                System.out.println("Error sending " + status);
            }
            return isHappy;
        };
    }
}
