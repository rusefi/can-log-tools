package com.rusefi.can;

import com.rusefi.can.reader.dbc.DbcField;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.dbc.DbcPacket;
import com.rusefi.sensor_logs.BinaryLogEntry;
import com.rusefi.sensor_logs.BinarySensorLog;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class LoggingStrategy {
    public static List<BinaryLogEntry> getFieldNameEntries(DbcFile dbc) {
        List<BinaryLogEntry> entries = new ArrayList<>();
        for (DbcPacket packet : dbc.packets.values()) {
            for (DbcField field : packet.getFields()) {
                entries.add(new BinaryLogEntry() {
                    @Override
                    public String getName() {
                        return field.getName();
                    }

                    @Override
                    public String getCategory() {
                        return field.getCategory();
                    }

                    @Override
                    public String getUnit() {
                        return "x";
                    }

                    @Override
                    public int getByteSize() {
                        return 4;
                    }

                    @Override
                    public void writeToLog(DataOutputStream dos, double value) throws IOException {
                        dos.writeFloat((float) value);
                    }

                    @Override
                    public String toString() {
                        return getName();
                    }
                });
            }
        }
        return entries;
    }

    public static void writeLog(DbcFile dbc, List<BinaryLogEntry> entries, List<CANPacket> packets) {
        Map<String, Double> values = new HashMap<>();

        AtomicReference<Long> time = new AtomicReference<>();
        BinarySensorLog<BinaryLogEntry> log = new BinarySensorLog<>(o -> {
            Double value = values.get(o.getName());
            if (value == null)
                return 0.0;
            return value;
        }, entries, time::get);


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
