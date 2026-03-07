package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.dbc.DbcField;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.dbc.DbcPacket;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class MatchFinderTest {

    @Test
    public void testMatchFinder() throws IOException {
        Path tempDir = Files.createTempDirectory("match_finder_test");
        try {
            File dbc1 = tempDir.resolve("test1.dbc").toFile();
            File trc1 = tempDir.resolve("test1.trc").toFile();
            File dbc2 = tempDir.resolve("test2.dbc").toFile();
            File trc2 = tempDir.resolve("test2.trc").toFile();

            // Create DBC 1 with one field
            Files.write(dbc1.toPath(), ("BO_ 100 TEST1: 8 Vector__XXX\n" +
                    " SG_ Field1 : 0|8@1+ (1,0) [0|255] \"\" Vector__XXX").getBytes());

            // Create TRC 1 with some data for Field1
            // PCAN 2.0 format: index, time, type, sid, length, data
            Files.write(trc1.toPath(), (";$FILEVERSION=2.0\n" +
                    ";   Start time: 2/26/2026 17:00:00.000.0\n" +
                    "      1      10.000 RX  0064 - 8  0A 00 00 00 00 00 00 00\n" +
                    "      2      20.000 RX  0064 - 8  14 00 00 00 00 00 00 00\n" +
                    "      3      30.000 RX  0064 - 8  1E 00 00 00 00 00 00 00\n").getBytes());

            // Create DBC 2 with one field (different name, same logic)
            Files.write(dbc2.toPath(), ("BO_ 200 TEST2: 8 Vector__XXX\n" +
                    " SG_ Field2 : 8|8@1+ (1,0) [0|255] \"\" Vector__XXX").getBytes());

            // Create TRC 2 with same data but in different byte and different ID
            Files.write(trc2.toPath(), (";$FILEVERSION=2.0\n" +
                    ";   Start time: 2/26/2026 17:00:00.000.0\n" +
                    "      1      10.000 RX  00C8 - 8  00 0A 00 00 00 00 00 00\n" +
                    "      2      20.000 RX  00C8 - 8  00 14 00 00 00 00 00 00\n" +
                    "      3      30.000 RX  00C8 - 8  00 1E 00 00 00 00 00 00\n").getBytes());

            MatchFinder.main(new String[]{dbc1.getAbsolutePath(), trc1.getAbsolutePath(), dbc2.getAbsolutePath(), trc2.getAbsolutePath()});

            File report = new File("match_report/index.html");
            assertTrue("Report should exist", report.exists());
            
            // Check if some image was generated
            File imagesDir = new File("match_report/images");
            assertTrue("Images directory should exist", imagesDir.exists());
            assertTrue("Should have at least one image", imagesDir.listFiles().length > 0);

        } finally {
            recursiveDelete(tempDir.toFile());
            recursiveDelete(new File("match_report"));
        }
    }

    @Test
    public void testExcludeConstantValues() throws IOException {
        Path tempDir = Files.createTempDirectory("match_finder_const_test");
        try {
            File dbc1 = tempDir.resolve("test1.dbc").toFile();
            File trc1 = tempDir.resolve("test1.trc").toFile();
            File dbc2 = tempDir.resolve("test2.dbc").toFile();
            File trc2 = tempDir.resolve("test2.trc").toFile();

            // Trace 1: one changing field, one constant field
            Files.write(dbc1.toPath(), ("BO_ 100 TEST1: 8 Vector__XXX\n" +
                    " SG_ Changing1 : 0|8@1+ (1,0) [0|255] \"\" Vector__XXX\n" +
                    " SG_ Constant1 : 8|8@1+ (1,0) [0|255] \"\" Vector__XXX").getBytes());
            Files.write(trc1.toPath(), (";$FILEVERSION=2.0\n" +
                    ";   Start time: 2/26/2026 17:00:00.000.0\n" +
                    "      1      10.000 RX  0064 - 8  0A 2A 00 00 00 00 00 00\n" +
                    "      2      20.000 RX  0064 - 8  14 2A 00 00 00 00 00 00\n" +
                    "      3      30.000 RX  0064 - 8  1E 2A 00 00 00 00 00 00\n").getBytes());

            // Trace 2: one changing field, one constant field
            Files.write(dbc2.toPath(), ("BO_ 200 TEST2: 8 Vector__XXX\n" +
                    " SG_ Changing2 : 0|8@1+ (1,0) [0|255] \"\" Vector__XXX\n" +
                    " SG_ Constant2 : 8|8@1+ (1,0) [0|255] \"\" Vector__XXX").getBytes());
            Files.write(trc2.toPath(), (";$FILEVERSION=2.0\n" +
                    ";   Start time: 2/26/2026 17:00:00.000.0\n" +
                    "      1      10.000 RX  00C8 - 8  0A 2A 00 00 00 00 00 00\n" +
                    "      2      20.000 RX  00C8 - 8  14 2A 00 00 00 00 00 00\n" +
                    "      3      30.000 RX  00C8 - 8  1E 2A 00 00 00 00 00 00\n").getBytes());

            MatchFinder.main(new String[]{dbc1.getAbsolutePath(), trc1.getAbsolutePath(), dbc2.getAbsolutePath(), trc2.getAbsolutePath()});

            File report = new File("match_report/index.html");
            assertTrue("Report should exist", report.exists());
            String htmlContent = new String(Files.readAllBytes(report.toPath()));
            
            // Should contain "Changing2" as it's the field from trace 2 being matched
            assertTrue("Should contain Changing2", htmlContent.contains("Changing2"));
            // Should NOT contain "Constant2" as it's constant
            assertTrue("Should NOT contain Constant2", !htmlContent.contains("Constant2"));
            
            // The best match for Changing2 should be Changing1
            assertTrue("Changing2 should be matched with Changing1", htmlContent.contains("Changing1"));

            // Verification for new features: DBC filename and red color for different names
            assertTrue("Should contain DBC1 filename", htmlContent.contains("test1.dbc"));
            assertTrue("Should contain DBC2 filename", htmlContent.contains("test2.dbc"));
            assertTrue("Should contain red color because Changing2 != Changing1", htmlContent.contains("color='red'"));

        } finally {
            recursiveDelete(tempDir.toFile());
            recursiveDelete(new File("match_report"));
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
