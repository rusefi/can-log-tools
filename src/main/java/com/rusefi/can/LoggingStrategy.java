package com.rusefi.can;

import com.rusefi.can.reader.dbc.DbcField;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.dbc.DbcPacket;
import com.rusefi.sensor_logs.BinaryLogEntry;
import com.rusefi.sensor_logs.BinarySensorLog;

import java.util.ArrayList;
import java.util.List;

public class LoggingStrategy {
    public static final String MLG = ".mlg";
    public static boolean LOG_ONLY_TRANSLATED_FIELDS;

    public static List<BinaryLogEntry> getFieldNameEntries(DbcFile dbc, boolean logOnlyTranslatedFields) {
        List<BinaryLogEntry> entries = new ArrayList<>();
        for (DbcPacket packet : dbc.packets.values()) {
            for (DbcField field : packet.getFields()) {
                if (logOnlyTranslatedFields && !field.isNiceName())
                    continue;
                entries.add(BinaryLogEntry.createFloatLogEntry(field.getName(), field.getCategory()));
            }
        }
        return entries;
    }

    public static void writeLog(DbcFile dbc, List<CANPacket> packets, String outputFileName) {
        List<BinaryLogEntry> entries = dbc.getFieldNameEntries();

        LoggingContext context = new LoggingContext();
        BinarySensorLog<BinaryLogEntry> log = context.getBinaryLogEntryBinarySensorLog(entries, outputFileName);

        PacketLogger logger = packetContent -> {
            DbcPacket packetMeta = dbc.findPacket(packetContent.getId());
            if (packetMeta == null)
                return false;

            for (DbcField field : packetMeta.getFields()) {
                context.values.put(field.getName(), field.getValue(packetContent));
            }
            return true;
        };

        context.processPackets(packets, log, logger);
    }

    interface PacketLogger {
        boolean takeValues(CANPacket packetContent);
    }

}
