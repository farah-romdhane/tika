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

public class EndianUtils_getShortLE_13_0_Test {

    @Test
    public void testGetShortLE() {
        byte[] data = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        int offset = 0;
        short expected = 0x0201;
        short result = EndianUtils.getShortLE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortLEWithNegativeOffset() {
        byte[] data = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        int offset = -1;
        Executable executable = () -> EndianUtils.getShortLE(data, offset);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }

    @Test
    public void testGetShortLEWithOffsetGreaterThanArrayLength() {
        byte[] data = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        int offset = 8;
        Executable executable = () -> EndianUtils.getShortLE(data, offset);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }

    @Test
    public void testGetShortLEWithEmptyArray() {
        byte[] data = {};
        int offset = 0;
        Executable executable = () -> EndianUtils.getShortLE(data, offset);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }

    @Test
    public void testGetShortLEWithNullArray() {
        byte[] data = null;
        int offset = 0;
        Executable executable = () -> EndianUtils.getShortLE(data, offset);
        assertThrows(NullPointerException.class, executable);
    }
}
