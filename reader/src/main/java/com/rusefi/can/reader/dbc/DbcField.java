package com.rusefi.can.reader.dbc;

import com.rusefi.can.CANPacket;

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

    public DbcField(int packetId, String name, int startOffset, int length, double mult, double offset, String category, boolean isBigEndian) {
        this.packetId = packetId;
        this.name = name;
        this.startOffset = crazyMotorolaMath(startOffset, length, isBigEndian);
        this.length = length;
        this.mult = mult;
        this.offset = offset;
        this.category = category;
        this.isBigEndian = isBigEndian;
        if (mult == 0 && offset == 0)
            throw new IllegalArgumentException("Really? multiplier and offset both zero for " + name);
    }

    public static int crazyMotorolaMath(int b, int length, boolean isBigEndian) {
        if (DbcFile.compatibilityWithBrokenRusEfiLogic || !isBigEndian)
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

    public static DbcField parseField(String line, String parentName, int packetId) {
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
        String endiannessCodeString = tokens[index + 2];
        // todo: what is this about exactly?
        if (endiannessCodeString.endsWith("-"))
            endiannessCodeString = endiannessCodeString.substring(0, endiannessCodeString.length() - 1);
        int endiannessCode = Integer.parseInt(endiannessCodeString);
        if (endiannessCode != 0 && endiannessCode != 1)
            throw new IllegalStateException("Unexpected endiannessCode " + endiannessCode);
        boolean isBigEndian = endiannessCode == 0;

        double mult = Double.parseDouble(tokens[index + 3]);
        double offset = Double.parseDouble(tokens[index + 4]);

        return new DbcField(packetId, name, startOffset, length, mult, offset, parentName, isBigEndian);
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
        return getRawValue(packet) * mult + offset;
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
            if (!DbcFile.compatibilityWithBrokenRusEfiLogic) {
                startBit = DbcField.crazyMotorolaMath(startBit, length, true);

            } else {
                if (/* byte endianess less important for one byte fields */ length > 8) {
                    startBit += 8;
                }
            }
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
