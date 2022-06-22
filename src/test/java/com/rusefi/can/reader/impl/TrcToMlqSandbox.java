package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.dbc.DbcField;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.dbc.DbcPacket;
import com.rusefi.sensor_logs.BinaryLogEntry;
import com.rusefi.sensor_logs.BinarySensorLog;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.rusefi.can.reader.impl.ParseDBC.VAG_MOTOR_1;

public class TrcToMlqSandbox {

    //    private static final String fileName = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\VAG\\2006-Passat-B6\\passat-b6-stock-ecu-ecu-ptcan-not-running-pedal-up-and-down.trc";
    private static final String fileName = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\VAG\\2006-Passat-B6\\passat-b6-stock-ecu-ecu-ptcan-parked-revving.trc";

    public static void main(String[] args) throws IOException {
        DbcFile dbc = new DbcFile();
        {
            BufferedReader reader = new BufferedReader(new StringReader(VAG_MOTOR_1));
            dbc.read(reader);
        }

        List<BinaryLogEntry> entries = new ArrayList<>();
        for (DbcPacket packet : dbc.packets) {
            for (DbcField field : packet.getFields()) {
                entries.add(new BinaryLogEntry() {
                    @Override
                    public String getName() {
                        return field.getName();
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

        PcanTrcReader reader = new PcanTrcReader();
        List<CANPacket> packets = reader.readFile(fileName);
        System.out.println(packets.size() + " packets");


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
