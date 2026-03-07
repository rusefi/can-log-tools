package com.rusefi.can.dbc.reader;

import com.rusefi.can.dbc.DbcField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DbcPacketBuilder {
    private final int packetId;
    private final String packetName;
    private final String source;
    private final List<DbcField> signals = new ArrayList<>();
    private boolean isConsumed;

    public DbcPacketBuilder(int packetId, String packetName, String source) {
        this.packetId = packetId;
        this.packetName = packetName;
        this.source = source;
    }

    public int getPacketId() {
        return packetId;
    }

    public String getPacketName() {
        return packetName;
    }

    public String getSource() {
        return source;
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
