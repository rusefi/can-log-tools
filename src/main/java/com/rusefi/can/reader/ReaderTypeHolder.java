package com.rusefi.can.reader;

import com.rusefi.can.TrcToMlq;

public enum ReaderTypeHolder {
    INSTANCE;

    public ReaderType type;

    public ReaderType getType() {
        if (type == null)
            type = TrcToMlq.parseCurrentReaderTypeSetting();
        return type;
    }
}
