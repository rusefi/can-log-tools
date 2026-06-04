package com.rusefi.can.reader;

import com.rusefi.can.TraceToMlqConverterTool;

public enum ReaderTypeHolder {
    INSTANCE;

    public ReaderType type;

    public ReaderType getType() {
        if (type == null)
            type = TraceToMlqConverterTool.parseCurrentReaderTypeSetting();
        return type;
    }
}
