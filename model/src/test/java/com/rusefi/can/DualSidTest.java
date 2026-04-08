package com.rusefi.can;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class DualSidTest {
    @Test
    public void test() {
        String formattedSid = DualSid.dualSid(123);
        assertEquals("123_07B", formattedSid);
    }
}
