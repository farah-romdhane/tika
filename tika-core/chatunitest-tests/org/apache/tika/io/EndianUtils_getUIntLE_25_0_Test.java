package org.apache.tika.io;

import org.apache.tika.io.EndianUtils;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;

public class EndianUtils_getUIntLE_25_0_Test {

    @Test
    public void testGetUIntLE() throws Exception {
        EndianUtils endianUtils = new EndianUtils();
        Method method = EndianUtils.class.getDeclaredMethod("getUIntLE", byte[].class, int.class);
        method.setAccessible(true);
        byte[] data = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        int offset = 0;
        long result = (long) method.invoke(endianUtils, data, offset);
        assertEquals(0x04030201L, result);
    }
}
