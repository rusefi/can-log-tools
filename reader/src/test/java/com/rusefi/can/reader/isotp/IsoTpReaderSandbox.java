package com.rusefi.can.reader.isotp;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.impl.AutoFormatReader;
import com.rusefi.io.can.isotp.HexBinary;
import com.rusefi.io.can.isotp.IsoTpCanDecoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IsoTpReaderSandbox {
    public static void main(String[] args) throws IOException {
        String fileName = "C:\\stuff\\1\\a.trc";

        List<CANPacket> packets = AutoFormatReader.INSTANCE.readFile(fileName);
        System.out.println("Got " + packets.size());

        IsoTpCanDecoder decoder = new IsoTpCanDecoder(1) {
            @Override
            protected void onTpFirstFrame() {

            }
        };

        List<Byte> list = new ArrayList<>();

        for (CANPacket p : packets) {

            if (p.getId() == 0x618) {
                byte[] dataNow = decoder.decodePacket(p.getData(), p.getData().length);
                for (byte b : dataNow)
                    list.add(b);

                if (decoder.isComplete()) {
                    System.out.println("Got " + HexBinary.printHexBinary(list));
                    list.clear();
                    decoder.reset();
                }
            }
        }
    }
}
