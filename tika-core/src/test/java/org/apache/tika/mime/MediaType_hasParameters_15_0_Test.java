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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class MediaType_hasParameters_15_0_Test {

    @Test
    public void testHasParametersWithEmptyParameters() {
        MediaType mediaType = new MediaType("text", "plain", Collections.emptyMap());
        assertFalse(mediaType.hasParameters());
    }

    @Test
    public void testHasParametersWithNonEmptyParameters() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("charset", "UTF-8");
        MediaType mediaType = new MediaType("text", "plain", parameters);
        assertTrue(mediaType.hasParameters());
    }

    @Test
    public void testHasParametersWithSpecialCharacterParameters() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("charset", "UTF-8!");
        MediaType mediaType = new MediaType("text", "plain", parameters);
        assertTrue(mediaType.hasParameters());
    }

    @Test
    public void testHasParametersWithQuotedParameters() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("charset", "\"UTF-8\"");
        MediaType mediaType = new MediaType("text", "plain", parameters);
        assertTrue(mediaType.hasParameters());
    }

    @Test
    public void testHasParametersWithMultipleParameters() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("charset", "UTF-8");
        parameters.put("format", "json");
        MediaType mediaType = new MediaType("text", "plain", parameters);
        assertTrue(mediaType.hasParameters());
    }

    @Test
    public void testHasParametersWithCaseInsensitiveParameters() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("CHARSET", "UTF-8");
        MediaType mediaType = new MediaType("text", "plain", parameters);
        assertTrue(mediaType.hasParameters());
    }

    @Test
    public void testHasParametersWithSpecialCharacterParameterValues() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("charset", "UTF-8!");
        MediaType mediaType = new MediaType("text", "plain", parameters);
        assertTrue(mediaType.hasParameters());
    }

    @Test
    public void testHasParametersWithQuotedParameterValues() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("charset", "\"UTF-8\"");
        MediaType mediaType = new MediaType("text", "plain", parameters);
        assertTrue(mediaType.hasParameters());
    }

    @Test
    public void testHasParametersWithMultipleParameterValues() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("charset", "UTF-8");
        parameters.put("format", "json");
        MediaType mediaType = new MediaType("text", "plain", parameters);
        assertTrue(mediaType.hasParameters());
    }

    @Test
    public void testHasParametersWithCaseInsensitiveParameterValues() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("CHARSET", "UTF-8");
        MediaType mediaType = new MediaType("text", "plain", parameters);
        assertTrue(mediaType.hasParameters());
    }
}
