package com.rusefi.sensor_logs;

import java.io.DataOutputStream;
import java.io.IOException;

public interface BinaryLogEntry {
    static BinaryLogEntry createFloatLogEntry(final String name, final String category) {
        return new BinaryLogEntry() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getCategory() {
                return category;
            }

            @Override
            public String getUnit() {
                return "x";
            }

            @Override
            public int getByteSize() {
                return 4;
            }

            @Override
            public void writeToLog(DataOutputStream dos, double value) throws IOException {
                dos.writeFloat((float) value);
            }

            @Override
            public String toString() {
                return getName();
            }
        };
    }

    String getName();

    String getCategory();

    String getUnit();

    int getByteSize();

    void writeToLog(DataOutputStream dos, double value) throws IOException;
}
