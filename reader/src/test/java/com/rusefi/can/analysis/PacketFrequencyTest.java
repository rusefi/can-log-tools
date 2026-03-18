package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import com.rusefi.can.dbc.DbcFile;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class PacketFrequencyTest {
    @Test
    public void testPacketFrequency() throws IOException {
        List<CANPacket> packets = new ArrayList<>();
        // ID 100: 0ms, 100ms, 200ms -> frequency (200-0)/(3-1) = 100ms
        packets.add(new CANPacket(0, 100, new byte[8]));
        packets.add(new CANPacket(100, 100, new byte[8]));
        packets.add(new CANPacket(200, 100, new byte[8]));

        // ID 200: 0ms, 50ms, 100ms, 150ms -> frequency (150-0)/(4-1) = 50ms
        packets.add(new CANPacket(0, 200, new byte[8]));
        packets.add(new CANPacket(50, 200, new byte[8]));
        packets.add(new CANPacket(100, 200, new byte[8]));
        packets.add(new CANPacket(150, 200, new byte[8]));

        String tempDir = Files.createTempDirectory("packet_frequency_test").toString();
        PacketFrequency.write(new DbcFile(), tempDir, packets, "test");

        File report = new File(tempDir, "frequency_test.txt");
        assertTrue(report.exists());

        String content = new String(Files.readAllBytes(report.toPath()));
        // Depending on whether 100 or 200 is first in TreeMap (both should be there)
        assertTrue(content.contains("id=100 frequencyMs=100.00 count=3"));
        assertTrue(content.contains("id=200 frequencyMs=50.00 count=4"));
    }
}
