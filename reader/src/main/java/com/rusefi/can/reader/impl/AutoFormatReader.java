package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.CANLineReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class AutoFormatReader implements CANLineReader {
    public static final AutoFormatReader INSTANCE = new AutoFormatReader();

    private CANLineReader delegate;

    @Override
    public CANPacket readLine(String line) {
        return delegate.readLine(line);
    }

    @Override
    public CANPacket readLine(String line, String fileName, int lineIndex) {
        return delegate.readLine(line, fileName, lineIndex);
    }

    @Override
    public List<CANPacket> readFile(String fileName) throws IOException {
        String firstLine = Files.lines(Paths.get(fileName)).findFirst().get();
        detectReader(firstLine);
        try {
            return delegate.readFile(fileName);
        } catch (Throwable e) {
            throw new IllegalStateException("While " + fileName, e);
        }
    }

    public void detectReader(String firstLine) {
        if (!firstLine.contains(PcanTrcReader2_0.FILEVERSION)
                && !firstLine.contains(CanHackerReader.HEADER)
                && !firstLine.contains(IxxatReader.START_TIME)
                && !firstLine.contains(IxxatReader.BusNo)
                && !firstLine.contains(SomethingLinuxReader.HEADER)
                && !firstLine.contains(BusMasterReader.HEADER)
                && !firstLine.equals(EcuMasterDongleReader.HEADER)
        )
            throw new IllegalStateException("Unexpected first line");
        if (firstLine.contains(CanHackerReader.HEADER)) {
            delegate = CanHackerReader.INSTANCE;
        } else if (firstLine.startsWith(IxxatReader.START_TIME) || firstLine.startsWith(IxxatReader.BusNo)     ) {
            delegate = IxxatReader.INSTANCE;
        } else if (firstLine.contains(SomethingLinuxReader.HEADER)) {
            delegate = SomethingLinuxReader.INSTANCE;
        } else if (firstLine.contains(BusMasterReader.HEADER)) {
            delegate = BusMasterReader.INSTANCE;
        } else if (firstLine.contains("1.1")) {
            delegate = PcanTrcReader1_1.INSTANCE;
        } else if (firstLine.contains("2.0")) {
            delegate = PcanTrcReader2_0.INSTANCE;
        } else {
            throw new IllegalStateException("Unsupported version in " + firstLine);
        }
    }

    @Override
    public List<CANPacket> skipHeaderAndRead(String fileName, int skipCount) throws IOException {
        return delegate.skipHeaderAndRead(fileName, skipCount);
    }
}
