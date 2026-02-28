package com.rusefi.can.analysis;

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
}
