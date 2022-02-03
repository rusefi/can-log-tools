package com.rusefi.can;

import com.rusefi.can.reader.CANoeReader;
import com.rusefi.can.writer.SteveWriter;

import java.io.IOException;
import java.util.List;

public class Util {
    public static void main(String[] args) throws IOException {
        CANoeReader reader = new CANoeReader();

        List<CANPacket> packetList = reader.readFile("Log2.log");

        SteveWriter writer = new SteveWriter("loggerProgram0.log");
        writer.write(packetList);

    }
}
