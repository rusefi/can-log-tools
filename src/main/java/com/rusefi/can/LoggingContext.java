package com.rusefi.can;

import com.rusefi.sensor_logs.BinaryLogEntry;
import com.rusefi.sensor_logs.BinarySensorLog;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

class LoggingContext {
    Map<String, Double> values = new HashMap<>();
    AtomicReference<Long> time = new AtomicReference<>();

    BinarySensorLog<BinaryLogEntry> getBinaryLogEntryBinarySensorLog(Collection<BinaryLogEntry> entries, String outputFileName) {
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
