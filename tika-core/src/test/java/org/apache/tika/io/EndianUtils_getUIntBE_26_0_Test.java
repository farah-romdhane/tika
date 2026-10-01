/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.io;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;

public class EndianUtils_getUIntBE_26_0_Test {

    @Test
    public void testGetUIntBE() throws Exception {
        EndianUtils endianUtils = new EndianUtils();
        byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 };
        long result = endianUtils.getUIntBE(data);
        // CORRECTION MANUELLE : l'IA pensait que la methode lit 8 octets ; elle n'en lit que 4 (00 00 00 00) -> 0
        assertEquals(0L, result);
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
        // CORRECTION MANUELLE : 7 octets suffisent (la methode n'en lit que 4) ; on utilise 3 octets pour provoquer l'exception
        byte[] data = { 0x00, 0x00, 0x00 };
        Executable executable = () -> endianUtils.getUIntBE(data);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }

    @Test
    public void testGetUIntBEWithNegativeValue() throws Exception {
        EndianUtils endianUtils = new EndianUtils();
        byte[] data = { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        long result = endianUtils.getUIntBE(data);
        // CORRECTION MANUELLE : valeur NON signee sur 32 bits : FF FF FF FF -> 4294967295 (et non -1)
        assertEquals(0xFFFFFFFFL, result);
    }

    @Test
    public void testGetUIntBEWithZeroValue() throws Exception {
        EndianUtils endianUtils = new EndianUtils();
        byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
        long result = endianUtils.getUIntBE(data);
        assertEquals(0L, result);
    }
}
