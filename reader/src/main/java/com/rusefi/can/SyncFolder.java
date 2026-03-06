package com.rusefi.can;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class SyncFolder {
    public static void main(String[] args) throws IOException, ParseException {
        if (args.length < 3) {
            System.out.println("Usage: SyncFolder <folder> <suffix1> <suffix2>");
            return;
        }

        String folderPath = args[0];
        String suffix1 = args[1];
        String suffix2 = args[2];

        runJob(folderPath, suffix1, suffix2);
    }

    private static void runJob(String folderPath, String suffix1, String suffix2) throws IOException, ParseException {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Invalid folder: " + folderPath);
            return;
        }

        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }

        Map<String, File> suffix1Files = new HashMap<>();
        Map<String, File> suffix2Files = new HashMap<>();

        for (File file : files) {
            if (!file.isFile()) continue;

            String name = file.getName();
            if (name.endsWith(suffix1)) {
                String prefix = name.substring(0, name.length() - suffix1.length());
                suffix1Files.put(prefix, file);
            } else if (name.endsWith(suffix2)) {
                String prefix = name.substring(0, name.length() - suffix2.length());
                suffix2Files.put(prefix, file);
            }
        }

        for (Map.Entry<String, File> entry : suffix1Files.entrySet()) {
            String prefix = entry.getKey();
            if (suffix2Files.containsKey(prefix)) {
                File file1 = entry.getValue();
                File file2 = suffix2Files.get(prefix);

                System.out.println("Syncing pair: " + file1.getName() + " and " + file2.getName());
                SyncTrcFiles.sync(file1.getAbsolutePath(), file2.getAbsolutePath());
            }
        }
    }
}
