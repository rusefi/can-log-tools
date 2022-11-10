package com.rusefi.can;

import com.rusefi.can.reader.ReaderType;

import java.io.IOException;

public class ByteRateOfChangeSandbox {
    public static void main(String[] args) throws IOException {
        ReaderTypeHolder.INSTANCE.type = ReaderType.CANHACKER;

        ByteRateOfChange.process("C:\\stuff\\rusefi_documentation\\OEM-Docs\\Nissan\\2011_Xterra\\2011-nissan-CAN-June-2021\\engine-not-running.trc");
    }
}
