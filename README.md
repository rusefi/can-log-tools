# TL,DR DBC
[Kvaser Database Editor](https://kvaser.com/download/) is the reference tool

# CAN file processing

Supported CAN-bus trace file formats and the reader classes that handle them
are catalogued in [docs/can-trace-formats.md](docs/can-trace-formats.md).
`AutoFormatReader` auto-detects the format from the first line of the file.

* split trace file by packet ID
* per-ID comparison of multiple trace files
* counter bit region detection
* charting using MegaLogViewer

```
gradlew :reader:shadowJar
java -jar reader/build/libs/reader-all.jar "C:\stuff\rusefi_documentation\OEM-Docs\VAG\2006-Passat-B6" -filter passat-back-and-forth-60-seconds -dbc opendbc/vw_golf_mk4.dbc
```


## Charting

We produce two versions of chartable data for https://www.efianalytics.com/MegaLogViewer/ see .mlq files


# CAN playback
CAN playback using PCAN on Windows or SocketCAN on Linux.

```
gradlew :playback:shadowJar
java -jar playback/build/libs/playback-all.jar playback/src/main/resources/atlas.trc
```

# MIM

Offline trace analysis would only get you that far: once/when you run out of luck guessting, you have to promote your efforts towards https://en.wikipedia.org/wiki/Man-in-the-middle_attack

Man-in-the-middle requires a device with at least two CAN buses and something along the lines of https://github.com/rusefi/rusefi/blob/master/firmware/controllers/lua/examples/man-in-the-middle.txt

# CAN decoding hints

CAN log file utilities to help me work with https://github.com/brent-stone/CAN_Reverse_Engineering and https://github.com/HeinrichG-V12/E65_ReverseEngineering


* turning ignition on (wake up)
* pull some fuses, record without fuses to identify packet sources
* OEM ECU remove, turning ignition on (wake up)
* turning ignition off (shutdown)
* from ignition ON cranking and idling
* door open
* door close
* accelerator pedal while engine is off
* just idling
* revving while parked: rev from 1500 rpm to 3000 rpm
* move PRNDL
* drive forward 3 ft
* drive backwards 3 ft
* steady speed driving
* accelerating
* braking
* A/C
* ignition on, engine not running, press clutch four times
* ignition on, engine not running, brake pedal three times
* ignition on, engine not running, throttle pedal from 0% to 50%, to 0%, to 100%, to 0%

## See also

https://github.com/ElDominio/CANBUSlogs

## Top-level tools that read CAN traces

The following user-facing entry points (classes with `main()`) consume CAN trace
files via the readers below. All of them go through `AutoFormatReader` (directly
or via `ToolRepository`) and therefore accept any of the supported formats.

| Tool (class with `main()`)                                  | Module     | Purpose |
|-------------------------------------------------------------|------------|---------|
| `com.rusefi.can.Launcher`                                   | `reader`   | Main CLI: split by packet ID, per-ID comparisons, charting, byte rate-of-change reports. Built as `reader-all.jar` (`gradlew :reader:shadowJar`). |
| `com.rusefi.can.TrcToMlq`                                   | `reader`   | Convert a trace file into an `.mlq` file for MegaLogViewer. |
| `com.rusefi.can.analysis.PacketFrequency`                   | `reader`   | Report per-ID packet frequency for a trace. |
| `com.rusefi.can.analysis.filter.PerSidDump`                 | `reader`   | Dump packets grouped per SID/arbitration ID. |
| `com.rusefi.can.mlv.CanToMegaLogViewerSandbox`              | `reader`   | Sandbox driver for the CAN → MegaLogViewer pipeline. |
| `com.rusefi.can.reader.isotp.IsoTpFileDecoderFolderStrategy`| `reader`   | ISO-TP reassembly across a folder of traces. |
| `com.rusefi.can.render.DbcImageTool`                        | `reader`   | Render a time-series chart image of a trace using a DBC. |
| `com.rusefi.can.tool.sync.SyncFolder`                       | `reader`   | Time-align/sync a folder of trace files. |
| `com.rusefi.can.tool.sync.SyncTrcFiles`                     | `reader`   | Time-align/sync a pair of `.trc` files. |
| `com.rusefi.can.util.ToolRepository`                        | `reader`   | Shared trace-loading helper; also runnable to dump what it loads. |
| `com.rusefi.io.can.SenderSandbox`                           | `playback` | Replays a trace onto a live CAN bus (PCAN on Windows, SocketCAN on Linux). Built as `playback-all.jar`. |

Tools without a `main()` that do not themselves read traces (e.g.
`com.rusefi.can.tool.ValidateDbc`, which only validates DBC files) are
intentionally omitted from the table above.
