package com.rusefi.sensor_logs;

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
}
