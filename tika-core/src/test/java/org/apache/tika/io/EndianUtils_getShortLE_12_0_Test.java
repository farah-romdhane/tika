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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class EndianUtils_getShortLE_12_0_Test {

    @Test
    public void testGetShortLE() {
        byte[] data = { (byte) 0x12, (byte) 0x34 };
        short expected = 0x3412;
        short result = EndianUtils.getShortLE(data);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortLEWithOffset() {
        byte[] data = { (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78 };
        short expected = 0x3412;
        short result = EndianUtils.getShortLE(data, 0);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortLEWithOffsetAndPartialData() {
        byte[] data = { (byte) 0x12, (byte) 0x34 };
        short expected = 0x3412;
        short result = EndianUtils.getShortLE(data, 0);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortLEWithEmptyData() {
        byte[] data = {};
        // CORRECTION MANUELLE : un tableau vide n'est pas null ; l'acces a data[0] leve ArrayIndexOutOfBoundsException
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> EndianUtils.getShortLE(data));
    }

    @Test
    public void testGetShortLEWithNullData() {
        byte[] data = null;
        assertThrows(NullPointerException.class, () -> EndianUtils.getShortLE(data));
    }
}
