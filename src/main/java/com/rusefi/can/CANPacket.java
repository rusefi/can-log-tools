package com.rusefi.can;

public class CANPacket {
    private final double timeStamp;
    private final int id;
    private final byte[] data;

    public CANPacket(double timeStamp, int id, byte[] data) {
        this.timeStamp = timeStamp;
        this.id = id;
        this.data = data;
    }

    public double getTimeStamp() {
        return timeStamp;
    }

    public int getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }
}
