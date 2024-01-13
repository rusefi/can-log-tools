package com.rusefi.can;

import com.rusefi.can.reader.impl.CANoeReader;
import com.rusefi.can.deprecated.decoders.PacketDecoder;
import com.rusefi.can.deprecated.PacketPayload;
import com.rusefi.can.deprecated.decoders.bmw.BmwRegistry;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * todo: kill this one?
 * @see TrcToMlq
 */
public class CAN2TS {
    public static void main(String[] args) throws IOException {
        CANoeReader reader = CANoeReader.INSTANCE;

        List<CANPacket> packetList = reader.readFile("C:\\stuff\\rusefi_documentation\\OEM-Docs\\Bmw\\2003_7_Series_e65\\HeinrichG-V12-E65_ReverseEngineering\\Log2.log");

//        SteveWriter writer = new SteveWriter("loggerProgram0.log");
//        writer.write(packetList);

        FileWriter fw = new FileWriter("sensors.txt");

        for (CANPacket packet : packetList) {
            PacketDecoder decoder = BmwRegistry.INSTANCE.decoderMap.get(packet.getId());
            if (decoder == null)
                continue;
            PacketPayload payload = decoder.decode(packet);
            if (payload == null)
                continue;


            for (SensorValue value : payload.getValues()) {
                fw.write(packet.getTimeStamp() + "," + value + "\n");
            }
        }
        fw.close();


    }
}
