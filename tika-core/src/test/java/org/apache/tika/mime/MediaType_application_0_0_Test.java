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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;

public class MediaType_application_0_0_Test {

    @Test
    public void testApplicationMethod() throws Exception {
        // Test with a valid type
        MediaType result = MediaType.application("json");
        assertNotNull(result);
        assertEquals("application/json", result.toString());
        // [CORRECTION C1] retire : assertThrows(NullPointerException) pour application(null)
        //   -> parse("application/null") retourne un MediaType valide
        // [CORRECTION C2] retire : assertThrows(IllegalArgumentException) pour application("")
        //   -> parse("application/") retourne null, aucune exception
        // [CORRECTION C3] retire : assertThrows(IllegalArgumentException) pour application("invalid/type")
        //   -> parse("application/invalid/type") retourne null, aucune exception
    }

    @Test
    public void testConstructor() throws Exception {
        // Test with valid type, subtype, and parameters
        Map<String, String> parameters = new HashMap<>();
        parameters.put("charset", "UTF-8");
        MediaType result = new MediaType("application", "json", parameters);
        assertNotNull(result);
        assertEquals("application/json; charset=UTF-8", result.toString());
        // Test with empty parameters
        result = new MediaType("application", "json", Collections.emptyMap());
        assertNotNull(result);
        assertEquals("application/json", result.toString());
        // Test with null parameters
        Executable executable = () -> new MediaType("application", "json", null);
        assertThrows(NullPointerException.class, executable);
        // Test with null type
        executable = () -> new MediaType(null, "json", Collections.emptyMap());
        assertThrows(NullPointerException.class, executable);
        // Test with null subtype
        executable = () -> new MediaType("application", null, Collections.emptyMap());
        assertThrows(NullPointerException.class, executable);
        // [CORRECTION C4-C7] retire 4 x assertThrows(IllegalArgumentException) :
        //   type vide, sous-type vide, type avec "/", sous-type avec "/"
        //   -> le constructeur fait seulement trim() + toLowerCase(), il ne valide rien
    }

    @Test
    public void testGetBaseType() throws Exception {
        MediaType result = MediaType.parse("application/json");
        assertNotNull(result.getBaseType());
        assertEquals(MediaType.parse("application/json"), result.getBaseType());
    }

    @Test
    public void testGetType() throws Exception {
        MediaType result = MediaType.parse("application/json");
        assertNotNull(result.getType());
        assertEquals("application", result.getType());
    }

    @Test
    public void testGetSubtype() throws Exception {
        MediaType result = MediaType.parse("application/json");
        assertNotNull(result.getSubtype());
        assertEquals("json", result.getSubtype());
    }

    @Test
    public void testGetParameters() throws Exception {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("charset", "UTF-8");
        MediaType result = new MediaType("application", "json", parameters);
        assertNotNull(result.getParameters());
        assertEquals(parameters, result.getParameters());
    }
}
