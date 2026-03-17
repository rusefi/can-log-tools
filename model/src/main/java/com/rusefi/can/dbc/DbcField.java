package com.rusefi.can.dbc;

import com.rusefi.can.CANPacket;

/**
 * also known as 'signal'
 */
public class DbcField implements Comparable<DbcField> {
    private final int packetId;
    private String name;
    private String shortName;
    private final int dbcStartOffset;
    private final int startOffset;
    private final int length;
    private final double mult;
    private final double offset;
    private final String category;
    private final boolean isBigEndian;
    private boolean isNiceName;
    private final boolean isSigned;

    private IDbcPacket parentPacket;

    public void setParentPacket(IDbcPacket parentPacket) {
        this.parentPacket = parentPacket;
    }

    public IDbcPacket getParentPacket() {
        return parentPacket;
    }

    public DbcField(int packetId, String name, int dbcStartOffset, int length, double mult, double offset, String category, boolean isBigEndian, boolean isSigned) {
        this.packetId = packetId;
        this.name = name;
        shortName = name;
        this.dbcStartOffset = dbcStartOffset;
        this.startOffset = crazyMotorolaMath(dbcStartOffset, length, isBigEndian);
        this.length = length;
        this.mult = mult;
        this.offset = offset;
        this.category = category;
        this.isBigEndian = isBigEndian;
        this.isSigned = isSigned;
        if (mult == 0 && offset == 0)
            throw new IllegalArgumentException("Really? multiplier and offset both zero for " + name);
    }

    public String getShortName() {
        return shortName;
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

    // returns the least significant bit
    // for Intel - right bit in the first byte
    // for Motorola - right bit in the last (!!!) byte
    public int getStartOffset() {
        return startOffset;
    }

    public int getDbcStartOffset() {
        return dbcStartOffset;
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
     * Return start bit as stated in DBC (MSB for motorola, LSB for intel)
     * @see ParseDBCWithCommentTest#parseMoto
     * @see ParseDbcFieldTest#testHumanStartIndex
     */
    public int getDbcStartIndex() {
        return getDbcStartIndex(startOffset, length, isBigEndian);
    }

    public static int getDbcStartIndex(int bitIndex, int bitWidth, boolean isBigEndian) {
        if (isBigEndian) {
            // reversed crazyMotorolaMath()
            int b = bitIndex;
            // convert from lsb0 bit numbering to msb0 bit numbering (or msb0 to lsb0)
            b = b - (b % 8) + 7 - (b % 8);
            // convert from lsbit of signal data to msbit of signal data, when bit numbering is msb0
            b = b - bitWidth + 1;
            // convert from msbit of signal data to lsbit of signal data, when bit numbering is msb0
            b = b - (b % 8) + 7 - (b % 8);
            return b;
        }
        else {
            // little endian
            return bitIndex;
        }
    }

    public static int getBitRange(byte[] data, int bitIndex, int bitWidth, boolean isBigEndian) {
        if (bitIndex < 0)
            throw new IllegalArgumentException("Huh? " + bitIndex + " " + bitWidth);

        int byteIndex = getByteIndex(bitIndex);
        int shift = getShift(byteIndex, bitIndex);
        if (byteIndex >= data.length)
            return 0;
        int value = 0;
        int dataShift = 0;
        int remainWidth = shift + bitWidth;
        while (remainWidth > 0) {
            value += (data[byteIndex] & 0xff) << dataShift;
            byteIndex += (isBigEndian ? -1 : +1);
            dataShift += 8;
            remainWidth -= 8;
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

    public void getUsedBits(java.util.BitSet usedBits) {
        getUsedBits(usedBits, startOffset, length, isBigEndian);
    }

    public static void getUsedBits(java.util.BitSet usedBits, int bitIndex, int bitWidth, boolean isBigEndian) {
        if (bitIndex < 0)
            throw new IllegalArgumentException("Huh? " + bitIndex + " " + bitWidth);

        if (isBigEndian) {
            // bitIndex is the LSB in the last byte
            int byteIndex = getByteIndex(bitIndex);
            int shift = getShift(byteIndex, bitIndex);

            if (shift + bitWidth <= 8) {
                for (int i = 0; i < bitWidth; i++) {
                    usedBits.set(bitIndex + i);
                }
            }
            else {
                int bitsInLastByte = 8 - shift;
                for (int i = 0; i < bitsInLastByte; i++) {
                    usedBits.set(bitIndex + i);
                }
                // previous bytes
                getUsedBits(usedBits, (byteIndex - 1) * 8, bitWidth - bitsInLastByte, true);
            }
        }
        else {
            // little endian. Bit numbers are just growing
            for (int i = 0; i < bitWidth; i++) {
                usedBits.set(bitIndex + i);
            }
        }
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
