package org.apache.tika.io;

import org.apache.tika.io.EndianUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.tika.exception.TikaException;

public class EndianUtils_readUE7_11_0_Test {

    @Test
    public void testReadUE7() throws IOException {
        // Test case 1: Normal input
        byte[] input1 = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        InputStream stream1 = new ByteArrayInputStream(input1);
        assertEquals(0x0102030405060708L, EndianUtils.readUE7(stream1));
        // Test case 2: Input with continuation bit
        byte[] input2 = { (byte) 0x81, (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85, (byte) 0x86, (byte) 0x87, (byte) 0x88 };
        InputStream stream2 = new ByteArrayInputStream(input2);
        assertEquals(0x0102030405060708L, EndianUtils.readUE7(stream2));
        // Test case 3: Input with continuation bit and last value
        byte[] input3 = { (byte) 0x81, (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85, (byte) 0x86, (byte) 0x87, (byte) 0x08 };
        InputStream stream3 = new ByteArrayInputStream(input3);
        assertEquals(0x0102030405060708L, EndianUtils.readUE7(stream3));
        // Test case 4: Input with only one byte
        byte[] input4 = { (byte) 0x01 };
        InputStream stream4 = new ByteArrayInputStream(input4);
        assertEquals(0x01L, EndianUtils.readUE7(stream4));
        // Test case 5: Input with only one byte and continuation bit
        byte[] input5 = { (byte) 0x81 };
        InputStream stream5 = new ByteArrayInputStream(input5);
        assertEquals(0x01L, EndianUtils.readUE7(stream5));
        // Test case 6: Input with only one byte and last value
        byte[] input6 = { (byte) 0x01 };
        InputStream stream6 = new ByteArrayInputStream(input6);
        assertEquals(0x01L, EndianUtils.readUE7(stream6));
        // Test case 7: Input with buffer underun
        byte[] input7 = {};
        InputStream stream7 = new ByteArrayInputStream(input7);
        assertThrows(IOException.class, () -> EndianUtils.readUE7(stream7));
    }
}
