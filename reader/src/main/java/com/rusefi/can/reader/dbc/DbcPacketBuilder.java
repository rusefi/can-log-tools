package com.rusefi.can.reader.dbc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DbcPacketBuilder {
    private final int packetId;
    private final String packetName;
    private final List<DbcField> signals = new ArrayList<>();
    private boolean isConsumed;

    public DbcPacketBuilder(int packetId, String packetName) {
        this.packetId = packetId;
        this.packetName = packetName;
    }

    public int getPacketId() {
        return packetId;
    }

    public String getPacketName() {
        return packetName;
    }

    public void add(DbcField field) {
        if (isConsumed)
            throw new IllegalStateException();
        signals.add(field);
    }

    public List<DbcField> getSignals() {
        return Collections.unmodifiableList(signals);
    }

    public void markConsumed() {
        isConsumed = true;
    }

    public boolean isConsumed() {
        return isConsumed;
    }
}
