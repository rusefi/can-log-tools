package com.rusefi.can.reader.isotp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles UDS download session logic.
 * Writes binary files
 * @see IsoTpFileDecoder
 */
public class UDSDecoder {
    public static final int RequestDownload = 0x34;
    public static final int TransferData = 0x36;
    private final ByteArrayOutputStream downloadBuffer = new ByteArrayOutputStream(1024 * 1024);
    final Map<Integer, byte[]> pendingBySeq = new HashMap<>(); // out-of-order buffer
    private final File outputDir;
    // UDS download session tracking state
    boolean downloadActive = false;
    long currentAddress = 0;
    long expectedSize = -1;
    long receivedSize = 0;

    private static AtomicInteger counter = new AtomicInteger();

    String outputFileName = null;
    // TransferData order handling
    private int expectedSeq = -1; // -1 means unknown/not started; otherwise 0..255
    private Integer lastFlushedSeq = null; // the last sequence actually appended
    private boolean outOfOrderEncountered = false;

    public UDSDecoder(File outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * Increments the given value by 1 and returns the result modulo 256.
     */
    private static int inc8(int v) {
        return (v + 1) & 0xFF;
    }

    /**
     * Handle an ISO-TP payload potentially containing a UDS service request.
     */
    public void handle(byte[] payload) throws IOException {
        if (payload == null || payload.length == 0) return;

        int sid = payload[0] & 0xFF;
        switch (sid) {
            case RequestDownload: // RequestDownload
                handleRequestDownload(payload);
                break;
            case TransferData: // TransferData
                handleTransferData(payload);
                break;
            case 0x37: // RequestTransferExit
                handleRequestTransferExit();
                break;
            default:
                // ignore other SIDs
                break;
        }
    }

    private void handleRequestDownload(byte[] payload) {
        if (payload.length < 3) return;
        int alfi = payload[2] & 0xFF; // address/length format identifier
        int addrLen = (alfi >> 4) & 0xF;
        int sizeLen = (alfi) & 0xF;
        int minLen = 3 + addrLen + sizeLen;
        if (payload.length >= minLen && addrLen > 0 && sizeLen > 0) {
            long addr = 0;
            for (int i = 0; i < addrLen; i++) {
                addr = (addr << 8) | (payload[3 + i] & 0xFF);
            }
            long size = 0;
            for (int i = 0; i < sizeLen; i++) {
                size = (size << 8) | (payload[3 + addrLen + i] & 0xFF);
            }
            downloadActive = true;
            currentAddress = addr;
            expectedSize = size;
            receivedSize = 0;
            downloadBuffer.reset();
            outputFileName = String.format("full-addr_0x%08X-size_0x%s.bin", (int) (addr & 0xFFFFFFFFL), Long.toHexString(size).toUpperCase());
            // Reset ordering state
            expectedSeq = -1;
            lastFlushedSeq = null;
            pendingBySeq.clear();
            outOfOrderEncountered = false;
            System.out.printf("[UDS][RequestDownload] 0x34 addr=0x%08X size=0x%s -> %s\n", (int) (addr & 0xFFFFFFFFL), Long.toHexString(size).toUpperCase(), outputFileName);
        }
    }

    private void appendData(byte[] data) {
        downloadBuffer.write(data, 0, data.length);
        receivedSize += data.length;
    }

    private void tryDrainInOrder() {
        // Keep draining while the next expected sequence is present in the buffer
        while (expectedSeq != -1 && pendingBySeq.containsKey(expectedSeq)) {
            byte[] next = pendingBySeq.remove(expectedSeq);
            appendData(next);
            lastFlushedSeq = expectedSeq;
            expectedSeq = inc8(expectedSeq);
        }
    }

    private void handleTransferData(byte[] payload) {
        if (!downloadActive || payload.length < 2) return;
        int blockSeq = payload[1] & 0xFF;
        int dataOffset = 2; // default: skip block sequence counter only
        int dataLen = payload.length - dataOffset;
        if (dataLen <= 0) return;

        byte[] data = Arrays.copyOfRange(payload, dataOffset, payload.length);

        // First block encountered in this session
        if (expectedSeq == -1) {
            if (blockSeq == 1) {
                // Normal case: starts at 1, append immediately
                appendData(data);
                lastFlushedSeq = blockSeq;
                expectedSeq = inc8(blockSeq);
                System.out.printf("[UDS][TransferData] 0x36 seq=%d (start) stream+=0x%X total=0x%X\n", blockSeq, dataLen, receivedSize);
                tryDrainInOrder();
            } else {
                // Buffer it and wait for earlier blocks to arrive to preserve order.
                outOfOrderEncountered = true;
                if (!pendingBySeq.containsKey(blockSeq)) {
                    pendingBySeq.put(blockSeq, data);
                    System.out.printf("[UDS][TransferData][OutOfOrderStart] 0x36 seq=%d buffered as first (waiting for earlier). Pending=%d\n", blockSeq, pendingBySeq.size());
                }
                // Initialize expectedSeq as the first seen for now; it may be adjusted down if an earlier seq arrives
                expectedSeq = blockSeq;
            }
            return;
        }

        // Duplicate of the most recently flushed block, Ignore.
        if (lastFlushedSeq != null && blockSeq == lastFlushedSeq) {
            System.out.printf("[UDS][TransferData][Duplicate] 0x36 seq=%d ignored (duplicate of last).\n", blockSeq);
            return;
        }

        // If nothing has been flushed yet, and we observe an earlier sequence than currently expected,
        // adjust the expected starting point down so we can drain in correct order.
        // ie, we received first the seq 2, and now the next seq is 1 (see testHandleOutOfOrderTransferData)
        if (lastFlushedSeq == null && (blockSeq & 0xFF) < (expectedSeq & 0xFF)) {
            expectedSeq = blockSeq & 0xFF;
        }

        if (blockSeq == expectedSeq) {
            // append and advance, then drain any pending in-order
            appendData(data);
            lastFlushedSeq = blockSeq;
            expectedSeq = inc8(expectedSeq);
            System.out.printf("[UDS][TransferData] 0x36 seq=%d stream+=0x%X total=0x%X\n", blockSeq, dataLen, receivedSize);
            tryDrainInOrder();
        } else {
            // Out of order: buffer it if we don't have it yet
            outOfOrderEncountered = true;
            if (!pendingBySeq.containsKey(blockSeq)) {
                pendingBySeq.put(blockSeq, data);
                System.out.printf("[UDS][TransferData][OutOfOrder] 0x36 seq=%d buffered (expected %d). Pending=%d\n", blockSeq, expectedSeq, pendingBySeq.size());
            } else {
                System.out.printf("[UDS][TransferData][Duplicate] 0x36 seq=%d ignored (already buffered).\n", blockSeq);
            }
        }
    }

    private void handleRequestTransferExit() throws IOException {
        if (!downloadActive) return;

        if (expectedSize >= 0 && expectedSize != receivedSize) {
            // todo: do not print scary message when we know that compressed content?!
            System.out.printf(
                    "\u001B[33m[UDS][RequestTransferExit][WARN] maybe compressed content? expectedSize=0x%s (%d) but receivedSize=0x%s (%d). File may be incomplete or oversized.\n \033[0m",
                    Long.toHexString(expectedSize).toUpperCase(), expectedSize,
                    Long.toHexString(receivedSize).toUpperCase(), receivedSize
            );
        }

        // If we still have pending out-of-order blocks, flush them now in best-effort order starting at expectedSeq
        if (!pendingBySeq.isEmpty() && expectedSeq != -1) {
            System.out.printf("[UDS][RequestTransferExit] Flushing %d buffered out-of-order blocks starting from seq=%d\n", pendingBySeq.size(), expectedSeq);
            for (int i = 0; i < 256; i++) {
                int seq = (expectedSeq + i) & 0xFF;
                byte[] data = pendingBySeq.remove(seq);
                if (data != null) {
                    appendData(data);
                    lastFlushedSeq = seq;
                }
                if (pendingBySeq.isEmpty()) break;
            }
            // If anything still remains (shouldn't), append in arbitrary order
            if (!pendingBySeq.isEmpty()) {
                for (Map.Entry<Integer, byte[]> e : pendingBySeq.entrySet()) {
                    appendData(e.getValue());
                }
                pendingBySeq.clear();
            }
        }

        byte[] toWrite = downloadBuffer.toByteArray();
        long finalSize = toWrite.length;
        File out = new File(outputDir, counter.incrementAndGet() + "_" + outputFileName);
        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(toWrite);
        } catch (IOException e) {
            System.err.printf("[UDS][RequestTransferExit][ERROR] Failed to write to file %s: %s\n", out.getAbsolutePath(), e.getMessage());
        }
        System.out.printf("[UDS][RequestTransferExit] 0x37 wrote %d bytes to %s (expected 0x%s)%s\n",
                toWrite.length,
                out.getAbsolutePath(),
                expectedSize > 0 ? Long.toHexString(expectedSize).toUpperCase() : Long.toHexString(finalSize).toUpperCase(),
                outOfOrderEncountered ? " [reordered]" : "");

        // Reset session
        downloadActive = false;
        currentAddress = 0;
        expectedSize = -1;
        receivedSize = 0;
        downloadBuffer.reset();
        outputFileName = null;
        expectedSeq = -1;
        lastFlushedSeq = null;
        pendingBySeq.clear();
        outOfOrderEncountered = false;
    }
}
