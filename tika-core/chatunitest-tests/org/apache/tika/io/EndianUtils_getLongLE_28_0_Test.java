package org.apache.tika.io;

import org.apache.tika.io.EndianUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;

public class EndianUtils_getLongLE_28_0_Test {

    @Test
    public void testGetLongLE() throws IOException {
        byte[] data = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        int offset = 0;
        long expected = 0x0807060504030201L;
        long result = EndianUtils.getLongLE(data, offset);
        assertEquals(expected, result);
    }
}
