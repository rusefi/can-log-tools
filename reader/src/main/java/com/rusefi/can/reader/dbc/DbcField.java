package com.rusefi.can.reader.dbc;

import com.rusefi.can.CANPacket;
import com.rusefi.can.dbc.FileNameProvider;

/**
 * also known as 'signal'
 */
public class DbcField implements Comparable<DbcField> {
    private final int packetId;
    private String name;
    private final int startOffset;
    private final int length;
    private final double mult;
    private final double offset;
    private final String category;
    private final boolean isBigEndian;
    private boolean isNiceName;
    private final boolean isSigned;

    private FileNameProvider parentPacket;

    public void setParentPacket(FileNameProvider parentPacket) {
        this.parentPacket = parentPacket;
    }

    public FileNameProvider getParentPacket() {
        return parentPacket;
    }

    public DbcField(int packetId, String name, int startOffset, int length, double mult, double offset, String category, boolean isBigEndian, boolean isSigned) {
        this.packetId = packetId;
        this.name = name;
        this.startOffset = crazyMotorolaMath(startOffset, length, isBigEndian);
        this.length = length;
        this.mult = mult;
        this.offset = offset;
        this.category = category;
        this.isBigEndian = isBigEndian;
        this.isSigned = isSigned;
        if (mult == 0 && offset == 0)
            throw new IllegalArgumentException("Really? multiplier and offset both zero for " + name);
    }

    public static int crazyMotorolaMath(int b, int length, boolean isBigEndian) {
        if (!isBigEndian)
            return b;

        // https://github.com/ebroecker/canmatrix/wiki/signal-Byteorder
        // convert from lsb0 bit numbering to msb0 bit numbering (or msb0 to lsb0)
        b = b - (b % 8) + 7 - (b % 8);
        // convert from lsbit of signal data to msbit of signal data, when bit numbering is msb0
        b = b + length - 1;
        // convert from msbit of signal data to lsbit of signal data, when bit numbering is msb0
        b = b - (b % 8) + 7 - (b % 8);
        return b;
    }

    public String getCategory() {
        return category;
    }

    public boolean isNiceName() {
        return isNiceName;
    }

    public String getName() {
        return name;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getLength() {
        return length;
    }

    public double getMult() {
        return mult;
    }

    public double getOffset() {
        return offset;
    }

    public boolean isBigEndian() {
        return isBigEndian;
    }

    public boolean isSigned() { return isSigned; }

    @Override
    public String toString() {
        return name;
    }

    /**
     * There is no way to explain it, only experience it
     * @see ParseDBCWithCommentTest#parseMoto
     * @see ParseDbcFieldTest#testHumanStartIndex
     */
    public int getHumanStartIndex() {
        return getHumanStartIndex(startOffset, length, isBigEndian);
    }

    public static int getHumanStartIndex(int bitIndex, int bitWidth, boolean isBigEndian) {
        if (isBigEndian) {
            int byteIndex = getByteIndex(bitIndex);
            int shift = getShift(byteIndex, bitIndex);
            int delta = shift + bitWidth - 8;
            if (delta > 0) {
                return bitIndex - delta;
            }
        }
        return bitIndex;
    }

    public static int getBitRange(byte[] data, int bitIndex, int bitWidth, boolean isBigEndian) {
        if (bitIndex < 0)
            throw new IllegalArgumentException("Huh? " + bitIndex + " " + bitWidth);

        int byteIndex = getByteIndex(bitIndex);
        int shift = getShift(byteIndex, bitIndex);
        if (byteIndex >= data.length)
            return 0;
        int value = data[byteIndex] & 0xff;
        if (shift + bitWidth > 8) {
            int otherByteIndex = (isBigEndian ? -1 : +1) + byteIndex;
            if (otherByteIndex < 0 || otherByteIndex >= data.length)
                return 0;
            value = value + data[otherByteIndex] * 256;
        }
        int mask = (1 << bitWidth) - 1;
        return (value >> shift) & mask;
    }

    private static int getShift(int byteIndex, int bitIndex) {
        int shift = bitIndex - byteIndex * 8;
        return shift;
    }

    private static int getByteIndex(int bitIndex) {
        int byteIndex = bitIndex >> 3;
        return byteIndex;
    }

    public double getValue(CANPacket packet) {
        long raw = getRawValue(packet);
        if (isSigned) {
            // extend the upper bit as sign
            raw <<= (64 - length);
            raw >>= (64 - length);
        }
        return raw * mult + offset;
    }

    public int getRawValue(CANPacket packet) {
        return getBitRange(packet.getData(), startOffset, length, isBigEndian);
    }

    public void rename(String niceName) {
        name = niceName;
        isNiceName = true;
    }

    public boolean coversByte(int byteIndex) {
        int startBit = byteIndex * 8;

        if (isBigEndian) {

                startBit = DbcField.crazyMotorolaMath(startBit, length, true);


        }

        if (startOffset > startBit)
            return false;
        return startOffset + length >= startBit + 8;
    }

    @Override
    public int compareTo(DbcField o) {
        if (packetId != o.packetId)
            return Integer.compare(packetId, o.packetId);
        return Integer.compare(startOffset, o.startOffset);
    }

    public int getSid() {
        return packetId;
    }

    public int getByteIndex() {
        return startOffset / 8;
    }
}
