package com.rusefi.mlv;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.dbc.DbcField;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.dbc.DbcPacket;
import com.rusefi.sensor_logs.BinaryLogEntry;
import com.rusefi.sensor_logs.BinarySensorLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class LoggingStrategy {
    public static final String MLG = ".mlg";
    public static boolean LOG_ONLY_TRANSLATED_FIELDS;

    public interface LoggingFilter {
        boolean accept(DbcPacket packet);
    }

    public static List<BinaryLogEntry> getFieldNameEntries(DbcFile dbc, boolean logOnlyTranslatedFields,
                                                           LoggingFilter filter) {
        List<BinaryLogEntry> entries = new ArrayList<>();
        for (DbcPacket packet : dbc.values()) {
            if (!filter.accept(packet))
                continue;
            for (DbcField field : packet.getFields()) {
                if (logOnlyTranslatedFields && !field.isNiceName())
                    continue;
                entries.add(BinaryLogEntry.createFloatLogEntry(field.getName(), field.getCategory()));
            }
        }
        return entries;
    }

    public static void writeLogByDbc(DbcFile dbc, List<CANPacket> packets, String outputFileName) {
        Set<Integer> allIds = CANPacket.getAllIds(packets);
        // we only log DBC frames if at least one packet is present in the trace
        LoggingFilter filter = packet -> allIds.contains(packet.getId());
        List<BinaryLogEntry> entries = dbc.getFieldNameEntries(filter);

        System.out.println(new Date() + " writeLog... " + outputFileName);
        LoggingContext snapshot = new LoggingContext();
        BinarySensorLog<BinaryLogEntry> log = snapshot.getBinaryLogEntryBinarySensorLog(entries, outputFileName);

        PacketLogger logger = packetContent -> {
            DbcPacket packetMeta = dbc.findPacket(packetContent.getId());
            if (packetMeta == null)
                return false;

            for (DbcField field : packetMeta.getFields()) {
                snapshot.put(field.getName(), field.getValue(packetContent));
            }
            return true;
        };

        snapshot.writeLogContent(packets, log, logger);
        System.out.println(new Date() + " writeLog " + outputFileName + " done!");
    }

    public interface PacketLogger {
        boolean takeValues(CANPacket packetContent);
    }

}
