### Core Classes
`CANPacket` and `DbcFile` are key classes of this repo.
- `CANPacket` represents a single CAN packet/frame.
- `DbcFile` represents a DBC (Database CAN) file.
- `DbcImageTool` renders time series of CANPacket into image
- `ChartImage` encapsulates a BufferedImage for rendering charts
- `AutoFormatReader` universal trace reader (see [docs/can-trace-formats.md](../docs/can-trace-formats.md) for the full catalog of supported CAN trace formats and their readers)

### Code Style
Always use curly braces for `if` statements.

Correct:
```java
if (customStartBit == -1 || customLength <= 0) {
    return false;
}
```

Incorrect:
```java
if (customStartBit == -1 || customLength <= 0) return false;
```
