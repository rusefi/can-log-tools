package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import com.rusefi.can.analysis.ByteRateOfChange.ByteStatistics;
import com.rusefi.can.dbc.DbcField;
import com.rusefi.can.dbc.DbcFile;
import com.rusefi.can.dbc.DbcPacket;
import org.junit.Test;

import java.util.*;
import static org.junit.Assert.*;


public class ByteRateOfChangeReportsTest {

    @Test
    public void hasVisualDifference_returnsFalseForIdenticalStats() {
        DbcField field = createDbcField();
        ByteRateOfChange.TraceReport trace1 = createTraceReport("a", field, byteStats(1, 2), 1);
        ByteRateOfChange.TraceReport trace2 = createTraceReport("b", field, byteStats(1, 2), 1);

        assertFalse(ByteRateOfChangeReports.hasVisualDifference(trace1, trace2, field));
    }

    @Test
    public void hasVisualDifference_returnsTrueForDifferentUniqueValueCount() {
        DbcField field = createDbcField();
        ByteRateOfChange.TraceReport trace1 = createTraceReport("a", field, byteStats(1, 2, 3), 2);
        ByteRateOfChange.TraceReport trace2 = createTraceReport("b", field, byteStats(1, 2), 2);

        assertTrue(ByteRateOfChangeReports.hasVisualDifference(trace1, trace2, field));
    }

    @Test
    public void hasVisualDifference_returnsTrueForDifferentValueSets() {
        DbcField field = createDbcField();
        ByteRateOfChange.TraceReport trace1 = createTraceReport("a", field, byteStats(1, 2), 1);
        ByteRateOfChange.TraceReport trace2 = createTraceReport("b", field, byteStats(1, 3), 1);

        assertTrue(ByteRateOfChangeReports.hasVisualDifference(trace1, trace2, field));
    }

    @Test
    public void hasVisualDifference_returnsTrueForDifferentTransitions() {
        DbcField field = createDbcField();
        ByteRateOfChange.TraceReport trace1 = createTraceReport("a", field, byteStats(1, 2), 1);
        ByteRateOfChange.TraceReport trace2 = createTraceReport("b", field, byteStats(1, 2), 3);

        assertTrue(ByteRateOfChangeReports.hasVisualDifference(trace1, trace2, field));
    }

    private static DbcField createDbcField() {
        DbcFile dbc = new DbcFile();
        DbcField field = new DbcField(100, "FieldA", 0, 8, 1.0, 0.0, "P100", false, false);
        DbcPacket packet = new DbcPacket(100, "Packet100", "SRC", 8, Collections.singletonList(field), dbc);
        dbc.addPacket(packet);
        return packet.getByName("FieldA");
    }

    private static ByteRateOfChange.TraceReport createTraceReport(String name, DbcField field, ByteStatistics stats, int transitions) {
        List<Integer> values = new ArrayList<>(stats.getUniqueValues());
        if (values.isEmpty()) {
            values.add(0);
        }

        List<CANPacket> packets = new ArrayList<>();
        int packetCount = Math.max(1, transitions + 1);

        for (int i = 0; i < packetCount; i++) {
            int rawValue = values.get(Math.min(i, values.size() - 1));
            CANPacket packet = new CANPacket(i * 10L, field.getSid(), new byte[8]);
            field.setValue(packet, field.getMult() * rawValue + field.getOffset());
            packets.add(packet);
        }

        stats.totalTransitions = transitions;

        HashMap<DbcField, ByteStatistics> map = new HashMap<>();
        map.put(field, stats);

        return new ByteRateOfChange.TraceReport(packets, name, map);
    }

    private static ByteStatistics byteStats(int... values) {
        ByteStatistics stats = new ByteStatistics(null);
        for (int value : values) {
            stats.registerValue(value);
        }
        return stats;
    }
}
