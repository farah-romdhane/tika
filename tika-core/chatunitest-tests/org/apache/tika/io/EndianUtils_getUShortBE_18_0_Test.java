package org.apache.tika.io;

import org.apache.tika.io.EndianUtils;
import java.io.IOException;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;

public class EndianUtils_getUShortBE_18_0_Test {

    @Test
    public void testGetUShortBE() throws IOException {
        byte[] data = { 0x01, 0x02 };
        int result = EndianUtils.getUShortBE(data);
        assertEquals(258, result);
    }

    @Test
    public void testGetUShortBEWithOffset() throws IOException {
        byte[] data = { 0x01, 0x02, 0x03, 0x04 };
        int result = EndianUtils.getUShortBE(data, 1);
        assertEquals(258, result);
    }

    @Test
    public void testGetUShortBEWithEmptyArray() throws IOException {
        byte[] data = {};
        try {
            EndianUtils.getUShortBE(data);
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected exception
        }
    }

    @Test
    public void testGetUShortBEWithNegativeOffset() throws IOException {
        byte[] data = { 0x01, 0x02 };
        try {
            EndianUtils.getUShortBE(data, -1);
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected exception
        }
    }

    @Test
    public void testGetUShortBEWithOffsetGreaterThanArrayLength() throws IOException {
        byte[] data = { 0x01, 0x02 };
        try {
            EndianUtils.getUShortBE(data, 2);
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected exception
        }
    }
}
