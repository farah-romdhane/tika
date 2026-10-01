package org.apache.tika.mime;

import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.function.Executable;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
