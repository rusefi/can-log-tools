package com.rusefi.can;

import com.rusefi.can.reader.ReaderType;

public enum ReaderTypeHolder {
    INSTANCE;

    ReaderType type;

    public ReaderType getType() {
        if (type == null)
            type = TrcToMlq.parseCurrentReaderTypeSetting();
        return type;
    }
}
