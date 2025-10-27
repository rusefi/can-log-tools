package com.rusefi.can.reader.isotp;

import java.io.File;
import java.io.IOException;

public class IsoTpReaderSandbox {
    public static void main(String[] args) throws IOException {
        //String fileName = "C:\\stuff\\1\\b.trc";

        //IsoTpFileDecoder.run(fileName);

        String folder = "C:\\Projects\\Rusefi\\8hp\\can-traces\\unit-1";

        String excludeProcessed = "^(?!processed).+\\.trc$";
        for( File f : new File(folder).listFiles((dir, name) -> name.matches(excludeProcessed))){
            IsoTpFileDecoder.run(f.getAbsolutePath());
        }
    }
}
