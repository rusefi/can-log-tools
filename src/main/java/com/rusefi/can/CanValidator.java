package com.rusefi.can;

import com.rusefi.can.decoders.bmw.Bmw192;
import com.rusefi.can.reader.CANoeReader;

import java.io.IOException;
import java.util.List;

public class CanValidator {
    public static void main(String[] args) throws IOException {
        validate("C:\\stuff\\rusefi_documentation\\OEM-Docs\\Bmw\\2003_7_Series_e65\\HeinrichG-V12-E65_ReverseEngineering\\Log1.log");
        validate("C:\\stuff\\rusefi_documentation\\OEM-Docs\\Bmw\\2003_7_Series_e65\\HeinrichG-V12-E65_ReverseEngineering\\Log2.log");

    }

    private static void validate(String fileName) throws IOException {
        CANoeReader reader = new CANoeReader();
        List<CANPacket> packetList = reader.readFile(fileName);

        for (CANPacket packet : packetList) {

            if (packet.getId() == Bmw192.ID)
                new Bmw192().decode(packet);

        }
    }
}
