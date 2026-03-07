package com.rusefi.can;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SyncTrcFilesTest {

    @Test
    public void testSyncTwoFiles() throws IOException, ParseException {
        Path tempDir = Files.createTempDirectory("sync_test");
        try {
            Path file1 = tempDir.resolve("file1.trc");
            Path file2 = tempDir.resolve("file2.trc");

            String content1 = ";$FILEVERSION=2.0\n" +
                    ";   Start time: 2/26/2026 17:09:05.000.0\n" +
                    ";---+-- ------+------ --+-- ----+-- --+-- -- -- -- -- -- -- --\n" +
                    "      1      10.000 RX  0123      8  01 02 03 04 05 06 07 08\n" +
                    "      2      20.000 RX  0123      8  01 02 03 04 05 06 07 08\n" +
                    "      3     100.000 RX  0123      8  01 02 03 04 05 06 07 08\n";

            String content2 = ";$FILEVERSION=2.0\n" +
                    ";   Start time: 2/26/2026 17:09:05.000.0\n" +
                    ";---+-- ------+------ --+-- ----+-- --+-- -- -- -- -- -- -- --\n" +
                    "      1      15.000 RX  0123      8  01 02 03 04 05 06 07 08\n" +
                    "      2      25.000 RX  0123      8  01 02 03 04 05 06 07 08\n" +
                    "      3     110.000 RX  0123      8  01 02 03 04 05 06 07 08\n";

            Files.write(file1, content1.getBytes());
            Files.write(file2, content2.getBytes());

            // SyncTrcFiles writes to a "synched" directory in the current working directory
            // This is a limitation of the current implementation of SyncTrcFiles.
            // For the test, we'll just run it and then check the "synched" directory.
            SyncTrcFiles.sync(file1.toString(), file2.toString());

            File synched1 = new File("synched/file1.trc");
            File synched2 = new File("synched/file2.trc");

            assertTrue("File 1 should be created", synched1.exists());
            assertTrue("File 2 should be created", synched2.exists());

            List<String> lines1 = Files.readAllLines(synched1.toPath());
            List<String> lines2 = Files.readAllLines(synched2.toPath());

            // Check overlap: 15ms to 100ms
            // file1: 20ms, 100ms should remain (10ms is dropped)
            // file2: 15ms, 25ms should remain (110ms is dropped)
            
            // Expected lines for file1 (ignoring header)
            long dataCount1 = lines1.stream().filter(l -> !l.trim().startsWith(";")).count();
            assertEquals("File 1 should have 2 data packets", 2, dataCount1);

            long dataCount2 = lines2.stream().filter(l -> !l.trim().startsWith(";")).count();
            assertEquals("File 2 should have 2 data packets", 2, dataCount2);

        } finally {
            recursiveDelete(tempDir.toFile());
            recursiveDelete(new File("synched"));
        }
    }

    @Test
    public void testSyncThreeFiles() throws IOException, ParseException {
        Path tempDir = Files.createTempDirectory("sync_test_3");
        try {
            Path file1 = tempDir.resolve("f1.trc");
            Path file2 = tempDir.resolve("f2.trc");
            Path file3 = tempDir.resolve("f3.trc");

            String header = ";$FILEVERSION=2.0\n" +
                    ";   Start time: 2/26/2026 17:09:05.000.0\n" +
                    ";---+-- ------+------ --+-- ----+-- --+-- -- -- -- -- -- -- --\n";

            Files.write(file1, (header + "      1      10.000 RX  0123      8  01 02 03 04 05 06 07 08\n" +
                                       "      2      60.000 RX  0123      8  01 02 03 04 05 06 07 08\n" +
                                       "      3     100.000 RX  0123      8  01 02 03 04 05 06 07 08\n").getBytes());
            Files.write(file2, (header + "      1      20.000 RX  0123      8  01 02 03 04 05 06 07 08\n" +
                                       "      2      60.000 RX  0123      8  01 02 03 04 05 06 07 08\n" +
                                       "      3     110.000 RX  0123      8  01 02 03 04 05 06 07 08\n").getBytes());
            Files.write(file3, (header + "      1      30.000 RX  0123      8  01 02 03 04 05 06 07 08\n" +
                                       "      2      60.000 RX  0123      8  01 02 03 04 05 06 07 08\n" +
                                       "      3     120.000 RX  0123      8  01 02 03 04 05 06 07 08\n").getBytes());

            // Expected overlap:
            // f1: 10 to 100
            // f2: 20 to 110
            // f3: 30 to 120
            // Max of starts: 30. Min of ends: 100.
            // Overlap: 30 to 100
            SyncTrcFiles.sync(file1.toString(), file2.toString(), file3.toString());

            // f1: 60, 100 -> 2 packets
            // f2: 60 -> 1 packet
            // f3: 30, 60 -> 2 packets

            assertEquals("f1.trc should have 2 packets (60ms, 100ms)", 2, countDataPackets(new File("synched/f1.trc")));
            assertEquals("f2.trc should have 1 packet (60ms)", 1, countDataPackets(new File("synched/f2.trc")));
            assertEquals("f3.trc should have 2 packets (30ms, 60ms)", 2, countDataPackets(new File("synched/f3.trc")));

        } finally {
            recursiveDelete(tempDir.toFile());
            recursiveDelete(new File("synched"));
        }
    }

    @Test
    public void testNoOverlap() throws IOException, ParseException {
        Path tempDir = Files.createTempDirectory("sync_no_overlap");
        try {
            Path file1 = tempDir.resolve("f1.trc");
            Path file2 = tempDir.resolve("f2.trc");

            String header1 = ";$FILEVERSION=2.0\n;   Start time: 2/26/2026 17:00:00.000.0\n;---+-- ------+------ --+-- ----+-- --+-- -- -- -- -- -- -- --\n";
            String header2 = ";$FILEVERSION=2.0\n;   Start time: 2/26/2026 18:00:00.000.0\n;---+-- ------+------ --+-- ----+-- --+-- -- -- -- -- -- -- --\n";

            Files.write(file1, (header1 + "      1      10.000 RX  0123      8  01 02 03 04 05 06 07 08\n").getBytes());
            Files.write(file2, (header2 + "      1      10.000 RX  0123      8  01 02 03 04 05 06 07 08\n").getBytes());

            SyncTrcFiles.sync(file1.toString(), file2.toString());

            File synchedDir = new File("synched");
            // If no overlap, sync returns early and might not create files or might create empty ones depending on implementation.
            // Current implementation returns early after printing "No overlapping time period found."
            // It doesn't even enter the loop to process files.
            
            File synched1 = new File("synched/f1.trc");
            assertTrue("Synched file should not exist if no overlap", !synched1.exists());

        } finally {
            recursiveDelete(tempDir.toFile());
            recursiveDelete(new File("synched"));
        }
    }

    private long countDataPackets(File file) throws IOException {
        return Files.readAllLines(file.toPath()).stream().filter(l -> !l.trim().startsWith(";")).count();
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
