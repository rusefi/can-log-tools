package com.rusefi.can.writer;

import com.rusefi.can.CANPacket;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class SteveWriter implements CANTextWriter {
    private final String fileName;

    public SteveWriter(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void write(List<CANPacket> packetList) throws IOException {
        try (FileWriter fw = new FileWriter(fileName)) {
            fw.write("#logger.arm Compiled last on: 30 Aug 1:51PM by Steve.\n" +
                    "#Starting RPI logging Unit.\n" +
                    "#Started canhandler on can0\n" +
                    "#Logger MAC Address: b8:27:eb:2f:1f:4c\n" +
                    "Starting thread: 0\n" +
                    "#Setup complete: 90.2269\n" +
                    "#Format: Delta Time: can_ID (hex) [can_DLC] CAN_data (hex)\n");
            StringBuilder sb = new StringBuilder();
            for (CANPacket packet : packetList)
                append(sb, packet);
            fw.write(sb.toString());
        }
    }


    public static void append(Appendable sb, CANPacket p) throws IOException {
        sb.append(p.getTimeStamp() + ":\t");
        sb.append(printHex3(p.getId()).toLowerCase() + "\t");
        byte[] data = p.getData();
        sb.append(Integer.toString(data.length));
        for (int i = 0; i < data.length; i++) {
            sb.append("\t" + printHexByte(data[i]).toLowerCase());
        }
        sb.append("\n");
    }

    private static String printHexByte(byte data) {
        return String.format("%02X", data);
    }

    private static String printHex3(int value) {
        return String.format("%1$03X", value);
    }
}
