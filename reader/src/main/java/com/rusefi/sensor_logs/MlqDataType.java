package com.rusefi.sensor_logs;

import java.io.DataInputStream;
import java.io.IOException;

public enum MlqDataType {
    U08,
    S08,
    U16,
    S16,
    U32,
    S32,
    S64,
    F32;

    public static MlqDataType findByOrdinal(int typeCode) {
        for (MlqDataType value : values())
            if (value.ordinal() == typeCode)
                return value;
        throw new IllegalStateException("Unexpected typeCode " + typeCode);
    }

    public int getRecordSize() {
        switch (this) {
            case U08:
            case S08:
                return 1;
            case U16:
            case S16:
                return 2;
            case U32:
            case S32:
            case F32:
                return 4;
            case S64:
            default:
                throw new UnsupportedOperationException("getRecordSize " + this);
        }
    }

    public float read(DataInputStream bis) throws IOException {
        switch (this) {
            case U08:
            case S08:
                return bis.readByte();
            case U16:
            case S16:
                return bis.readShort();
            case U32:
            case S32:
                return bis.readInt();
            case F32:
                return bis.readFloat();
            case S64:
            default:
                throw new UnsupportedOperationException("read " + this);
        }
    }
}
