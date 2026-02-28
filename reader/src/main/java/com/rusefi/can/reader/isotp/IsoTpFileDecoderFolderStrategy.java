package com.rusefi.can.reader.isotp;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IsoTpFileDecoderFolderStrategy {

    static private final List<Integer> isoTpIds = Arrays.asList(
            0x7DF,  // common UDS scanner broadcast
            0x7E0, 0x7E1, 0x7E2, 0x7E3, 0x7E4, 0x7E5, 0x7E6, 0x7E7, // common UDS scanner
            0x7E8, 0x7E9, 0x7EA, 0x7EB, 0x7EC, 0x7ED, 0x7EE, 0x7EF, // common UDS device
            0x618,  // BMW EGS
            0x6F1, 0x6F2, 0x6F3, 0x6F4  // BMW scanner
    );
    static private int isoHeaderByteIndex = 1;    // 1 for BMW, 0 for standard UDS

    public static boolean withTimestamp = true;

    @Parameter(description = "<mandatory_filename>")
    private List<String> mainParameters = new ArrayList<>();

    @Parameter(names = {"--withTimestamp", "-t"})
    private boolean withTimestampArg = false;

    @Parameter(names = "--isoIndex")
    private Integer isoHeaderByteIndexArg = 1; // Default value

    public static void main(String[] args) throws IOException {
        IsoTpFileDecoderFolderStrategy appArgs = new IsoTpFileDecoderFolderStrategy();

        JCommander jc = JCommander.newBuilder()
                .addObject(appArgs)
                .build();

        try {
            jc.parse(args);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            jc.usage(); // Prints help menu if user enters wrong args
            return;
        }

        if (args.length < 1) {
            throw new IllegalStateException("Folder name argument expected");
        }
        if (appArgs.mainParameters.size() != 1) {
            throw new ParameterException("The first unnamed argument (filename) is mandatory.");
        }
        String folderName = appArgs.mainParameters.get(0);
        withTimestamp = appArgs.withTimestampArg;
        isoHeaderByteIndex = appArgs.isoHeaderByteIndexArg;

        processFolder(folderName);
    }

    private static void processFolder(String folder) throws IOException {
        String excludeProcessed = "^(?!processed).+\\.trc$";
        File[] files = new File(folder).listFiles((dir, name) -> name.matches(excludeProcessed));

        if (files == null) {
            throw new IOException("Failed to list files in folder: " + folder);
        }

        for (File f : files) {
            if (f.isDirectory())
                continue;
            IsoTpFileDecoder.run(f.getAbsolutePath(), new HashSet<>(isoTpIds), isoHeaderByteIndex);
        }
    }
}
