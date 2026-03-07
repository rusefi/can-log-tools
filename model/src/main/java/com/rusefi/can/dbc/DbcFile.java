package com.rusefi.can.dbc;

import java.util.*;
import java.util.function.Function;

public class DbcFile implements FileNameProvider {
    private final LinkedHashMap<Integer, com.rusefi.can.dbc.DbcPacket> packets = new LinkedHashMap<>();
    private String fileName;

    @Override
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public static final boolean debugEnabled = false;

    public int size() {
        return packets.size();
    }

    public void addPacket(com.rusefi.can.dbc.DbcPacket packet) {
        packets.put(packet.getId(), packet);
    }

    public com.rusefi.can.dbc.DbcPacket findPacket(int canId) {
        return packets.get(com.rusefi.can.dbc.J1939Logic.trimSid(canId));
    }


    public com.rusefi.can.dbc.DbcPacket getPacketByIndexSlow(int index) {
        return new ArrayList<>(packets.values()).get(index);
    }

    public com.rusefi.can.dbc.DbcPacket getPacket(int sid) {
        int trimmedSid = com.rusefi.can.dbc.J1939Logic.trimSid(sid);
        return packets.computeIfAbsent(trimmedSid, new Function<Integer, com.rusefi.can.dbc.DbcPacket>() {
            @Override
            public com.rusefi.can.dbc.DbcPacket apply(Integer integer) {
                String packetName = Integer.toHexString(sid) + "_" + sid;
                String packetPrefix = "_unknown_" + sid;
                com.rusefi.can.dbc.DbcPacket packet = new com.rusefi.can.dbc.DbcPacket(sid, packetName, "unknown", new com.rusefi.can.dbc.util.GapFactory(Collections.emptyList(), packetPrefix).withGaps(sid), DbcFile.this);
                return packet;
            }
        });
    }

    public Collection<com.rusefi.can.dbc.DbcPacket> values() {
        return packets.values();
    }

}
