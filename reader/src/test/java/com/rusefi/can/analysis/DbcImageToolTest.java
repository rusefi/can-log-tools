package com.rusefi.can.analysis;

import com.rusefi.can.render.DbcImageTool;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class DbcImageToolTest {
    @Test
    public void testEscapeFileName() {
        assertEquals("normal_name", DbcImageTool.escapeFileName("normal_name"));
        assertEquals("name_with_colon", DbcImageTool.escapeFileName("name:with:colon"));
        assertEquals("name_with_slash", DbcImageTool.escapeFileName("name/with/slash"));
        assertEquals("name_with_backslash", DbcImageTool.escapeFileName("name\\with\\backslash"));
        assertEquals("AVL_TORQ_CRSH_DMEE_ Actual torque crankshaft DME_EGS",
            DbcImageTool.escapeFileName("AVL_TORQ_CRSH_DMEE: Actual torque crankshaft DME\\EGS"));
    }

    @Test
    public void testLongNameIsTruncated() {
        StringBuilder longName = new StringBuilder("SIGNAL_NAME: ");
        for (int i = 0; i < 30; i++)
            longName.append("very long DBC comment ");
        String escaped = DbcImageTool.escapeFileName(longName.toString());
        assertEquals(180, escaped.length());
        assertEquals("SIGNAL_NAME_ very long DBC comment", escaped.substring(0, 34));

        String exactly180 = "x".repeat(180);
        assertEquals(exactly180, DbcImageTool.escapeFileName(exactly180));
    }
}
