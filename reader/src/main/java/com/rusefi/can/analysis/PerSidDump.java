package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import com.rusefi.can.DualSid;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.dbc.DbcPacket;
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
    public static void handle(DbcFile dbc, String reportDestinationFolder, String simpleFileName, List<CANPacket> packets) throws IOException {
        Objects.requireNonNull(dbc);

        generateBySourceReport(dbc, reportDestinationFolder, simpleFileName, packets);

        String filteredDestinationFolder = reportDestinationFolder + File.separator + "filtered";
        new File(filteredDestinationFolder).mkdirs();

        Set<Integer> sids = packets.stream()
                .map(CANPacket::getId)
                .collect(Collectors.toCollection(() -> new TreeSet<>()));

        // O(n*M) is not so bad
        for (int sid : sids) {
            DbcPacket packet = dbc.getPacket(sid);
            String suffix = packet == null ? "" : ("_" + packet.getName());

            String outputFileName = filteredDestinationFolder + File.separator + simpleFileName + "_filtered_" + DualSid.dualSid(sid, "_") + suffix + ".txt";
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

            String middleOutputFileName = filteredDestinationFolder + File.separator + simpleFileName + "_filtered_" + DualSid.dualSid(sid, "_") + "_middle.txt";
            PrintWriter middle = new PrintWriter(new FileOutputStream(middleOutputFileName));

            String decAndHex = middlePacket.getId() + "_" + Integer.toHexString(middlePacket.getId());
            String payloadVariableName = "payload" + (packet == null ? decAndHex : packet.getName());
            String variableName = packet == null ? "CAN_" + decAndHex : packet.getName();
            String methodNameSuffix = packet == null ? ("Can" + decAndHex) : packet.getName();
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

            middle.println(getBytesAsString(middlePacket.data));

            String txMethodName = "send" + methodNameSuffix;

            middle.println();
            middle.println();
            middle.println("static void " + txMethodName + "() {");
            middle.println("static uint8_t " + payloadVariableName + "[] = {" + arrayToCode(middlePacket) + "};");
            middle.println("static int " + counterVariable + ";");

            if (packet != null) {
                String extIdSuffix = isExtId ? ", 0, true" : "";
                middle.println("\tCanTxMessage msg(CanCategory::NBC, " + packet.getName() + extIdSuffix + ");");
                middle.println("\tmsg.setArray(" + payloadVariableName + ");");
            }
            middle.println("}");

            middle.close();
        }
    }

    private static void generateBySourceReport(DbcFile dbc, String reportDestinationFolder, String simpleFileName, List<CANPacket> canPackets) throws IOException {
        String outputFileName = reportDestinationFolder + File.separator + simpleFileName + "_by_source.txt";
        PrintWriter pw = new PrintWriter(new FileOutputStream(outputFileName));

        Map<String, List<DbcPacket>> bySource = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (DbcPacket packet : dbc.values()) {
            String source = packet.getSource();
            if (source == null || source.isEmpty()) {
                source = "Unknown";
            }
            bySource.computeIfAbsent(source, k -> new ArrayList<>()).add(packet);
        }

        Map<Integer, Integer> counts = new HashMap<>();
        for (CANPacket packet : canPackets) {
            counts.put(packet.getId(), counts.getOrDefault(packet.getId(), 0) + 1);
        }

        for (Map.Entry<String, List<DbcPacket>> entry : bySource.entrySet()) {
            pw.println("Source: " + entry.getKey());

            for (DbcPacket packet : entry.getValue()) {
                int count = counts.getOrDefault(packet.getId(), 0);
                if (count != 0)
                    pw.println("  Frame: " + DualSid.dualSid(packet.getId(), "_") + " " + packet.getName() + ": " + count);
            }

            for (DbcPacket packet : entry.getValue()) {
                int count = counts.getOrDefault(packet.getId(), 0);
                if (count == 0)
                    pw.println("  Frame: " + DualSid.dualSid(packet.getId(), "_") + " " + packet.getName() + " NO PACKETS");
            }

            pw.println();
        }

        pw.close();
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
