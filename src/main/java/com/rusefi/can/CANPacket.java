package com.rusefi.can;

import java.util.Arrays;

import static com.rusefi.can.Utils.bytesToHex;
import static com.rusefi.can.Utils.bytesToHexWithSpaces;

public class CANPacket {
    private final double timeStamp;
    private final int id;
    private final byte[] data;

    public CANPacket(double timeStamp, int id, byte[] data) {
        this.timeStamp = timeStamp;
        this.id = id;
        this.data = data;
    }

    public int getTwoBytes(int index) {
        return getUnsigned(index + 1) * 256 + getUnsigned(index);
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

    public int getUnsigned(int i) {
        return Byte.toUnsignedInt(data[i]);
    }

    public void assertThat(String msg, PackerAssertion assertion) {
        if (!assertion.test(this))
            throw new IllegalStateException("Not " + msg + " " + bytesToHexWithSpaces(data));
    }

    public int getUnsignedInt(int index) {
        return Byte.toUnsignedInt(data[index]);
    }

    public interface PackerAssertion {
        boolean test(CANPacket packet);
    }
}
