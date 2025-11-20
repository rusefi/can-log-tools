package com.rusefi.can.reader.isotp;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class UDSDecoderTest {

    /**
     * Tests the handle method for a valid RequestDownload scenario.
     */
    @Test
    public void testHandleRequestDownloadValid() throws IOException {
        File outputDir = new File("output");
        UDSDecoder decoder = new UDSDecoder(outputDir);

        // Example payload: [0x34, 0x00, 0x12, 0x00, 0x00, 0x01, 0x00, 0x00, 0x04]
        byte[] payload = new byte[]{
                (byte) 0x34, // RequestDownload
                0x00, // Subfunction
                0x12, // Address/length format identifier: addrLen = 1, sizeLen = 2
                0x10, // Address
                0x00, (byte) 0x80 // Size = 128 bytes
        };

        decoder.handle(payload);

        // Validate the UDSDecoder state
        assertTrue(decoder.downloadActive);
        assertEquals(0x10, decoder.currentAddress);
        assertEquals(128, decoder.expectedSize);
        assertEquals("full-addr_0x00000010-size_0x80.bin", decoder.outputFileName);
    }

    /**
     * Tests the handle method for a RequestDownload with invalid length.
     */
    @Test
    public void testHandleRequestDownloadInvalid() throws IOException {
        File outputDir = new File("output");
        UDSDecoder decoder = new UDSDecoder(outputDir);

        // Invalid payload: too short
        byte[] payload = new byte[]{(byte) 0x34};

        decoder.handle(payload);

        // Validate that the state was not modified
        assertFalse(decoder.downloadActive);
        assertEquals(-1, decoder.expectedSize);
        assertNull(decoder.outputFileName);
    }

    /**
     * Tests the handle method for a valid TransferData scenario.
     */
    @Test
    public void testHandleTransferDataValid() throws IOException {
        File outputDir = new File("output");
        UDSDecoder decoder = new UDSDecoder(outputDir);

        // Initialize the decoder with a valid RequestDownload
        byte[] downloadPayload = new byte[]{
                (byte) 0x34, 0x00, 0x12, 0x10, 0x00, (byte) 0x80
        };
        decoder.handle(downloadPayload);

        assertTrue(decoder.downloadActive);

        // Valid TransferData payload
        byte[] transferPayload = new byte[]{(byte) 0x36, 0x01, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC};
        decoder.handle(transferPayload);

        // Validate internal state and buffer content
        assertEquals(3, decoder.receivedSize);
    }

    /**
     * Tests the handle method for a TransferData with out of order sequence.
     */
    @Test
    public void testHandleOutOfOrderTransferData() throws IOException {
        File outputDir = new File("output");
        UDSDecoder decoder = new UDSDecoder(outputDir);

        // Initialize the decoder with a valid RequestDownload
        byte[] downloadPayload = new byte[]{
                (byte) 0x34, 0x00, 0x12, 0x10, 0x00, (byte) 0x80
        };
        decoder.handle(downloadPayload);

        assertTrue(decoder.downloadActive);

        // Out-of-order TransferData payloads
        byte[] seq2 = new byte[]{(byte) 0x36, 0x02, (byte) 0xCC, (byte) 0xDD};
        byte[] seq1 = new byte[]{(byte) 0x36, 0x01, (byte) 0xAA, (byte) 0xBB};

        decoder.handle(seq2); // Out of order
        decoder.handle(seq1); // Now in order

        // Assert data was added in correct order
        assertEquals(4, decoder.receivedSize);
        assertTrue(decoder.pendingBySeq.isEmpty());
    }

    /**
     * Tests the handle method for a valid RequestTransferExit.
     */
    @Test
    public void testHandleRequestTransferExit() throws IOException {
        File outputDir = new File("output");
        UDSDecoder decoder = new UDSDecoder(outputDir);

        // Initialize with a RequestDownload followed by TransferData
        byte[] downloadPayload = new byte[]{
                (byte) 0x34, 0x00, 0x12, 0x10, 0x00, (byte) 0x80
        };
        decoder.handle(downloadPayload);

        assertTrue(decoder.downloadActive);

        byte[] transferPayload = new byte[]{(byte) 0x36, 0x01, (byte) 0xAA, (byte) 0xBB};
        decoder.handle(transferPayload);

        assertEquals(2, decoder.receivedSize);

        // End the session with RequestTransferExit
        byte[] exitPayload = new byte[]{(byte) 0x37};
        decoder.handle(exitPayload);

        // Validate that the session has ended
        assertFalse(decoder.downloadActive);
        assertEquals(-1, decoder.expectedSize);
    }

    /**
     * Tests that invalid SID bytes are ignored by the handle method.
     */
    @Test
    public void testHandleInvalidSid() throws IOException {
        File outputDir = new File("output");
        UDSDecoder decoder = new UDSDecoder(outputDir);

        // Send a payload with invalid SID
        byte[] payload = new byte[]{(byte) 0x99, 0x00};
        decoder.handle(payload);

        // Validate that no state was modified
        assertFalse(decoder.downloadActive);
        assertEquals(-1, decoder.expectedSize);
        assertNull(decoder.outputFileName);
    }
}