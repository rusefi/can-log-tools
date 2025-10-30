package com.rusefi.can.reader.isotp;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.impl.AutoFormatReader;
import com.rusefi.io.can.isotp.HexBinary;
import com.rusefi.io.can.isotp.IsoTpCanDecoder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class IsoTpFileDecoder {

    public static void run(String fileName, Set<Integer> isoTpIds, int isoHeaderByteIndex) throws IOException {
        File f = new File(fileName);

        try (FileWriter fw = new FileWriter(f.getParent() + File.separator + "processed_" + f.getName())) {
            process(fileName, fw, isoTpIds, isoHeaderByteIndex);
        }
    }

    private static void process(String fileName, FileWriter fw, Set<Integer> isoTpIds, int isoHeaderByteIndex) throws IOException {


        List<CANPacket> packets = AutoFormatReader.INSTANCE.readFile(fileName);
        System.out.println("Got " + packets.size() + " packets from " + fileName);

        Map<Integer, IsoTpCanDecoder> decoderById = new HashMap<>();


        List<Byte> list = new ArrayList<>();

        for (CANPacket p : packets) {
            if (!isoTpIds.contains(p.getId()))
                continue;

            IsoTpCanDecoder decoder = decoderById.computeIfAbsent(p.getId(), new Function<Integer, IsoTpCanDecoder>() {
                @Override
                public IsoTpCanDecoder apply(Integer integer) {
                    return new IsoTpCanDecoder(isoHeaderByteIndex) {
                        @Override
                        protected void onTpFirstFrame() {

                        }

                        @Override
                        protected byte[] handleFlowControl(int flowStatus, int blockSize, int separationTime) {
                            return new byte[0];
                        }
                    };
                }
            });

            byte[] dataNow = decoder.decodePacket(p.getData(), p.getData().length);
            for (byte b : dataNow)
                list.add(b);

            if (decoder.isComplete()) {
                //fw.append(Integer.toHexString(p.getId()) + ": Got " + HexBinary.printHexBinary(list) + "\n");
                fw.append(String.format("%3H [%4d]: %s\n", p.getId(), list.size(), HexBinary.printHexBinary(list)));
                list.clear();
                decoder.reset();
            }
        }
    }
}
