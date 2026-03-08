package com.rusefi.can.dbc.util;

import com.rusefi.can.dbc.DbcField;

import java.util.ArrayList;
import java.util.List;

/**
 * this class creates synthetic signals/fields for all unused bits in packet/frame
 */
public class GapFactory {
    private final int bitCount;
    private final boolean[] isUsed;
    private final List<DbcField> signals = new ArrayList<>();
    private final String packetName;

    public GapFactory(List<DbcField> signals, String packetName, int length) {
        this.packetName = packetName;
        this.bitCount = 8 * length;
        this.isUsed = new boolean[bitCount];
        this.signals.addAll(signals);
        java.util.BitSet usedBits = new java.util.BitSet(isUsed.length);
        for (DbcField dbcField : signals) {
            dbcField.getUsedBits(usedBits, dbcField.getStartOffset(), dbcField.getLength(), dbcField.isBigEndian());
        }
        for (int i = 0; i < isUsed.length; i++) {
            isUsed[i] = usedBits.get(i);
        }
    }

    public List<DbcField> withGaps(int packetId) {
        for (int bitIndex = 0; bitIndex < isUsed.length; bitIndex++) {
            if (!isUsed[bitIndex]) {
                int endIndex = findUnusedSectionEndIndex(bitIndex);

                int length = endIndex - bitIndex;
                String name;
                if (length == 8) {
                    name = packetName + "_gap_byte_" + bitIndex;
                } else {
                    name = packetName + "_gap_bits_" + bitIndex + "_" + length;
                }
                signals.add(new DbcField(packetId, name, bitIndex, length, 1, 0, "", false, false));

                bitIndex = endIndex - 1;
            }
        }

        return signals;
    }

    /**
     * @return index after current unused section
     */
    public int findUnusedSectionEndIndex(int bitIndex) {
        bitIndex++; // skipping first unused bit

        while (true) {
            if (bitIndex >= isUsed.length)
                return bitIndex;
            if (isUsed[bitIndex])
                return bitIndex;
            bitIndex++;
            if (bitIndex % 8 == 0)
                return bitIndex; // edge of byte
        }
    }
}
