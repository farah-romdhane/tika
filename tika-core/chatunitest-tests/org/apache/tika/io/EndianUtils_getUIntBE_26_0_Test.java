package org.apache.tika.io;

import org.apache.tika.io.EndianUtils;
import org.junit.jupiter.api.function.Executable;
import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class EndianUtils_getUIntBE_26_0_Test {

    @Test
    public void testGetUIntBE() throws Exception {
        EndianUtils endianUtils = new EndianUtils();
        byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 };
        long result = endianUtils.getUIntBE(data);
        assertEquals(1L, result);
    }

    @Test
    public void testGetUIntBEWithOffset() throws Exception {
        EndianUtils endianUtils = new EndianUtils();
        byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 };
        long result = endianUtils.getUIntBE(data, 4);
        assertEquals(1L, result);
    }

    @Test
    public void testGetUIntBEWithNegativeOffset() throws Exception {
        EndianUtils endianUtils = new EndianUtils();
        byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 };
        Executable executable = () -> endianUtils.getUIntBE(data, -1);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }

    @Test
    public void testGetUIntBEWithOffsetGreaterThanLength() throws Exception {
        EndianUtils endianUtils = new EndianUtils();
        byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 };
        Executable executable = () -> endianUtils.getUIntBE(data, 8);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }

    @Test
    public void testGetUIntBEWithNullData() throws Exception {
        EndianUtils endianUtils = new EndianUtils();
        Executable executable = () -> endianUtils.getUIntBE(null);
        assertThrows(NullPointerException.class, executable);
    }

    @Test
    public void testGetUIntBEWithEmptyData() throws Exception {
        EndianUtils endianUtils = new EndianUtils();
        byte[] data = {};
        Executable executable = () -> endianUtils.getUIntBE(data);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }

    @Test
    public void testGetUIntBEWithPartialData() throws Exception {
        EndianUtils endianUtils = new EndianUtils();
        byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
        Executable executable = () -> endianUtils.getUIntBE(data);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }

    @Test
    public void testGetUIntBEWithNegativeValue() throws Exception {
        EndianUtils endianUtils = new EndianUtils();
        byte[] data = { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        long result = endianUtils.getUIntBE(data);
        assertEquals(0xFFFFFFFFFFFFFFFFL, result);
    }

    @Test
    public void testGetUIntBEWithZeroValue() throws Exception {
        EndianUtils endianUtils = new EndianUtils();
        byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
        long result = endianUtils.getUIntBE(data);
        assertEquals(0L, result);
    }
}
