package org.apache.tika.io;

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

public class EndianUtils_getIntBE_23_0_Test {

    @Test
    public void testGetIntBE() throws Exception {
        EndianUtils endianUtils = new EndianUtils();
        Method method = EndianUtils.class.getDeclaredMethod("getIntBE", byte[].class, int.class);
        method.setAccessible(true);
        byte[] data = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        int result = (int) method.invoke(endianUtils, data, 0);
        assertEquals(0x01020304, result);
        result = (int) method.invoke(endianUtils, data, 4);
        assertEquals(0x05060708, result);
    }
}
