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

import java.lang.reflect.Method;
import java.util.Collections;

import org.junit.jupiter.api.Test;

public class MediaType_hashCode_19_0_Test {

    @Test
    public void testHashCode() throws Exception {
        // Create a MediaType object
        MediaType mediaType = new MediaType("text", "plain", Collections.emptyMap());
        // Get the hashCode method using reflection
        Method hashCodeMethod = MediaType.class.getDeclaredMethod("hashCode");
        // Make the method accessible
        hashCodeMethod.setAccessible(true);
        // Invoke the hashCode method
        int hashCode = (int) hashCodeMethod.invoke(mediaType);
        // Verify the result
        assertEquals("text/plain".hashCode(), hashCode);
    }

    @Test
    public void testHashCodeWithParameters() throws Exception {
        // Create a MediaType object with parameters
        MediaType mediaType = new MediaType("text", "plain", Collections.singletonMap("charset", "UTF-8"));
        // Get the hashCode method using reflection
        Method hashCodeMethod = MediaType.class.getDeclaredMethod("hashCode");
        // Make the method accessible
        hashCodeMethod.setAccessible(true);
        // Invoke the hashCode method
        int hashCode = (int) hashCodeMethod.invoke(mediaType);
        // Verify the result
        assertEquals("text/plain; charset=UTF-8".hashCode(), hashCode);
    }

    @Test
    public void testHashCodeWithEmptyParameters() throws Exception {
        // Create a MediaType object with empty parameters
        MediaType mediaType = new MediaType("text", "plain", Collections.emptyMap());
        // Get the hashCode method using reflection
        Method hashCodeMethod = MediaType.class.getDeclaredMethod("hashCode");
        // Make the method accessible
        hashCodeMethod.setAccessible(true);
        // Invoke the hashCode method
        int hashCode = (int) hashCodeMethod.invoke(mediaType);
        // Verify the result
        assertEquals("text/plain".hashCode(), hashCode);
    }

    @Test
    public void testHashCodeWithWhitespace() throws Exception {
        // Create a MediaType object with whitespace
        MediaType mediaType = new MediaType(" text ", " plain ", Collections.emptyMap());
        // Get the hashCode method using reflection
        Method hashCodeMethod = MediaType.class.getDeclaredMethod("hashCode");
        // Make the method accessible
        hashCodeMethod.setAccessible(true);
        // Invoke the hashCode method
        int hashCode = (int) hashCodeMethod.invoke(mediaType);
        // Verify the result
        assertEquals("text/plain".hashCode(), hashCode);
    }

    @Test
    public void testHashCodeWithEmptyType() throws Exception {
        // Create a MediaType object with empty type
        MediaType mediaType = new MediaType("", "plain", Collections.emptyMap());
    }
}
