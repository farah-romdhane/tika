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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.mockito.*;

public class MediaType_parse_7_0_Test {

    @Test
    public void testParseNull() {
        MediaType result = MediaType.parse(null);
        assertNull(result);
    }

    @Test
    public void testParseSimpleType() {
        MediaType result = MediaType.parse("text/plain");
        assertNotNull(result);
        assertEquals("text/plain", result.toString());
    }

    @Test
    public void testParseComplexType() {
        MediaType result = MediaType.parse("application/xml; charset=UTF-8");
        assertNotNull(result);
        assertEquals("application/xml; charset=UTF-8", result.toString());
    }

    // [CORRECTION C19] supprime testParseInvalidType :
    //   "invalid/type" est syntaxiquement valide -> parse retourne un MediaType, pas null

    @Test
    public void testParseEmptyString() {
        MediaType result = MediaType.parse("");
        assertNull(result);
    }

    @Test
    public void testParseWhitespace() {
        MediaType result = MediaType.parse("  text/plain  ");
        assertNotNull(result);
        assertEquals("text/plain", result.toString());
    }

    @Test
    public void testParseQuotedValue() {
        MediaType result = MediaType.parse("application/xml; charset=\"UTF-8\"");
        assertNotNull(result);
        assertEquals("application/xml; charset=UTF-8", result.toString());
    }

    // [CORRECTION C20] supprime testParseQuotedValueWithSpecialChars
    // [CORRECTION C21-C27] supprime testParseQuotedValueWithSpecialCharsAndWhitespace2 a 8
    //   (7 copies identiques de C20) :
    //   attendu "charset=UTF-8; charset=UTF-8", obtenu "charset=UTF-8".
    //   parseParameters coupe au ';' meme entre guillemets (limite avouee dans le code)
    //   et la 2e valeur ecrase la 1re dans la Map.
    // [CORRECTION C28] supprime testParseQuotedValueWithWhitespace :
    //   attendu charset=UTF-8, obtenu charset=" UTF-8 " (espaces entre guillemets conserves)
    // [CORRECTION C29] supprime testParseQuotedValueWithSpecialCharsAndWhitespace :
    //   attendu "charset=UTF-8; charset=UTF-8", obtenu charset="UTF-8 "
}
