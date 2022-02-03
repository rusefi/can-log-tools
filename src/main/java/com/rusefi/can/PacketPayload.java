package com.rusefi.can;

public class PacketPayload {

    private final double timeStamp;
    private final SensorValue[] values;

    public PacketPayload(double timeStamp, SensorValue... values) {
        this.timeStamp = timeStamp;
        this.values = values;
    }

    public double getTimeStamp() {
        return timeStamp;
    }

    public SensorValue[] getValues() {
        return values;
    }
}
