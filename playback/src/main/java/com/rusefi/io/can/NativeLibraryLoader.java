package com.rusefi.io.can;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class NativeLibraryLoader {
    public static void loadLibrary(String libraryName) throws IOException {
        String fileName = libraryName + ".dll";
        InputStream inputStream = NativeLibraryLoader.class.getResourceAsStream("/" + fileName);
        if (inputStream == null) {
            // Try without leading slash just in case
            inputStream = NativeLibraryLoader.class.getResourceAsStream(fileName);
        }
        if (inputStream == null) {
            throw new FileNotFoundException("Could not find library " + fileName + " in classpath");
        }

        File tempFile = File.createTempFile(libraryName, ".dll");
        tempFile.deleteOnExit();

        Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        System.out.println("Loading " + libraryName + " from " + tempFile.getAbsolutePath());
        System.load(tempFile.getAbsolutePath());
    }
}
