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

/**
 * takes can frames, writes text files of ISO-TP flow
 * @see UDSDecoder
 */
public class IsoTpFileDecoder {

    public static void run(String traceFileName, Set<Integer> isoTpIds, int isoHeaderByteIndex) throws IOException {
        File f = new File(traceFileName);

        try (FileWriter decodedUdsAsText = new FileWriter(f.getParent() + File.separator + "processed_" + f.getName())) {
            process(traceFileName, decodedUdsAsText, isoTpIds, isoHeaderByteIndex);
        }
    }

    private static void process(String traceFileName, FileWriter decodedUdsAsText, Set<Integer> isoTpIds, int isoHeaderByteIndex) throws IOException {
        long previousTimestamp = -1;

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
                decodedUdsAsText.append("BAD " + e);
                return;
            }
            List<Byte> list = bytesById.computeIfAbsent(p.getId(), id -> new ArrayList<>());
            for (byte b : dataNow)
                list.add(b);

            if (decoder.isComplete()) {
                // Collect payload before clearing
                byte[] payload = new byte[list.size()];
                for (int i = 0; i < list.size(); i++)
                    payload[i] = list.get(i);
                // Decode UDS
                if (payload.length > 0) {
                    String durationStr = "";
                    if (previousTimestamp != -1) {
                        long duration = (long) (p.getTimeStampMs() - previousTimestamp);
                        if (duration > 500 && IsoTpFileDecoderFolderStrategy.withTimestamp) {
                            durationStr = " duration " + duration;
                        }
                    }
                    previousTimestamp = (long) p.getTimeStampMs();

                    int sid = payload[0] & 0xFF;
                    String sidAsText = getById(sid);
                    String timestampString = IsoTpFileDecoderFolderStrategy.withTimestamp ? (" at " + p.getTimeStampMs() + "ms") : "";
                    decodedUdsAsText.append("SID " + Integer.toHexString(sid) + sidAsText + timestampString  + durationStr + "\n");
                    udsDecoder.handle(payload);
                }
                //fw.append(Integer.toHexString(p.getId()) + ": Got " + HexBinary.printHexBinary(list) + "\n");
                decodedUdsAsText.append(String.format("%3H [%4d]: %s\n", p.getId(), list.size(), HexBinary.printHexBinary(list)));
                list.clear();
                decoder.reset();
            }
        }
    }

    private static String getById(int sid) {
        if (sid == 0x22)
            return " SID_ReadDataByIdentifier";
        if (sid == 0x09)
            return " Calibration ID";
        if (sid == 0x10)
            return " SID_DiagnosticSessionControl";
        if (sid == 0x11)
            return " SID_ECUReset";
        if (sid == 0x14)
            return " SID_ClearDiagnosticInformation";
        if (sid == 0x27)
            return " SID_SecurityAccess";
        if (sid == 0x28)
            return " SID_CommunicationControl";
        if (sid == 0x2E)
            return " SID_WriteDataByIdentifier";
        if (sid == 0x31)
            return " SID_RoutineControl";
        if (sid == 0x34)
            return " SID_RequestDownload";
        if (sid == 0x35)
            return " SID_RequestUpload";
        if (sid == 0x36)
            return " SID_TransferData";
        if (sid == 0x37)
            return " SID_RequestTransferExit";
        if (sid == 0x3E)
            return " SID_TesterPresent";
        if (sid == 0x7F)
            return" SID_NegativeResponse";
        if (sid == 0x85)
            return" SID_ControlDTCSetting";
        if (sid == 0x86)
            return" SID_ResponseOnEvent";
        if (sid >= 0x40) {
            return " RESP" + getById(sid - 0x40);
        }
        return " unknown";
    }
}
