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
            for (int bitIndex = dbcField.getHumanStartIndex(); bitIndex < dbcField.getHumanStartIndex() + dbcField.getLength(); bitIndex++) {
                try {
                    isUsed[bitIndex] = true;
                } catch (ArrayIndexOutOfBoundsException e) {
                    //throw new IllegalStateException("While " + packetName + " " + dbcField, e);
                    System.err.println("Too long indexes in " + packetName + " " + dbcField);
                    break;
                }
            }
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
