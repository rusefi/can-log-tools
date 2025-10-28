package com.rusefi.can.reader.isotp;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class IsoTpFileDecoderFolderStrategy {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalStateException("Folder name argument expected");
        }
        String folderName = args[0];
        processFolder(folderName);
    }

    private static void processFolder(String folder) throws IOException {
        Set<Integer> isoTpIds = new HashSet<>(Arrays.asList(0x618, 0x6F4));
        String excludeProcessed = "^(?!processed).+\\.trc$";
        for (File f : new File(folder).listFiles((dir, name) -> name.matches(excludeProcessed))) {
            IsoTpFileDecoder.run(f.getAbsolutePath(), isoTpIds);
        }
    }
}
