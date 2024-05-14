package com.rusefi.can.reader.dbc;

import com.rusefi.can.CANPacket;

public class DbcField {
    private String name;
    private final int startOffset;
    private final int length;
    private final double mult;
    private final double offset;
    private final String category;
    private final boolean isBigEndian;
    private boolean isNiceName;

    public DbcField(String name, int startOffset, int length, double mult, double offset, String category, boolean isBigEndian) {
        this.name = name;
        this.startOffset = startOffset;
        this.length = length;
        this.mult = mult;
        this.offset = offset;
        this.category = category;
        this.isBigEndian = isBigEndian;
    }

    public static DbcField parseField(DbcPacket parent, String line) {
        line = DbcFile.replaceSpecialWithSpaces(line);
        String[] tokens = line.split(" ");
        if (tokens.length < 2)
            return null;
        String name = tokens[1];
        int index = 1;
        try {
            while (!tokens[index - 1].equals(":"))
                index++;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalStateException("While parsing [" + line + "]", e);
        }

        if (DbcFile.debugEnabled)
            System.out.println(line);
        int startOffset;
        try {
            startOffset = Integer.parseInt(tokens[index]);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("While " + line, e);
        }
        int length = Integer.parseInt(tokens[index + 1]);
        int endiannessCode = Integer.parseInt(tokens[index + 2]);
        if (endiannessCode != 0 && endiannessCode != 1)
            throw new IllegalStateException("Unexpected endiannessCode " + endiannessCode);
        boolean isBigEndian = endiannessCode == 0;

        double mult = Double.parseDouble(tokens[index + 3]);
        double offset = Double.parseDouble(tokens[index + 4]);

        return new DbcField(name, startOffset, length, mult, offset, parent.getName(), isBigEndian);
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

    @Override
    public String toString() {
        return "DbcField{" +
                "name='" + name + '\'' +
                ", startOffset=" + startOffset +
                ", length=" + length +
                ", mult=" + mult +
                ", isBigEndian=" + isBigEndian +
                '}';
    }

    public static int getBitRange(byte[] data, int bitIndex, int bitWidth, boolean isBigEndian) {
        if (bitIndex < 0)
            throw new IllegalArgumentException("Huh? " + bitIndex + " " + bitWidth);

        int byteIndex = bitIndex >> 3;
        int shift = bitIndex - byteIndex * 8;
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

    public double getValue(CANPacket packet) {
        return getBitRange(packet.getData(), startOffset, length, isBigEndian) * mult + offset;
    }

    public void rename(String niceName) {
        name = niceName;
        isNiceName = true;
    }

    public boolean coversByte(int byteIndex) {
        int startBit = byteIndex * 8;
        if (startOffset > startBit)
            return false;
        return startOffset + length >= startBit + 8;
    }
}
