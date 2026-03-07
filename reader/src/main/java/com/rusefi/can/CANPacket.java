package com.rusefi.can;

public class CANPacket {
    private final double timeStampMs;
    private final int id;
    private final byte[] data;

    public CANPacket(double timeStampMs, int id, byte[] data) {
        this.timeStampMs = timeStampMs;
        this.id = id;
        this.data = data;
    }

    public double getTimeStampMs() {
        return timeStampMs;
    }

    public int getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }

    public int getUnsigned(int i) {
        return Byte.toUnsignedInt(data[i]);
    }

    @Override
    public String toString() {
        return "CANPacket{" +
                "id=" + id +
                '}';
    }
}
