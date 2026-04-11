package com.rusefi.can.util;

import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import static org.junit.Assert.assertTrue;

public class ToolRepositoryTest {
    public static class MockTool {
        public static void main(String[] args) {
            System.out.println("MockTool ran");
        }
    }

    @Test
    public void testToolExecution() throws Exception {
        // Clear tools to avoid running other tools like ValidateDbc which might exit
        ToolRepository.getTools().clear();
        ToolRepository.registerTool(MockTool.class);
        
        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        try {
            ToolRepository.main(new String[0]);
        } finally {
            System.setOut(oldOut);
        }
        
        String output = baos.toString();
        assertTrue("Output should contain 'MockTool ran'", output.contains("MockTool ran"));
    }
}
