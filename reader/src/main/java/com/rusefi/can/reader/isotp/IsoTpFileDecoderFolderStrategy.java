package com.rusefi.can.reader.isotp;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IsoTpFileDecoderFolderStrategy {

    static private final List<Integer> isoTpIds = Arrays.asList(
            0x7DF,  // common UDS scanner broadcast
            0x7E0, 0x7E1, 0x7E2, 0x7E3, 0x7E4, 0x7E5, 0x7E6, 0x7E7, // common UDS scanner
            0x7E8, 0x7E9, 0x7EA, 0x7EB, 0x7EC, 0x7ED, 0x7EE, 0x7EF, // common UDS device
            0x618,  // BMW EGS
            0x6F1, 0x6F2, 0x6F3, 0x6F4  // BMW scanner
    );
    static private final int isoHeaderByteIndex = 1;    // 1 for BMW, 0 for standard UDS

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalStateException("Folder name argument expected");
        }
        String folderName = args[0];
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
