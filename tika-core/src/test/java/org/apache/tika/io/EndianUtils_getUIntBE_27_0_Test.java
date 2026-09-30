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

public class EndianUtils_getUIntBE_27_0_Test {

    @Test
    public void testGetUIntBE() throws IOException, TikaException {
        byte[] data = new byte[] { 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00 };
        int offset = 0;
        long expected = 1L;
        long result = EndianUtils.getUIntBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUIntBEWithNegativeNumber() throws IOException, TikaException {
        byte[] data = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFE, 0x00, 0x00, 0x00, 0x00 };
        int offset = 0;
        long expected = 4294967294L;
        long result = EndianUtils.getUIntBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUIntBEWithMaxValue() throws IOException, TikaException {
        byte[] data = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        int offset = 0;
        long expected = 4294967295L;
        long result = EndianUtils.getUIntBE(data, offset);
        assertEquals(expected, result);
    }
}
