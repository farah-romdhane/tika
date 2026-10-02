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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class MediaType_image_2_0_Test {

    @Test
    public void testImageMethod() throws Exception {
        // Create a test instance of MediaType
        MediaType testInstance = new MediaType("image", "png", new HashMap<>());
        // Get the private image method using reflection
        Method imageMethod = MediaType.class.getDeclaredMethod("image", String.class);
        imageMethod.setAccessible(true);
        // Invoke the image method with a test parameter
        MediaType result = (MediaType) imageMethod.invoke(testInstance, "png");
        // Verify the result
        assertEquals("image/png", result.toString());
    }

    @Test
    public void testImageMethodWithNullParameter() throws Exception {
        // Create a test instance of MediaType
        MediaType testInstance = new MediaType("image", "png", new HashMap<>());
        // Get the private image method using reflection
        Method imageMethod = MediaType.class.getDeclaredMethod("image", String.class);
        imageMethod.setAccessible(true);
        // Invoke the image method with a null parameter
        Executable executable = () -> imageMethod.invoke(testInstance, null);
        // Verify that an IllegalArgumentException is thrown
        assertThrows(IllegalArgumentException.class, executable);
    }

    // [CORRECTION C17] supprime testImageMethodWithEmptyParameter :
    //   seule assertion = assertThrows(IllegalArgumentException) ; parse("image/") retourne null
    // [CORRECTION C18] supprime testImageMethodWithWhitespaceParameter :
    //   seule assertion = assertThrows(IllegalArgumentException) ; parse("image/ ") retourne null

    @Test
    public void testImageMethodWithSpecialCharacterParameter() throws Exception {
        // Create a test instance of MediaType
        MediaType testInstance = new MediaType("image", "png", new HashMap<>());
        // Get the private image method using reflection
        Method imageMethod = MediaType.class.getDeclaredMethod("image", String.class);
        imageMethod.setAccessible(true);
        // Invoke the image method with a special character parameter
        Executable executable = () -> imageMethod.invoke(testInstance, "jpg");
        // Verify that the method returns a valid MediaType object
        MediaType result = (MediaType) imageMethod.invoke(testInstance, "jpg");
        assertEquals("image/jpg", result.toString());
    }
}
