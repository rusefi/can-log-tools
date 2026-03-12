package com.rusefi.can.analysis.matcher;

import com.rusefi.can.dbc.DbcField;

public class Match {
    public DbcField f1;
    public DbcField f2;
    public double distance;

    Match(DbcField f1, DbcField f2, double distance) {
        this.f1 = f1;
        this.f2 = f2;
        this.distance = distance;
    }
}
