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
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Write a separate file for each unique packet ID
 */
public class PerSidDump {
    public static void handle(DbcFile dbc, String reportDestinationFolder, String simpleFileName, List<CANPacket> packets) throws IOException {
        String filteredDestinationFolder = reportDestinationFolder + File.separator + "filtered";
        new File(filteredDestinationFolder).mkdirs();

        TreeSet<Integer> sids = new TreeSet<>();
        // todo: one day I will let streams into my heart
        for (CANPacket packet : packets)
            sids.add(packet.getId());

        // O(n*M) is not so bad
        for (int sid : sids) {
            DbcPacket packet = dbc == null ? null : dbc.packets.get(sid);
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

            StringBuilder payloadLine = middlePacket.asLua(payloadVariableName);

            middle.println(variableName + " = " + middlePacket.getId());
            middle.println(payloadLine);

            String counterVariable = "counter" + decAndHex;

            middle.println();
            middle.println(counterVariable + " = 0");
            middle.println("function " + rxMethodName + "(bus, id, dlc, data)");
            middle.println("\t" + counterVariable + " = (" + counterVariable + " + 1) % 256");
            middle.println("\t" + payloadVariableName + "[x] = " + counterVariable);
            //middle.println("\tprint ('MOTOR_" + middlePacket.getId() + "' " ..)

            middle.println("\ttxCan(VEHICLE_BUS, " + variableName + ", 0, " + payloadVariableName + ")");

            middle.println("end");
            middle.println();

            middle.println("canRxAdd(ECU_BUS, " + variableName + ", " + rxMethodName + ")");
            middle.println("canRxAdd(ECU_BUS, " + variableName + ", " + "drop" + ")");
            middle.println("canRxAdd(ECU_BUS, " + variableName + ", " + "drop" + ")");

            middle.println();

            middle.println(middlePacket.getBytesAsString());

            String txMethodName = "send" + methodNameSuffix;

            middle.println();
            middle.println();
            middle.println("static void " + txMethodName + "() {");
            middle.println("static uint8_t " + payloadVariableName + "[] = {" + middlePacket.arrayToCode() + "};");
            middle.println("static int " + counterVariable + ";");

            if (packet != null) {
                middle.println("\tCanTxMessage msg(CanCategory::NBC, " + packet.getName() + ");");
                middle.println("\tmsg.setArray(" + payloadVariableName + ", " + middlePacket.getData().length + ");");


            }
            middle.println("}");

            middle.close();
        }
    }
}
