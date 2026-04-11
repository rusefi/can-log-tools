package com.rusefi.can.analysis.filter;

import com.rusefi.can.CANPacket;
import com.rusefi.can.DualSid;
import com.rusefi.can.dbc.DbcFile;
import com.rusefi.can.dbc.reader.DbcFileReader;
import com.rusefi.can.reader.impl.AutoFormatReader;
import com.rusefi.can.util.ToolRepository;
import com.rusefi.can.writer.SteveWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Write a separate file for each unique packet ID
 */
public class PerSidDump {
    private static final String OUTPUT_SUBFOLDER_NAME = "filtered";

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: PerSidDump <dbcFile> <traceFile>");
            ToolRepository.exitWithErrorCodeUnlessToolRegistry();
            return;
        }

        String dbcPath = args[0];
        String tracePath = args[1];

        DbcFile dbc = DbcFileReader.readFromFile(dbcPath);
        List<CANPacket> packets = AutoFormatReader.INSTANCE.readFile(tracePath);

        handle(dbc, ".", new File(tracePath).getName(), packets);
    }

    public static void handle(DbcFile dbc, String reportDestinationFolder, String simpleFileName, List<CANPacket> packets) throws IOException {
        Objects.requireNonNull(dbc);

        String filteredDestinationFolder = reportDestinationFolder + File.separator + OUTPUT_SUBFOLDER_NAME;
        new File(filteredDestinationFolder).mkdirs();

        Set<Integer> sids = packets.stream()
                .map(CANPacket::getId)
                .collect(Collectors.toCollection(() -> new TreeSet<>()));

        // O(n*M) is not so bad
        for (int sid : sids) {
            String suffix = "_" + DbcFile.getPacketName(dbc, sid);

            String outputFileName = filteredDestinationFolder + File.separator + simpleFileName + "_filtered_" + DualSid.dualSid(sid) + suffix + ".txt";
            PrintWriter pw = new PrintWriter(new FileOutputStream(outputFileName));

            List<CANPacket> filteredPackets = new ArrayList<>();

            for (CANPacket canPacket : packets) {
                if (canPacket.getId() != sid)
                    continue;

                // no specific reason to use SteveWriter just need any human-readable writer here
                SteveWriter.append(pw, canPacket);
                filteredPackets.add(canPacket);
            }
            pw.close();

            int middleIndex = filteredPackets.size() / 2;
            CANPacket middlePacket = filteredPackets.get(middleIndex);

            String middleOutputFileName = filteredDestinationFolder + File.separator + simpleFileName + "_filtered_" + DualSid.dualSid(sid) + "_middle.txt";
            PrintWriter middle = new PrintWriter(new FileOutputStream(middleOutputFileName));

            String decAndHex = middlePacket.getId() + "_" + Integer.toHexString(middlePacket.getId());
            String packetName = DbcFile.getPacketName(dbc, sid);
            String payloadVariableName = "payload" + packetName;
            String variableName = packetName;
            String methodNameSuffix = packetName;
            String rxMethodName = "on" + methodNameSuffix;
            boolean isExtId = sid > 0x7ff;

            StringBuilder payloadLine = asLua(middlePacket, payloadVariableName);

            middle.println(variableName + " = " + middlePacket.getId());
            middle.println(payloadLine);

            String counterVariable = "counter" + decAndHex;

            middle.println();
            middle.println(counterVariable + " = 0");
            middle.println("function send" + methodNameSuffix + "()");
            middle.println("function " + rxMethodName + "(bus, id, dlc, data)");
            middle.println("\t" + counterVariable + " = (" + counterVariable + " + 1) % 256");
            middle.println("\t" + payloadVariableName + "[x] = " + counterVariable);
            //middle.println("\tprint ('MOTOR_" + middlePacket.getId() + "' " ..)

            middle.println("\ttxCan(VEHICLE_BUS, " + variableName + ", " + (isExtId ? "1" : "0") + ", " +
                    payloadVariableName + ")");

            middle.println("end");
            middle.println();

            middle.println("canRxAdd(ECU_BUS, " + variableName + ", " + rxMethodName + ")");
            middle.println("canRxAdd(ECU_BUS, " + variableName + ", " + "drop" + ")");
            middle.println("canRxAdd(ECU_BUS, " + variableName + ", " + "drop" + ")");

            middle.println();

            middle.println(getBytesAsString(middlePacket.getData()));

            String txMethodName = "send" + methodNameSuffix;

            middle.println();
            middle.println();
            middle.println("static void " + txMethodName + "() {");
            middle.println("static uint8_t " + payloadVariableName + "[] = {" + arrayToCode(middlePacket) + "};");
            middle.println("static int " + counterVariable + ";");

            if (packetName != null) {
                String extIdSuffix = isExtId ? ", 0, true" : "";
                middle.println("\tCanTxMessage msg(CanCategory::NBC, " + packetName + extIdSuffix + ");");
                middle.println("\tmsg.setArray(" + payloadVariableName + ");");
            }
            middle.println("}");

            middle.close();
        }
    }

    public static StringBuilder asLua(CANPacket canPacket, String arrayName) {
        StringBuilder result = new StringBuilder();
        result.append(arrayName + " = {");

        result.append(arrayToCode(canPacket));
        result.append("}\n");
        return result;
    }

    public static StringBuilder arrayToCode(CANPacket canPacket) {
        StringBuilder result = new StringBuilder();
        byte[] data = canPacket.getData();
//        System.out.println(String.format("Got ECU 0x%x", getId()) + " " + data.length);

        for (int index = 0; index < data.length; index++) {
            if (index > 0)
                result.append(", ");

            result.append(String.format("0x%02x", data[index]));

        }
        return result;
    }

    public static CharSequence getBytesAsString(byte[] data) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < data.length; index++) {
            if (index > 0)
                result.append(" ");

            result.append(String.format("0x%02x", data[index]));

        }
        return result;
    }
}
