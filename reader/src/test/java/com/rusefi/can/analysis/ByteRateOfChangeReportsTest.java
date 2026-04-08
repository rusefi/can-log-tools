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
        Map<DbcField, ByteStatistics> map = new HashMap<>();
        stats.totalTransitions = transitions;
        map.put(field, stats);

        List<CANPacket> packets = Arrays.asList(
                new CANPacket(0, 100, new byte[]{0}),
                new CANPacket(10, 100, new byte[]{0})
        );

        return new ByteRateOfChange.TraceReport(packets, name, new HashMap<>(map));
    }

    private static ByteStatistics byteStats(int... values) {
        ByteStatistics stats = new ByteStatistics(null);
        for (int value : values) {
            stats.registerValue(value);
        }
        return stats;
    }
}
