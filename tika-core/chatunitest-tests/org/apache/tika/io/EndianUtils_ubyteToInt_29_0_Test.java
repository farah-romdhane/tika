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

public class EndianUtils_ubyteToInt_29_0_Test {

    @Test
    public void testUbyteToInt() throws IOException {
        byte[] data = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        int result = EndianUtils.ubyteToInt((byte) 0x01);
        assertEquals(1, result);
    }
}
