package org.apache.tika.io;

import org.apache.tika.io.EndianUtils;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class EndianUtils_getUShortBE_19_0_Test {

    @Test
    public void testGetUShortBE() throws IOException, TikaException {
        byte[] data = { (byte) 0x12, (byte) 0x34 };
        int offset = 0;
        int expected = 0x1234;
        int result = EndianUtils.getUShortBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUShortBEWithOffset() throws IOException, TikaException {
        byte[] data = { (byte) 0x56, (byte) 0x78, (byte) 0x9A, (byte) 0xBC };
        int offset = 2;
        int expected = 0x9A9B;
        int result = EndianUtils.getUShortBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUShortBEWithNegativeOffset() throws IOException, TikaException {
        byte[] data = { (byte) 0x12, (byte) 0x34 };
        int offset = -1;
        Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> {
            EndianUtils.getUShortBE(data, offset);
        });
        assertEquals("Index 1 out of bounds for length 2", exception.getMessage());
    }

    @Test
    public void testGetUShortBEWithTooLargeOffset() throws IOException, TikaException {
        byte[] data = { (byte) 0x12, (byte) 0x34 };
        int offset = 2;
        Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> {
            EndianUtils.getUShortBE(data, offset);
        });
        assertEquals("Index 2 out of bounds for length 2", exception.getMessage());
    }
}
