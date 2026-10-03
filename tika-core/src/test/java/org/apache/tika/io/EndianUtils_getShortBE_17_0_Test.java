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

import java.io.IOException;

import org.junit.jupiter.api.Test;

import org.apache.tika.exception.TikaException;

public class EndianUtils_getShortBE_17_0_Test {

    @Test
    public void testGetShortBE() throws IOException, TikaException {
        byte[] data = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        int offset = 0;
        // CORRECTION MANUELLE : l'IA attendait 0x0100 (little-endian). En big-endian, 00 01 -> 0x0001
        short expected = 0x0001;
        short result = EndianUtils.getShortBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortBEWithOffset() throws IOException, TikaException {
        byte[] data = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        int offset = 2;
        // CORRECTION MANUELLE : l'IA attendait 0x0302 (little-endian). En big-endian, 02 03 -> 0x0203
        short expected = 0x0203;
        short result = EndianUtils.getShortBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortBEWithNegativeOffset() throws IOException, TikaException {
        byte[] data = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        int offset = -1;
        Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> {
            EndianUtils.getShortBE(data, offset);
        });
        // CORRECTION MANUELLE : avec offset = -1, Java signale l'index fautif -1 (et non 7)
        assertEquals("Index -1 out of bounds for length 8", exception.getMessage());
    }

    @Test
    public void testGetShortBEWithLargeOffset() throws IOException, TikaException {
        byte[] data = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        int offset = 8;
        Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> {
            EndianUtils.getShortBE(data, offset);
        });
        assertEquals("Index 8 out of bounds for length 8", exception.getMessage());
    }
}
