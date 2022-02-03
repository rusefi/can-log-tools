package com.rusefi.can;

public class SensorValue {
    private final SensorType type;
    private final double value;

    public SensorValue(SensorType type, double value) {
        this.type = type;
        this.value = value;
    }

    public SensorType getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "{" +
                type +
                ", value=" + value +
                '}';
    }
}
