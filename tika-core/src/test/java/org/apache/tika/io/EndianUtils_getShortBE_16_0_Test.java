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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EndianUtils_getShortBE_16_0_Test {

    @Mock
    private EndianUtils endianUtils;

    @Test
    public void testGetShortBE() throws IOException {
        byte[] data = new byte[] { 0x01, 0x02 };
        short expected = 0x0201;
        when(endianUtils.getUShortBE(data, 0)).thenReturn(0x0201);
        short result = EndianUtils.getShortBE(data);
        assertEquals(expected, result);
        verify(endianUtils, times(1)).getUShortBE(data, 0);
    }
}
