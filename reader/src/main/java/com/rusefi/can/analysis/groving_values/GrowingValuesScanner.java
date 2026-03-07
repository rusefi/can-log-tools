package com.rusefi.can.analysis.groving_values;

import com.rusefi.can.CANPacket;
import com.rusefi.can.core.ByteId;
import com.rusefi.can.dbc.DbcField;
import com.rusefi.can.dbc.DbcFile;
import com.rusefi.can.dbc.DbcPacket;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GrowingValuesScanner {
    public static void scanForGrowing(DbcFile dbc, String simpleFileName, List<CANPacket> packets, String reportDestinationFolder, int delta) throws IOException {
        String outputFileName = reportDestinationFolder + File.separator + simpleFileName + "_" + delta + "_growing.txt";
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(outputFileName))) {

            runScanner(dbc, packets, pw, delta);
        }
    }

    private static void runScanner(DbcFile dbc, List<CANPacket> packets, PrintWriter pw, int delta) {
        Map<ByteId, ByteState> states = runScanner(packets, delta);

        for (ByteState state : states.values()) {
            if (state.isIncrementByte()) {

                int sid = state.byteId.getSid();
                DbcPacket packet = dbc.getPacket(sid);
                String key = packet == null ? Integer.toString(sid) : packet.getName();
                if (packet != null) {
                    DbcField field = packet.getFieldAtByte(state.byteId.getByteIndex());
                    if (field != null)
                        key += " " + field.getName();
                }
                pw.println(key + " only increments at " + state.byteId.getByteIndex() + " last value " + state.value);
            }
        }

    }

    public static Map<ByteId, ByteState> runScanner(List<CANPacket> packets, int delta) {
        Map<ByteId, ByteState> states = new TreeMap<>();


        for (CANPacket packet : packets) {
            for (int byteIndex = 0; byteIndex < packet.getData().length; byteIndex++) {
                byte byteValue = packet.getData()[byteIndex];

                ByteId byteId = ByteId.createByte(packet.getId(), byteIndex);
                if (!states.containsKey(byteId)) {
                    states.put(byteId, new ByteState(byteId, byteValue));
                    continue;
                }
                ByteState state = states.get(byteId);

                if (state.value == byteValue) {
                    // value has not changed - boring
                    continue;
                }


                int actualDelta = (byteValue - state.value) & 0xFF;

                boolean isIncrement = actualDelta <= delta;
                state.withIncrement = state.withIncrement || isIncrement;
                state.badChange = state.badChange || !isIncrement;

                state.value = byteValue;
            }
        }
        return states;
    }

    public static class ByteState {
        private final ByteId byteId;
        private int value;
        public boolean withIncrement;
        public boolean badChange;

        public ByteState(ByteId byteId, int value) {
            this.byteId = byteId;
            this.value = value;
        }

        public boolean isIncrementByte() {
            return this.withIncrement && !this.badChange;
        }
    }

}
