package com.rusefi.can.reader.dbc;

public class GapFactory {
    private final static int BYTES = 8;
    private final boolean[] isUsed = new boolean[8 * BYTES];

    public void add(DbcField dbcField) {
//        fields.add(dbcField);
        for (int bitIndex = dbcField.getStartOffset(); bitIndex < dbcField.getStartOffset() + dbcField.getLength(); bitIndex++) {
            isUsed[bitIndex] = true;
        }
    }
}
