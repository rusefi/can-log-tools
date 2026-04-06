package com.rusefi.can.tool.sync;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SyncFolder {
    public static void main(String[] args) throws IOException, ParseException {
        if (args.length < 3) {
            System.out.println("Usage: SyncFolder <folder> <suffix1> <suffix2> [suffix3]");
            return;
        }

        String folderPath = args[0];
        String[] suffixes = new String[args.length - 1];
        System.arraycopy(args, 1, suffixes, 0, suffixes.length);
        System.out.println("SyncFolder " + folderPath + " " + Arrays.toString(suffixes));

        runJob(folderPath, suffixes);
    }

    private static void runJob(String folderPath, String[] suffixes) throws IOException, ParseException {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Invalid folder: " + folderPath);
            return;
        }

        File[] files = folder.listFiles();
        if (files == null) {
            System.out.println("No files: " + folderPath);
            return;
        }

        Map<String, File>[] suffixFiles = new Map[suffixes.length];
        for (int i = 0; i < suffixes.length; i++) {
            suffixFiles[i] = new HashMap<>();
        }

        for (File file : files) {
            if (!file.isFile())
                continue;

            String name = file.getName();
            for (int i = 0; i < suffixes.length; i++) {
                String suffix = suffixes[i];
//                System.out.println(name + ": Checking if " + suffix);
                if (name.endsWith(suffix)) {
                    String prefix = name.substring(0, name.length() - suffix.length());
                    suffixFiles[i].put(prefix, file);
//                    System.out.println("Found " + prefix);
                    break;
                }
            }
        }

        for (Map.Entry<String, File> entry : suffixFiles[0].entrySet()) {
            String prefix = entry.getKey();

            boolean allFound = true;
            String[] paths = new String[suffixes.length];
            paths[0] = entry.getValue().getAbsolutePath();

            for (int i = 1; i < suffixes.length; i++) {
                if (suffixFiles[i].containsKey(prefix)) {
                    paths[i] = suffixFiles[i].get(prefix).getAbsolutePath();
                } else {
                    allFound = false;
                    System.out.println("Not all found");
                    break;
                }
            }

            if (allFound) {
                System.out.println("Syncing group for prefix [" + prefix + "]: " + Arrays.toString(paths));
                SyncTrcFiles.sync(paths);
            }
        }
    }
}
