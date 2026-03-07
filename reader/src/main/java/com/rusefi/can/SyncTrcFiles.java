package com.rusefi.can;

import com.rusefi.can.reader.impl.AutoFormatReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SyncTrcFiles {
    private static final String START_TIME_HEADER = ";   Start time: ";
    public static final String PATHNAME = "synced";

    public static void main(String[] args) throws IOException, ParseException {
        if (args.length < 2) {
            System.out.println("Usage: SyncTrcFiles <file1> <file2> [file3]");
            return;
        }

        sync(args);
    }

    public static void sync(String... paths) throws IOException, ParseException {
        int n = paths.length;
        long[] fileStartTimes = new long[n];
        List<CANPacket>[] allPackets = new List[n];

        for (int i = 0; i < n; i++) {
            fileStartTimes[i] = readStartTime(paths[i]);
            allPackets[i] = AutoFormatReader.INSTANCE.readFile(paths[i]);
            double durationSeconds = 0;
            if (!allPackets[i].isEmpty()) {
                durationSeconds = (allPackets[i].get(allPackets[i].size() - 1).getTimeStampMs() - allPackets[i].get(0).getTimeStampMs()) / 1000.0;
            }
            System.out.println("File " + (i + 1) + " Start Time: " + new Date(fileStartTimes[i]) + " (Duration: " + durationSeconds + "s)");
        }

        for (int i = 0; i < n; i++) {
            if (allPackets[i].isEmpty()) {
                System.out.println("File " + (i + 1) + " is empty.");
                return;
            }
        }

        long overlapStart = Long.MIN_VALUE;
        long overlapEnd = Long.MAX_VALUE;

        long[] absStarts = new long[n];
        long[] absEnds = new long[n];

        for (int i = 0; i < n; i++) {
            absStarts[i] = fileStartTimes[i] + (long) allPackets[i].get(0).getTimeStampMs();
            absEnds[i] = fileStartTimes[i] + (long) allPackets[i].get(allPackets[i].size() - 1).getTimeStampMs();

            overlapStart = Math.max(overlapStart, absStarts[i]);
            overlapEnd = Math.min(overlapEnd, absEnds[i]);
        }

        if (overlapStart >= overlapEnd) {
            System.out.println("No overlapping time period found.");
            return;
        }

        System.out.println("Overlap Start: " + new Date(overlapStart));
        System.out.println("Overlap End: " + new Date(overlapEnd));

        File synchedDir = new File(PATHNAME);
        if (!synchedDir.exists()) {
            synchedDir.mkdir();
        }

        for (int i = 0; i < n; i++) {
            processFile(paths[i], PATHNAME +
                    "/" + new File(paths[i]).getName(), fileStartTimes[i], overlapStart, overlapEnd);
        }

        for (int i = 0; i < n; i++) {
            printDroppedRanges("File " + (i + 1), absStarts[i], absEnds[i], overlapStart, overlapEnd);
        }
    }

    private static void printDroppedRanges(String label, long absStart, long absEnd, long overlapStart, long overlapEnd) {
        long totalDuration = absEnd - absStart;
        System.out.println(label + " dropped ranges:");
        if (absStart < overlapStart) {
            long droppedBefore = overlapStart - absStart;
            double percentBefore = totalDuration == 0 ? 0 : (100.0 * droppedBefore / totalDuration);
            System.out.println("  Before: " + droppedBefore + "ms (" + new Date(absStart) + " to " + new Date(overlapStart) + ", " + String.format("%.2f", percentBefore) + "%)");
        } else {
            System.out.println("  Before: None");
        }
        if (absEnd > overlapEnd) {
            long droppedAfter = absEnd - overlapEnd;
            double percentAfter = totalDuration == 0 ? 0 : (100.0 * droppedAfter / totalDuration);
            System.out.println("  After: " + droppedAfter + "ms (" + new Date(overlapEnd) + " to " + new Date(absEnd) + ", " + String.format("%.2f", percentAfter) + "%)");
        } else {
            System.out.println("  After: None");
        }
    }

    private static void processFile(String inputPath, String outputPath, long fileStartTime, long overlapStart, long overlapEnd) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            
            String line;
            boolean inHeader = true;
            AutoFormatReader.INSTANCE.detectReader(Files.lines(Paths.get(inputPath)).findFirst().get());
            
            int lineIndex = 0;
            while ((line = reader.readLine()) != null) {
                lineIndex++;
                if (line.trim().startsWith(";")) {
                    writer.println(line);
                    continue;
                }
                
                // Once we hit a non-comment line, we are in the data section
                CANPacket packet = AutoFormatReader.INSTANCE.readLine(line, inputPath, lineIndex);
                if (packet != null) {
                    long absTime = fileStartTime + (long) packet.getTimeStampMs();
                    if (absTime >= overlapStart && absTime <= overlapEnd) {
                        writer.println(line);
                    }
                }
            }
        }
    }

    private static long readStartTime(String path) throws IOException, ParseException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(START_TIME_HEADER)) {
                    String dateStr = line.substring(START_TIME_HEADER.length()).trim();
                    // Example: 2/26/2026 17:09:05.020.0
                    // Or: 10.02.2024 18:56:39.137.0
                    
                    // The .0 at the end might be fractional milliseconds or something.
                    // Let's try to handle both common PCAN formats.
                    
                    DateFormat format;
                    if (dateStr.contains("/")) {
                        format = new SimpleDateFormat("M/d/yyyy HH:mm:ss.SSS");
                    } else {
                        format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
                    }
                    
                    // Remove trailing .0 if present (it's often 4 digits after seconds instead of 3)
                    if (dateStr.matches(".*\\.\\d{3}\\.\\d$")) {
                        dateStr = dateStr.substring(0, dateStr.lastIndexOf('.'));
                    }

                    return format.parse(dateStr).getTime();
                }
            }
        }
        throw new IllegalStateException("Start time not found in " + path);
    }
}
