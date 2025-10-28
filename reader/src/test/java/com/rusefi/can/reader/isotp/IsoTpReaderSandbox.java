package com.rusefi.can.reader.isotp;

import java.io.IOException;

public class IsoTpReaderSandbox {
    public static void main(String[] args) throws IOException {
        //String fileName = "C:\\stuff\\1\\b.trc";


        //IsoTpFileDecoder.run(fileName);

        String folder = "C:\\Projects\\Rusefi\\8hp\\can-traces\\unit-1";

        IsoTpFileDecoderFolderStrategy.main(new String[]{folder});
    }
}
