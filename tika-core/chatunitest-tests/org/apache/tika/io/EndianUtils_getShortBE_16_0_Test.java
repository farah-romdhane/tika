package org.apache.tika.io;

import org.apache.tika.io.EndianUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;

@ExtendWith(MockitoExtension.class)
public class EndianUtils_getShortBE_16_0_Test {

    @Mock
    private EndianUtils endianUtils;

    @Test
    public void testGetShortBE() throws IOException {
        byte[] data = new byte[] { 0x01, 0x02 };
        short expected = 0x0201;
        when(endianUtils.getUShortBE(data, 0)).thenReturn(0x0201);
        short result = EndianUtils.getShortBE(data);
        assertEquals(expected, result);
        verify(endianUtils, times(1)).getUShortBE(data, 0);
    }
}
