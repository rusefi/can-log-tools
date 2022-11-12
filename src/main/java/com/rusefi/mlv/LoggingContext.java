package com.rusefi.mlv;

import com.rusefi.can.CANPacket;
import com.rusefi.mlv.LoggingStrategy;
import com.rusefi.sensor_logs.BinaryLogEntry;
import com.rusefi.sensor_logs.BinarySensorLog;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class LoggingContext {
    public Map<String, Double> values = new HashMap<>();
    AtomicReference<Long> time = new AtomicReference<>();

    public BinarySensorLog<BinaryLogEntry> getBinaryLogEntryBinarySensorLog(Collection<BinaryLogEntry> entries, String outputFileName) {
        return new BinarySensorLog<>(o -> {
            Double value = this.values.get(o.getName());
            if (value == null)
                return 0.0;
            return value;
        }, entries, getTimeProvider(), outputFileName);
    }

    public void processPackets(List<CANPacket> packets, BinarySensorLog<BinaryLogEntry> log, LoggingStrategy.PacketLogger logger) {
        for (CANPacket packetContent : packets) {
            this.time.set((long) (packetContent.getTimeStamp() * 1000));
            boolean needLine = logger.takeValues(packetContent);
            if (needLine)
                log.writeSensorLogLine();
        }
        log.close();
    }

    public BinarySensorLog.TimeProvider getTimeProvider() {
        return () -> this.time.get();
    }
}
