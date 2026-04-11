package com.rusefi.util;

import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FolderUtilTest {

    @Test
    public void testHandleFolderWithPipe() throws IOException {
        Path tempDir = Files.createTempDirectory("folderUtilTest");
        try {
            Files.createFile(tempDir.resolve("test1.trc"));
            Files.createFile(tempDir.resolve("test2.txt"));
            Files.createFile(tempDir.resolve("test3.csv"));

            List<String> handledFiles = new ArrayList<>();
            FolderUtil.handleFolder(tempDir.toString(), (simpleFileName, fullFileName) -> {
                handledFiles.add(simpleFileName);
            }, ".trc|.txt");

            assertEquals(2, handledFiles.size());
            assertTrue(handledFiles.contains("test1.trc"));
            assertTrue(handledFiles.contains("test2.txt"));
        } finally {
            deleteDirectory(tempDir.toFile());
        }
    }

    @Test
    public void testHandleFolderSingleSuffix() throws IOException {
        Path tempDir = Files.createTempDirectory("folderUtilTestSingle");
        try {
            Files.createFile(tempDir.resolve("test1.trc"));
            Files.createFile(tempDir.resolve("test2.txt"));

            List<String> handledFiles = new ArrayList<>();
            FolderUtil.handleFolder(tempDir.toString(), (simpleFileName, fullFileName) -> {
                handledFiles.add(simpleFileName);
            }, ".trc");

            assertEquals(1, handledFiles.size());
            assertTrue(handledFiles.contains("test1.trc"));
        } finally {
            deleteDirectory(tempDir.toFile());
        }
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}
