# CAN trace formats catalog

This is the shared catalog of CAN-bus trace file formats supported by this
repository. Every reader listed here lives under
`reader/src/main/java/com/rusefi/can/reader/impl/` and implements
`com.rusefi.can.reader.CANLineReader`.

Whenever a new trace format reader is added, please update this catalog so we
keep a single source of truth.

## Universal entry point

`AutoFormatReader` inspects the first line of the file and dispatches to the
appropriate concrete reader. Prefer using it unless you have a specific reason
to pick a reader manually.

## Supported formats

| Format                  | Typical extension | First-line / signature                  | Reader class             | Notes |
|-------------------------|-------------------|-----------------------------------------|--------------------------|-------|
| PEAK PCAN trace v1.1    | `.trc`            | `;$FILEVERSION=1.1`                     | `PcanTrcReader1_1`       | PEAK-System PCAN-View / PCAN-Explorer export. |
| PEAK PCAN trace v2.0    | `.trc`            | `;$FILEVERSION=2.0`                     | `PcanTrcReader2_0`       | Newer PEAK trace format. |
| Vector CANoe ASCII      | `.asc`            | CANoe `.asc` log                        | `CANoeReader`            | Vector CANoe/CANalyzer ASCII export. |
| BusMaster log           | `.log`            | starts with `BUSMASTER Ver 3`           | `BusMasterReader`        | BUSMASTER open-source CAN tool. |
| CanHacker               | text              | starts with `@ TEXT`                    | `CanHackerReader`        | CanHacker dongle text log. |
| EcuMaster ADU dongle    | text              | first line is `;`                       | `EcuMasterDongleReader`  | EcuMaster CAN dongle export. |
| IXXAT                   | text              | starts with `Start Time` or `"Bus","No"`| `IxxatReader`            | IXXAT canAnalyser export. |
| SocketCAN (candump)     | text              | contains `) can0 `                      | `SomethingLinuxReader`   | Linux SocketCAN `candump` output. |
| Auto-detect             | any of the above  | —                                       | `AutoFormatReader`       | Sniffs the first line and delegates. |

## Adding a new reader

1. Implement `CANLineReader` under `reader/src/main/java/com/rusefi/can/reader/impl/`.
2. Expose a signature constant (e.g. `public static final String HEADER = ...`)
   so that `AutoFormatReader#detectReader` can recognize the format.
3. Register the new branch in `AutoFormatReader#detectReader`.
4. Add a row to the table above.
