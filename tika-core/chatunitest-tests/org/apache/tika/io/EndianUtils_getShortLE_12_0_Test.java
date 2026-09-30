package org.apache.tika.io;

import org.apache.tika.io.EndianUtils;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;

public class EndianUtils_getShortLE_12_0_Test {

    @Test
    public void testGetShortLE() {
        byte[] data = { (byte) 0x12, (byte) 0x34 };
        short expected = 0x3412;
        short result = EndianUtils.getShortLE(data);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortLEWithOffset() {
        byte[] data = { (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78 };
        short expected = 0x3412;
        short result = EndianUtils.getShortLE(data, 0);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortLEWithOffsetAndPartialData() {
        byte[] data = { (byte) 0x12, (byte) 0x34 };
        short expected = 0x3412;
        short result = EndianUtils.getShortLE(data, 0);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortLEWithEmptyData() {
        byte[] data = {};
        assertThrows(NullPointerException.class, () -> EndianUtils.getShortLE(data));
    }

    @Test
    public void testGetShortLEWithNullData() {
        byte[] data = null;
        assertThrows(NullPointerException.class, () -> EndianUtils.getShortLE(data));
    }
}
