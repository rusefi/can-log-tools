package com.rusefi.can.reader.dbc;

import com.rusefi.can.CANPacket;

public class DbcField {
    private String name;
    private final int startOffset;
    private final int length;
    private final double mult;
    private final double offset;
    private String category;
    private boolean isNiceName;

    public DbcField(String name, int startOffset, int length, double mult, double offset, String category) {
        this.name = name;
        this.startOffset = startOffset;
        this.length = length;
        this.mult = mult;
        this.offset = offset;
        this.category = category;
    }

    public static DbcField parseField(DbcPacket parent, String line) {
        line = DbcFile.replaceSpecialWithSpaces(line);
        String[] tokens = line.split(" ");
        String name = tokens[1];
        int index = 1;
        while (!tokens[index - 1].equals(":"))
            index++;


        if (DbcFile.debugEnabled)
            System.out.println(line);
        int startOffset;
        try {
            startOffset = Integer.parseInt(tokens[index]);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("While " + line, e);
        }
        int length = Integer.parseInt(tokens[index + 1]);

        double mult = Double.parseDouble(tokens[index + 3]);
        double offset = Double.parseDouble(tokens[index + 4]);

        return new DbcField(name, startOffset, length, mult, offset, parent.getName());
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
        if (bitIndex < 0)
            throw new IllegalArgumentException("Huh? " + bitIndex + " " + bitWidth);
        int byteIndex = bitIndex >> 3;
        int shift = bitIndex - byteIndex * 8;
        if (byteIndex >= data.length)
            return 0;
        int value = data[byteIndex] & 0xff;
        if (shift + bitWidth > 8) {
            if (byteIndex + 1 >= data.length)
                return 0;
            value = value + data[1 + byteIndex] * 256;
        }
        int mask = (1 << bitWidth) - 1;
        return (value >> shift) & mask;
    }

    public double getValue(CANPacket packet) {
        return getBitIndex(packet.getData(), startOffset, length) * mult + offset;
    }

    public void rename(String niceName) {
        name = niceName;
        isNiceName = true;
    }
}
