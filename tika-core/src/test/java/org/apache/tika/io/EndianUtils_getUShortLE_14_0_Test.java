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

public class EndianUtils_getUShortLE_14_0_Test {

    @Test
    public void testGetUShortLE() throws IOException, TikaException {
        byte[] data = { 0x01, 0x02 };
        int result = EndianUtils.getUShortLE(data);
        assertEquals(0x0201, result);
    }

    @Test
    public void testGetUShortLEWithOffset() throws IOException, TikaException {
        byte[] data = { 0x01, 0x02, 0x03, 0x04 };
        int result = EndianUtils.getUShortLE(data, 1);
        assertEquals(0x0403, result);
    }

    @Test
    public void testGetUShortLEWithEmptyArray() throws IOException, TikaException {
        byte[] data = {};
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getUShortLE(data));
    }

    @Test
    public void testGetUShortLEWithSingleElementArray() throws IOException, TikaException {
        byte[] data = { 0x01 };
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getUShortLE(data));
    }
}
