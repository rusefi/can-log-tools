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

    public static void main(String[] args) throws IOException, ParseException {
        if (args.length < 2) {
            System.out.println("Usage: SyncTrcFiles <file1> <file2>");
            return;
        }

        String file1 = args[0];
        String file2 = args[1];

        sync(file1, file2);
    }

    public static void sync(String path1, String path2) throws IOException, ParseException {
        long start1 = readStartTime(path1);
        long start2 = readStartTime(path2);

        System.out.println("File 1 Start Time: " + new Date(start1) + " (" + start1 + " ms)");
        System.out.println("File 2 Start Time: " + new Date(start2) + " (" + start2 + " ms)");

        List<CANPacket> packets1 = AutoFormatReader.INSTANCE.readFile(path1);
        List<CANPacket> packets2 = AutoFormatReader.INSTANCE.readFile(path2);

        if (packets1.isEmpty() || packets2.isEmpty()) {
            System.out.println("One of the files is empty.");
            return;
        }

        long absStart1 = start1 + (long) packets1.get(0).getTimeStampMs();
        long absEnd1 = start1 + (long) packets1.get(packets1.size() - 1).getTimeStampMs();

        long absStart2 = start2 + (long) packets2.get(0).getTimeStampMs();
        long absEnd2 = start2 + (long) packets2.get(packets2.size() - 1).getTimeStampMs();

        long overlapStart = Math.max(absStart1, absStart2);
        long overlapEnd = Math.min(absEnd1, absEnd2);

        if (overlapStart >= overlapEnd) {
            System.out.println("No overlapping time period found.");
            return;
        }

        System.out.println("Overlap Start: " + new Date(overlapStart));
        System.out.println("Overlap End: " + new Date(overlapEnd));

        File synchedDir = new File("synched");
        if (!synchedDir.exists()) {
            synchedDir.mkdir();
        }

        processFile(path1, "synched/" + new File(path1).getName(), start1, overlapStart, overlapEnd);
        processFile(path2, "synched/" + new File(path2).getName(), start2, overlapStart, overlapEnd);

        printDroppedRanges("File 1", absStart1, absEnd1, overlapStart, overlapEnd);
        printDroppedRanges("File 2", absStart2, absEnd2, overlapStart, overlapEnd);
    }

    private static void printDroppedRanges(String label, long absStart, long absEnd, long overlapStart, long overlapEnd) {
        System.out.println(label + " dropped ranges:");
        if (absStart < overlapStart) {
            System.out.println("  Before: " + (overlapStart - absStart) + "ms (" + new Date(absStart) + " to " + new Date(overlapStart) + ")");
        } else {
            System.out.println("  Before: None");
        }
        if (absEnd > overlapEnd) {
            System.out.println("  After: " + (absEnd - overlapEnd) + "ms (" + new Date(overlapEnd) + " to " + new Date(absEnd) + ")");
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
