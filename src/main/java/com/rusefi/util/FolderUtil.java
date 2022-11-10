package com.rusefi.util;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class FolderUtil {
    public static void handleFolder(String inputFolderName, FileAction fileAction) throws IOException {
        File inputFolder = new File(inputFolderName);
        for (String simpleFileName : Objects.requireNonNull(inputFolder.list((dir, name) -> name.endsWith(".trc")))) {
            System.out.println("Handling input file " + simpleFileName);
            String fullInputFile = inputFolderName + File.separator + simpleFileName;

            fileAction.doJob(simpleFileName, fullInputFile);
        }
    }

    public interface FileAction {
        void doJob(String simpleFileName, String fullFileName) throws IOException;
    }
}
