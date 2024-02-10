package com.rusefi.can.reader;

public enum ReaderType {
    AUTO,
    // todo: do we even need all these types? shall we always auto-detect?
    PCAN,
    CANOE,
    CANHACKER,
}
