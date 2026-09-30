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

import java.io.IOException;

import org.junit.jupiter.api.*;
import org.mockito.*;

import org.apache.tika.exception.TikaException;

public class EndianUtils_getUShortBE_19_0_Test {

    @Test
    public void testGetUShortBE() throws IOException, TikaException {
        byte[] data = { (byte) 0x12, (byte) 0x34 };
        int offset = 0;
        int expected = 0x1234;
        int result = EndianUtils.getUShortBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUShortBEWithOffset() throws IOException, TikaException {
        byte[] data = { (byte) 0x56, (byte) 0x78, (byte) 0x9A, (byte) 0xBC };
        int offset = 2;
        int expected = 0x9A9B;
        int result = EndianUtils.getUShortBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUShortBEWithNegativeOffset() throws IOException, TikaException {
        byte[] data = { (byte) 0x12, (byte) 0x34 };
        int offset = -1;
        Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> {
            EndianUtils.getUShortBE(data, offset);
        });
        assertEquals("Index 1 out of bounds for length 2", exception.getMessage());
    }

    @Test
    public void testGetUShortBEWithTooLargeOffset() throws IOException, TikaException {
        byte[] data = { (byte) 0x12, (byte) 0x34 };
        int offset = 2;
        Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> {
            EndianUtils.getUShortBE(data, offset);
        });
        assertEquals("Index 2 out of bounds for length 2", exception.getMessage());
    }
}
