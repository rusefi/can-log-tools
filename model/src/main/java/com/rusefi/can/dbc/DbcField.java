package com.rusefi.can.dbc;

import com.rusefi.can.CANPacket;
import com.rusefi.can.dbc.util.GapFactory;

import java.util.BitSet;

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
    /**
     * Motorola is big endian, '@0' in DBC
     * Intel is little endian, '@1' in DBC
     */
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

    public boolean isGap() {
        return name.contains(com.rusefi.can.dbc.util.GapFactory.GAP_BITS) || name.contains(com.rusefi.can.dbc.util.GapFactory.GAP_BYTE) || name.contains("_gap_");
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the internal LSB bit index used for actual bit extraction ({@link #getBitRange}).
     * This is the result of applying {@link #crazyMotorolaMath} to the raw DBC start bit:
     * <ul>
     *   <li>Intel (little-endian): same as {@link #getDbcStartOffset()} — LSB of the signal in the first byte.</li>
     *   <li>Motorola (big-endian): transformed value — LSB of the signal in the <em>last</em> byte.</li>
     * </ul>
     * <strong>Do NOT pass this value to the {@link DbcField} constructor</strong> as {@code dbcStartOffset};
     * doing so would double-apply {@code crazyMotorolaMath} and corrupt Motorola signal decoding.
     * Use {@link #getDbcStartOffset()} when you need the raw DBC bit number.
     */
    public int getStartOffset() {
        return startOffset;
    }

    /**
     * Returns the raw start bit number as written in the DBC file.
     * <ul>
     *   <li>Intel (little-endian): LSB of the signal.</li>
     *   <li>Motorola (big-endian): MSB of the signal (DBC convention).</li>
     * </ul>
     * Pass this value to the {@link DbcField} constructor as {@code dbcStartOffset};
     * {@link #crazyMotorolaMath} will be applied internally to produce {@link #getStartOffset()}.
     */
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

    public BitSet getUsedBits() {
        BitSet usedBits = new BitSet();
        getUsedBits(usedBits, startOffset, length, isBigEndian);
        return usedBits;
    }

    public static void getUsedBits(BitSet usedBits, int bitIndex, int bitWidth, boolean isBigEndian) {
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


    private static void setBitRange(byte[] data, int totalBitIndex, int bitWidth, int value, boolean isBigEndian) {
        int leftBitWidth = bitWidth;
        int byteIndex = totalBitIndex >> 3;
        int bitInByteIndex = totalBitIndex - byteIndex * 8;

        if (bitWidth <= 0 || totalBitIndex < 0)
            throw new IllegalArgumentException("Huh? " + totalBitIndex + " " + bitWidth);

        if (bitInByteIndex + bitWidth > 8) {
            int bitsToHandleNow = 8 - bitInByteIndex;
            if (isBigEndian) {
                setBitRange(data, (byteIndex - 1) * 8, leftBitWidth - bitsToHandleNow, value >> bitsToHandleNow, true);
            } else {
                setBitRange(data, totalBitIndex + bitsToHandleNow, leftBitWidth - bitsToHandleNow, value >> bitsToHandleNow, false);
            }
            leftBitWidth = bitsToHandleNow;
        }

        int mask = (1 << leftBitWidth) - 1;
        data[byteIndex] = (byte) (data[byteIndex] & ~(mask << bitInByteIndex));
        int maskedValue = value & mask;
        int shiftedValue = maskedValue << bitInByteIndex;
        data[byteIndex] = (byte) (data[byteIndex] | shiftedValue);
    }

    public void setRawValue(CANPacket packet, int value) {
        setBitRange(packet.getData(), startOffset, length, value, isBigEndian);
    }
    public void setValue(CANPacket packet, double value) {
        setRawValue(packet, (int) ((value - offset) / mult));
    }

}
