package com.rusefi.can.reader.dbc;

import java.util.ArrayList;
import java.util.List;

/**
 * this class creates synthetic signals/fields for all unused bits in packet/frame
 */
public class GapFactory {
    private final static int BYTES = 8;
    private final boolean[] isUsed = new boolean[8 * BYTES];
    private final List<DbcField> signals = new ArrayList<>();
    private final String packetName;

    public GapFactory(List<DbcField> signals, String packetName) {
        this.packetName = packetName;
        this.signals.addAll(signals);
        for (DbcField dbcField : signals) {
            for (int bitIndex = dbcField.getStartOffset(); bitIndex < dbcField.getStartOffset() + dbcField.getLength(); bitIndex++) {
                try {
                    isUsed[bitIndex] = true;
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new IllegalStateException("While " + packetName + " " + dbcField, e);
                }
            }
        }
    }

    public List<DbcField> withGaps(int packetId) {
        for (int bitIndex = 0; bitIndex < isUsed.length; bitIndex++) {
            if (!isUsed[bitIndex]) {
                int endIndex = findUnusedSectionEndIndex(bitIndex);

                int length = endIndex - bitIndex;
                if (length == 8) {
                    signals.add(new DbcField(packetId, packetName + "_gap_byte_" + bitIndex, bitIndex, length, 1, 0, "", false));
                } else {
                    signals.add(new DbcField(packetId, packetName + "_gap_bits_" + bitIndex + "_" + length, bitIndex, length, 1, 0, "", false));
                }

                bitIndex = endIndex - 1;
            }
        }

        return signals;
    }

    /**
     * @return index after current unused section
     */
    private int findUnusedSectionEndIndex(int bitIndex) {
        bitIndex++; // skipping first unused bit

        while (true) {
            if (isUsed[bitIndex])
                return bitIndex;
            bitIndex++;
            if (bitIndex % 8 == 0)
                return bitIndex; // edge of byte
        }
    }
}
