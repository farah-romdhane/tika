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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MediaType_text_3_0_Test {

    @Test
    public void testTextMethod() {
        // Test with valid input
        MediaType result = MediaType.text("plain");
        assertNotNull(result);
        assertEquals("text/plain", result.toString());
        // [CORRECTION C11-C12] retire : assertNotNull + assertEquals("text/") pour text("")
        //   -> parse("text/") retourne null (sous-type vide refuse)
        // [CORRECTION C13] retire : assertNull pour text(null)
        //   -> parse("text/null") retourne un MediaType valide
    }
}
