package com.rusefi.can.reader.dbc;

import com.rusefi.can.reader.dbc.DbcField;

import java.util.ArrayList;
import java.util.List;

public class DbcPacket {
    private final int id;
    private final String name;
    private final List<DbcField> fields = new ArrayList<>();

    public DbcPacket(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "DbcPacket{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public void add(DbcField dbcField) {
        fields.add(dbcField);
    }

    public DbcField find(String name) {
        for (DbcField field : fields) {
            if (field.getName().equalsIgnoreCase(name))
                return field;
        }
        return null;
    }
}
