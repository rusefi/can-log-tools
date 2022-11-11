package com.rusefi.can;

import com.rusefi.can.reader.dbc.DbcField;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.dbc.DbcPacket;
import com.rusefi.sensor_logs.BinaryLogEntry;
import com.rusefi.sensor_logs.BinarySensorLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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

        Map<String, Double> values = new HashMap<>();
        AtomicReference<Long> time = new AtomicReference<>();
        BinarySensorLog<BinaryLogEntry> log = new BinarySensorLog<>(o -> {
            Double value = values.get(o.getName());
            if (value == null)
                return 0.0;
            return value;
        }, entries, time::get, outputFileName);

        for (CANPacket packetContent : packets) {
            DbcPacket packetMeta = dbc.findPacket(packetContent.getId());
            if (packetMeta == null)
                continue;

            time.set((long) (packetContent.getTimeStamp() * 1000));
            for (DbcField field : packetMeta.getFields()) {
                values.put(field.getName(), field.getValue(packetContent));
            }
            log.writeSensorLogLine();
        }
        log.close();
    }
}
