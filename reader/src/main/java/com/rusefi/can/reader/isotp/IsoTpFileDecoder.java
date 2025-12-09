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

    public static void run(String traceFileName, Set<Integer> isoTpIds, int isoHeaderByteIndex) throws IOException {
        File f = new File(traceFileName);

        try (FileWriter decodedUdsAsText = new FileWriter(f.getParent() + File.separator + "processed_" + f.getName())) {
            process(traceFileName, decodedUdsAsText, isoTpIds, isoHeaderByteIndex);
        }
    }

    private static void process(String traceFileName, FileWriter fw, Set<Integer> isoTpIds, int isoHeaderByteIndex) throws IOException {


        List<CANPacket> packets = AutoFormatReader.INSTANCE.readFile(traceFileName);
        System.out.println("Got " + packets.size() + " packets from " + traceFileName);

        Map<Integer, IsoTpCanDecoder> decoderById = new HashMap<>();


        Map<Integer, List<Byte>> bytesById = new HashMap<>();

        File inputFile = new File(traceFileName);
        File outputDir = new File(inputFile.getParentFile() + File.separator + "from_" + inputFile.getName());
        outputDir.mkdirs();
        UDSDecoder udsDecoder = new UDSDecoder(outputDir);

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

            byte[] dataNow;
            try {
                dataNow = decoder.decodePacket(p.getData(), p.getData().length);
            } catch (IllegalStateException e) {
                fw.append("BAD " + e);
                return;
            }
            List<Byte> list = bytesById.computeIfAbsent(p.getId(), id -> new ArrayList<>());
            for (byte b : dataNow)
                list.add(b);

            if (decoder.isComplete()) {
                // Collect payload before clearing
                byte[] payload = new byte[list.size()];
                for (int i = 0; i < list.size(); i++) payload[i] = list.get(i);
                // Decode UDS
                if (payload.length > 0) {
                    udsDecoder.handle(payload);
                }
                //fw.append(Integer.toHexString(p.getId()) + ": Got " + HexBinary.printHexBinary(list) + "\n");
                fw.append(String.format("%3H [%4d]: %s\n", p.getId(), list.size(), HexBinary.printHexBinary(list)));
                list.clear();
                decoder.reset();
            }
        }
    }
}
