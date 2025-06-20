package com.rusefi.sensor_logs;

public interface SensorLog {
    void writeSensorLogLine();

    void close();
}
