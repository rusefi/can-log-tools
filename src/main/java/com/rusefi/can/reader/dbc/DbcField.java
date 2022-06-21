package com.rusefi.can.reader.dbc;

public class DbcField {
    private final String name;
    private final int startOffset;
    private final int length;
    private final double mult;

    public DbcField(String name, int startOffset, int length, double mult) {
        this.name = name;
        this.startOffset = startOffset;
        this.length = length;
        this.mult = mult;
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

    @Override
    public String toString() {
        return "DbcField{" +
                "name='" + name + '\'' +
                ", startOffset=" + startOffset +
                ", length=" + length +
                ", mult=" + mult +
                '}';
    }

    public static int getBitIndex(byte[] data, int bitIndex, int bitWidth) {
        int byteIndex = bitIndex >> 3;
        int shift = bitIndex - byteIndex * 8;
        int value = data[byteIndex];
        if (shift + bitWidth > 8) {
            value = value + data[1 + byteIndex] * 256;
        }
        int mask = (1 << bitWidth) - 1;
        return (value >> shift) & mask;
    }
}
