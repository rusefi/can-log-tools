package com.rusefi.io.can;


import peak.can.basic.*;

import static peak.can.basic.TPCANMessageType.PCAN_MESSAGE_STANDARD;

public class PCanHelper {
    public static final TPCANHandle CHANNEL = TPCANHandle.PCAN_USBBUS1;

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
        //log.info(String.format("Sending id=%x %s", id, HexBinary.printByteArray(payLoad)));
        TPCANMsg msg = new TPCANMsg(id, PCAN_MESSAGE_STANDARD.getValue(),
                (byte) payLoad.length, payLoad);
        return can.Write(CHANNEL, msg);
    }

    public static PCANBasic createAndInit() {
        PCANBasic pcan = createPCAN();
        TPCANStatus initStatus = init(pcan);
        if (initStatus != TPCANStatus.PCAN_ERROR_OK) {
            System.out.println("TPCANStatus " + initStatus);
            System.exit(-1);
        }
        return pcan;
    }

    public static CanSender create() {
        PCANBasic pcan = createAndInit();
        return new CanSender() {
            @Override
            public void send(int id, byte[] payload) {
                PCanHelper.send(pcan, id, payload);
            }
        };
    }
}
