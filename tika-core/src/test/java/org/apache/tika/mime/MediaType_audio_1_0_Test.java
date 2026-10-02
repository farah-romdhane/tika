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
package org.apache.tika.mime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

// [CORRECTION C8] retire @ExtendWith(MockitoExtension.class), le champ @Mock et setUp() :
//   le mock n'etait jamais utilise et faisait planter le test
//   (Byte Buddy trop ancien : "Unknown Java version: 21") -> probleme d'environnement
public class MediaType_audio_1_0_Test {

    @Test
    public void testAudio() {
        // Test with valid subtype
        MediaType result = MediaType.audio("test");
        assertEquals("audio/test", result.toString());
        assertEquals("audio", result.getType());
        assertEquals("test", result.getSubtype());
        assertTrue(result.getParameters().isEmpty());
        // [CORRECTION C9] retire : assertThrows(NullPointerException) pour audio(null)
        //   -> parse("audio/null") retourne un MediaType valide
        // [CORRECTION C10] retire : assertThrows(IllegalArgumentException) pour audio("")
        //   -> parse("audio/") retourne null, aucune exception
    }
}
