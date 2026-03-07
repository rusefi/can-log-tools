package com.rusefi.can.dbc;

public class J1939Logic {
    public static boolean gmlanIgnoreSender = false;
    public static boolean j1939_mode = false;

    // GMLAN specific: leave the only ArbID, trim priority and sender fields
    // J1939 specific: trim source and destination (if any)
    public static int trimSid(int sid) {
        boolean longId = (sid > 0x7FF);
        if (gmlanIgnoreSender && longId)
            return (sid & 0x03FF_FE00);
        else if (j1939_mode && longId) {
            int pduFormat = (sid >> 16) & 0xFF;
            if (pduFormat < 0xF0) // PDU1 - peer to peer
                return (sid & 0x03FF_0000);
            else // PDU2 - broadcast
                return (sid & 0x03FF_FF00);
        }
        else {
            return sid;
        }
    }
}
