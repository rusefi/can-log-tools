package com.rusefi.can.core;

import com.rusefi.can.DualSid;
import com.rusefi.can.dbc.DbcField;

import java.util.Objects;

public class ByteId implements Comparable<ByteId> {
    final int sid;
    final int byteIndex;

    private ByteId(int sid, int byteIndex) {
        this.sid = sid;
        this.byteIndex = byteIndex;
    }

    public static ByteId createByte(int sid, int byteIndex) {
        return new ByteId(sid, byteIndex);
    }

    public static ByteId convert(DbcField dbcField) {
        if (dbcField.getLength() != 8 || dbcField.getStartOffset() % 8 != 0)
            return null;
        return createByte(dbcField.getSid(), dbcField.getByteIndex());
    }

    public String getLogKey() {
        return DualSid.dualSid(sid) + "_byte_" + byteIndex + "_bit_" + (byteIndex * 8);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ByteId byteId = (ByteId) o;
        return sid == byteId.sid && byteIndex == byteId.byteIndex;
    }

    public int getSid() {
        return sid;
    }

    public int getByteIndex() {
        return byteIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sid, byteIndex);
    }

    @Override
    public int compareTo(ByteId o) {
        ByteId other = o;
        if (other.sid != sid)
            return sid - other.sid;
        return byteIndex - other.byteIndex;
    }

    @Override
    public String toString() {
        return getLogKey();
    }
}
