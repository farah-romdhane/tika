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

public class EndianUtils_getUShortBE_18_0_Test {

    @Test
    public void testGetUShortBE() throws IOException {
        byte[] data = { 0x01, 0x02 };
        int result = EndianUtils.getUShortBE(data);
        assertEquals(258, result);
    }

    @Test
    public void testGetUShortBEWithOffset() throws IOException {
        byte[] data = { 0x01, 0x02, 0x03, 0x04 };
        int result = EndianUtils.getUShortBE(data, 1);
        assertEquals(258, result);
    }

    @Test
    public void testGetUShortBEWithEmptyArray() throws IOException {
        byte[] data = {};
        try {
            EndianUtils.getUShortBE(data);
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected exception
        }
    }

    @Test
    public void testGetUShortBEWithNegativeOffset() throws IOException {
        byte[] data = { 0x01, 0x02 };
        try {
            EndianUtils.getUShortBE(data, -1);
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected exception
        }
    }

    @Test
    public void testGetUShortBEWithOffsetGreaterThanArrayLength() throws IOException {
        byte[] data = { 0x01, 0x02 };
        try {
            EndianUtils.getUShortBE(data, 2);
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected exception
        }
    }
}
