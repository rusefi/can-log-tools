package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import com.rusefi.can.DualSid;
import com.rusefi.can.Launcher;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.mlv.LoggingContext;
import com.rusefi.mlv.LoggingStrategy;
import com.rusefi.sensor_logs.BinaryLogEntry;
import com.rusefi.sensor_logs.BinarySensorLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CanToMegaLogViewer {
    public static void createMegaLogViewer(String reportDestinationFolder, List<CANPacket> canPackets, String simpleFileName) throws IOException {

        if (Launcher.dbcFileName != null) {
            DbcFile dbc = DbcFile.readFromFile(Launcher.dbcFileName);
            String outputFileName = reportDestinationFolder + File.separator + simpleFileName + ".by_dbc.mlg";
            LoggingStrategy.writeLog(dbc, canPackets, outputFileName);
        }

        writeByIds(reportDestinationFolder, canPackets, simpleFileName);
    }

    private static void writeByIds(String reportDestinationFolder, List<CANPacket> canPackets, String simpleFileName) {
        List<BinaryLogEntry> entries = new ArrayList<>();
        Set<ByteRateOfChange.ByteId> byteIds = new HashSet<>();

        Set<Integer> SIDs = new HashSet<>();
        for (CANPacket packet : canPackets) {
            SIDs.add(packet.getId());

            for (int byteIndex = 0; byteIndex < packet.getData().length; byteIndex++) {
                ByteRateOfChange.ByteId key = ByteRateOfChange.ByteId.createByte(packet.getId(), byteIndex);
                byteIds.add(key);
            }

        }

        for (ByteRateOfChange.ByteId key : byteIds) {
            entries.add(BinaryLogEntry.createFloatLogEntry(key.getLogKey(), Integer.toBinaryString(key.sid)));
        }

        for (Integer sid : SIDs) {
            for (int i = 0; i < 7; i++) {
                {
                    String twoBytesKey = getTwoBytesKeyM(sid, i);
                    entries.add(BinaryLogEntry.createFloatLogEntry(twoBytesKey, Integer.toBinaryString(sid)));
                }
                {
                    String twoBytesKey = getTwoBytesKeyL(sid, i);
                    entries.add(BinaryLogEntry.createFloatLogEntry(twoBytesKey, Integer.toBinaryString(sid)));
                }
            }
        }

        LoggingContext context = new LoggingContext();
        BinarySensorLog<BinaryLogEntry> log = context.getBinaryLogEntryBinarySensorLog(entries, reportDestinationFolder + File.separator + simpleFileName + LoggingStrategy.MLG);

        context.writeLogContent(canPackets, log, packetContent -> {
            byte[] bytes = packetContent.getData();
            for (int i = 0; i < bytes.length; i++) {
                int value = bytes[i] & 0xFF;

                int sid = packetContent.getId();
                {
                    String name = ByteRateOfChange.ByteId.createByte(sid, i).getLogKey();
                    context.currentSnapshot.put(name, (double) value);
                }
                {
                    if (i < bytes.length - 1) {
                        int value2 = bytes[i + 1] & 0xFF;
                        {
                            String name = getTwoBytesKeyM(sid, i);
                            context.currentSnapshot.put(name, (double) value2 * 256 + value);
                        }
                        {
                            String name = getTwoBytesKeyL(sid, i);
                            context.currentSnapshot.put(name, (double) value2 + value * 256);
                        }
                    }
                }
            }
            return true;
        });
    }


    private static String getTwoBytesKeyM(Integer sid, int i) {
        return DualSid.dualSid(sid) + "__" + i + "_" + (i + 1);
    }

    private static String getTwoBytesKeyL(Integer sid, int i) {
        return DualSid.dualSid(sid) + "__" + (i + 1) + "_" + i;
    }
}
