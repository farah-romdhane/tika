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

public class EndianUtils_getUIntBE_27_0_Test {

    @Test
    public void testGetUIntBE() throws IOException, TikaException {
        byte[] data = new byte[] { 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00 };
        int offset = 0;
        long expected = 1L;
        long result = EndianUtils.getUIntBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUIntBEWithNegativeNumber() throws IOException, TikaException {
        byte[] data = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFE, 0x00, 0x00, 0x00, 0x00 };
        int offset = 0;
        long expected = 4294967294L;
        long result = EndianUtils.getUIntBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUIntBEWithMaxValue() throws IOException, TikaException {
        byte[] data = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        int offset = 0;
        long expected = 4294967295L;
        long result = EndianUtils.getUIntBE(data, offset);
        assertEquals(expected, result);
    }
}
