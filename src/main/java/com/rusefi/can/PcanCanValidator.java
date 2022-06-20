package com.rusefi.can;

import com.rusefi.can.reader.CANLineReader;
import com.rusefi.can.reader.impl.PcanReader;

import java.io.IOException;

public class PcanCanValidator {
    public static void main(String[] args) throws IOException {
        CANLineReader reader = PcanReader.INSTANCE;

//        CANoeCanValidator.validate("C:\\stuff\\rusefi_documentation\\OEM-Docs\\Bmw\\2003_7_Series_e65\\HeinrichG-V12-E65_ReverseEngineering\\E65-760-andrey-2021-feb-21-engine-off-acc-on.trc", reader);
//        CANoeCanValidator.validate("C:\\stuff\\rusefi_documentation\\OEM-Docs\\Bmw\\2003_7_Series_e65\\HeinrichG-V12-E65_ReverseEngineering\\E65-760-andrey-2021-feb-21-engine-off-inpa-tcu-reset-codes.trc", reader);

        CANoeCanValidator.validate("C:\\stuff\\rusefi_documentation\\OEM-Docs\\Bmw\\2003_7_Series_e65\\HeinrichG-V12-E65_ReverseEngineering\\E65-760-andrey-2021-feb-21-engine-off-inpa-reset-codes.trc", reader);

    }


}
