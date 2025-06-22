package com.rusefi.sensor_logs;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Objects;

public class Record {
    private final String fieldName;
    private final MlqDataType type;
    private final float scale;

    public Record(String fieldName, MlqDataType type, float scale) {
        this.fieldName = fieldName;
        this.type = type;
        this.scale = scale;
    }

    public String getFieldName() {
        return fieldName;
    }

    public MlqDataType getType() {
        return type;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Record record = (Record) o;
        return Objects.equals(fieldName, record.fieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fieldName);
    }

    public float read(DataInputStream bis) throws IOException {
        return type.read(bis) * scale;
    }
}
