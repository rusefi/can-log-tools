package com.rusefi.can;

import com.rusefi.can.decoders.bmw.Bmw0AA;
import com.rusefi.can.decoders.bmw.Bmw0B5;
import com.rusefi.can.decoders.bmw.Bmw1D0;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BmwE65DecoderTest {
    @Test
    public void decodeRpm() {
        CANPacket packetAA = new CANPacket(1,
                0xAA,
                new byte[]{0x39, 0x47, 0x02, 0x00, (byte) 0x87, 0x0A, (byte) 0x80, 0x33});

        PacketPayload payload = new Bmw0AA().decode(packetAA);
        assertEquals(2, payload.getValues().length);
        assertValue(SensorType.PPS, 0.0, payload.getValues()[0]);
        assertValue(SensorType.RPM, 673.75, payload.getValues()[1]);
    }

    @Test
    public void decodeClt() {
        CANPacket packet1D0 = new CANPacket(1,
                0x1D0, new byte[]{0x62, 0x41, (byte) 0xBB, (byte) 0xC6, 0x7B, 0x5F, 0x3D, (byte) 0x88});
        PacketPayload payload = new Bmw1D0().decode(packet1D0);
        assertEquals(3, payload.getValues().length);
        assertValue(SensorType.CLT, 50.0, payload.getValues()[0]);
        assertValue(SensorType.MAP, 99.4, payload.getValues()[1]);
        assertValue(SensorType.FUEL_AMOUNT, 24443, payload.getValues()[2]);
    }

    @Test
    public void decodeTorqueRequestEGS() {
        CANPacket packet = new CANPacket(1,
                -1, new byte[]{(byte) 0x9F, 0x01, 0x32, 0x20, 0x23, 0x30, (byte) 0xFF, 0x43});
        PacketPayload payload = Bmw0B5.INSTANCE.decode(packet);
        assertValue(SensorType.GEARBOX_CURRENT_TORQUE, 400.0, payload.getValues()[0]);
        assertValue(SensorType.GEARBOX_TORQUE_CHANGE_REQUEST, 2, payload.getValues()[1]);

    }

    private void assertValue(SensorType expectedType, double v, SensorValue value) {
        assertEquals(expectedType, value.getType());
        assertEquals(v, value.getValue(), 0.01);
    }
}
