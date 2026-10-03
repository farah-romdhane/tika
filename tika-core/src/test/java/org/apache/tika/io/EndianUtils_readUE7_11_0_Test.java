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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

public class EndianUtils_readUE7_11_0_Test {

    @Test
    public void testReadUE7() throws IOException {
        // Test case 1: Normal input
        byte[] input1 = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        InputStream stream1 = new ByteArrayInputStream(input1);
        // CORRECTION MANUELLE : 0x01 n'a pas le bit de continuation -> lecture terminee apres 1 octet -> 1
        assertEquals(1L, EndianUtils.readUE7(stream1));
        // Test case 2: Input with continuation bit
        byte[] input2 = { (byte) 0x81, (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85, (byte) 0x86, (byte) 0x87, (byte) 0x88 };
        InputStream stream2 = new ByteArrayInputStream(input2);
        // CORRECTION MANUELLE : la methode lit au maximum 6 octets de 7 bits : 1,2,3,4,5,6
        // -> 1*2^35 + 2*2^28 + 3*2^21 + 4*2^14 + 5*2^7 + 6 = 34902966918
        assertEquals(34902966918L, EndianUtils.readUE7(stream2));
        // Test case 3: Input with continuation bit and last value
        byte[] input3 = { (byte) 0x81, (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85, (byte) 0x86, (byte) 0x87, (byte) 0x08 };
        InputStream stream3 = new ByteArrayInputStream(input3);
        // CORRECTION MANUELLE : meme calcul (limite de 6 octets atteinte avant le dernier octet)
        assertEquals(34902966918L, EndianUtils.readUE7(stream3));
        // Test case 4: Input with only one byte
        byte[] input4 = { (byte) 0x01 };
        InputStream stream4 = new ByteArrayInputStream(input4);
        assertEquals(0x01L, EndianUtils.readUE7(stream4));
        // Test case 5: Input with only one byte and continuation bit
        byte[] input5 = { (byte) 0x81 };
        InputStream stream5 = new ByteArrayInputStream(input5);
        // CORRECTION MANUELLE : 0x81 annonce un octet suivant qui n'existe pas -> IOException (buffer underrun)
        assertThrows(IOException.class, () -> EndianUtils.readUE7(stream5));
        // Test case 6: Input with only one byte and last value
        byte[] input6 = { (byte) 0x01 };
        InputStream stream6 = new ByteArrayInputStream(input6);
        assertEquals(0x01L, EndianUtils.readUE7(stream6));
        // Test case 7: Input with buffer underun
        byte[] input7 = {};
        InputStream stream7 = new ByteArrayInputStream(input7);
        assertThrows(IOException.class, () -> EndianUtils.readUE7(stream7));
    }
}
