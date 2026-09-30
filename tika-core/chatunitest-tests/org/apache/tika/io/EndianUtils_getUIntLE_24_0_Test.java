package org.apache.tika.io;

import org.apache.tika.io.EndianUtils;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class EndianUtils_getUIntLE_24_0_Test {

    @Test
    public void testGetUIntLEWithOffset() throws IOException, TikaException {
        byte[] data = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 };
        long result = EndianUtils.getUIntLE(data, 4);
        assertEquals(16777216, result);
    }
}
