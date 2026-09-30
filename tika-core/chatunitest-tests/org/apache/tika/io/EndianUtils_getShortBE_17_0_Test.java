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

public class EndianUtils_getShortBE_17_0_Test {

    @Test
    public void testGetShortBE() throws IOException, TikaException {
        byte[] data = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        int offset = 0;
        short expected = 0x0100;
        short result = EndianUtils.getShortBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortBEWithOffset() throws IOException, TikaException {
        byte[] data = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        int offset = 2;
        short expected = 0x0302;
        short result = EndianUtils.getShortBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortBEWithNegativeOffset() throws IOException, TikaException {
        byte[] data = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        int offset = -1;
        Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> {
            EndianUtils.getShortBE(data, offset);
        });
        assertEquals("Index 7 out of bounds for length 8", exception.getMessage());
    }

    @Test
    public void testGetShortBEWithLargeOffset() throws IOException, TikaException {
        byte[] data = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        int offset = 8;
        Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> {
            EndianUtils.getShortBE(data, offset);
        });
        assertEquals("Index 8 out of bounds for length 8", exception.getMessage());
    }
}
