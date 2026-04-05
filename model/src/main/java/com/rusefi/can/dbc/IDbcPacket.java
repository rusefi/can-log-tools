package com.rusefi.can.dbc;

public interface IDbcPacket extends FileNameProvider {
    String getSource();

    int MAX_PACKET_SIZE = 8;
}
