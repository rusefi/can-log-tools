package com.rusefi.can.reader;

import com.rusefi.can.TrcToMlqConverterTool;

public enum ReaderTypeHolder {
    INSTANCE;

    public ReaderType type;

    public ReaderType getType() {
        if (type == null)
            type = TrcToMlqConverterTool.parseCurrentReaderTypeSetting();
        return type;
    }
}
