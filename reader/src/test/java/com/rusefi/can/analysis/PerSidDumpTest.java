package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import com.rusefi.can.dbc.DbcFile;
import com.rusefi.can.reader.dbc.DbcFileReader;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class PerSidDumpTest {

    @Test
    public void testBySourceReport() throws IOException {
        Path tempDir = Files.createTempDirectory("persid_test");
        try {
            File dbcFile = tempDir.resolve("test.dbc").toFile();
            Files.write(dbcFile.toPath(), (
                    "BO_ 100 MSG1: 8 SOURCE1\n" +
                    " SG_ Field1 : 0|8@1+ (1,0) [0|255] \"\" Vector__XXX\n" +
                    "BO_ 200 MSG2: 8 SOURCE2\n" +
                    " SG_ Field2 : 0|8@1+ (1,0) [0|255] \"\" Vector__XXX\n" +
                    "BO_ 300 MSG3: 8 SOURCE1\n" +
                    " SG_ Field3 : 0|8@1+ (1,0) [0|255] \"\" Vector__XXX\n"
            ).getBytes());

            DbcFile dbc = DbcFileReader.readFromFile(dbcFile.getAbsolutePath());
            List<CANPacket> packets = Arrays.asList(
                    new CANPacket(10, 100, new byte[]{1, 2}),
                    new CANPacket(11, 100, new byte[]{3, 4}),
                    new CANPacket(12, 200, new byte[]{5, 6})
            );

            PerSidDump.handle(dbc, tempDir.toString(), "test_report", packets);

            File reportFile = tempDir.resolve("test_report_by_source.txt").toFile();
            assertTrue("Report file should exist", reportFile.exists());

            String content = new String(Files.readAllBytes(reportFile.toPath()));
            assertTrue("Should contain SOURCE1", content.contains("Source: SOURCE1"));
            assertTrue("Should contain SOURCE2", content.contains("Source: SOURCE2"));
            assertTrue("Should contain MSG1 under SOURCE1 with count 2", content.contains("Frame: 100_0x64 MSG1: 2"));
            assertTrue("Should contain MSG2 under SOURCE2 with count 1", content.contains("Frame: 200_0xc8 MSG2: 1"));
            assertTrue("Should contain MSG3 under SOURCE1 with NO PACKETS", content.contains("Frame: 300_0x12c MSG3 NO PACKETS"));

        } finally {
            recursiveDelete(tempDir.toFile());
        }
    }

    private void recursiveDelete(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                recursiveDelete(f);
            }
        }
        file.delete();
    }
}
