package com.rusefi.can.reader.dbc;

public class DbcField {
    private final String name;
    private final int startOffset;
    private final int length;
    private final double mult;

    public DbcField(String name, int startOffset, int length, double mult) {
        this.name = name;
        this.startOffset = startOffset;
        this.length = length;
        this.mult = mult;
    }

    public String getName() {
        return name;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getLength() {
        return length;
    }

    public double getMult() {
        return mult;
    }

    @Override
    public String toString() {
        return "DbcField{" +
                "name='" + name + '\'' +
                ", startOffset=" + startOffset +
                ", length=" + length +
                ", mult=" + mult +
                '}';
    }
}
