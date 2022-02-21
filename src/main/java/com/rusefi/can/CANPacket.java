package com.rusefi.can;

import com.rusefi.can.decoders.AbstractPacketDecoder;

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

    /**
     * @param index, starting from zero
     */
    public int getTwoBytesByByteIndex(int index) {
        return getByBitIndex(index * 8, 16);
    }

    public int getByBitIndex(int bitIndex, int bitWidth) {
        int byteIndex = bitIndex / 8;
        int shift = bitIndex - byteIndex * 8;
        int value = getUnsigned(byteIndex);
        if (shift + bitIndex > 8) {
            value = value + getUnsigned(byteIndex + 1) * 256;
        }
        return value >> shift & AbstractPacketDecoder.mask(bitWidth);
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
