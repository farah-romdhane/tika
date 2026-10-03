package org.apache.tika.mime;

import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.function.Executable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Test
    public void testImageMethodWithEmptyParameter() throws Exception {
        // Create a test instance of MediaType
        MediaType testInstance = new MediaType("image", "png", new HashMap<>());
        // Get the private image method using reflection
        Method imageMethod = MediaType.class.getDeclaredMethod("image", String.class);
        imageMethod.setAccessible(true);
        // Invoke the image method with an empty parameter
        Executable executable = () -> imageMethod.invoke(testInstance, "");
        // Verify that an IllegalArgumentException is thrown
        assertThrows(IllegalArgumentException.class, executable);
    }

    @Test
    public void testImageMethodWithWhitespaceParameter() throws Exception {
        // Create a test instance of MediaType
        MediaType testInstance = new MediaType("image", "png", new HashMap<>());
        // Get the private image method using reflection
        Method imageMethod = MediaType.class.getDeclaredMethod("image", String.class);
        imageMethod.setAccessible(true);
        // Invoke the image method with a whitespace parameter
        Executable executable = () -> imageMethod.invoke(testInstance, " ");
        // Verify that an IllegalArgumentException is thrown
        assertThrows(IllegalArgumentException.class, executable);
    }

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
