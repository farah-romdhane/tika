package org.apache.tika.mime;

import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.function.Executable;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashSet;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class MediaType_application_0_0_Test {

    @Test
    public void testApplicationMethod() throws Exception {
        // Test with a valid type
        MediaType result = MediaType.application("json");
        assertNotNull(result);
        assertEquals("application/json", result.toString());
        // Test with a null type
        Executable executable = () -> MediaType.application(null);
        assertThrows(NullPointerException.class, executable);
        // Test with an empty type
        executable = () -> MediaType.application("");
        assertThrows(IllegalArgumentException.class, executable);
        // Test with a type containing invalid characters
        executable = () -> MediaType.application("invalid/type");
        assertThrows(IllegalArgumentException.class, executable);
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
        // Test with empty type
        executable = () -> new MediaType("", "json", Collections.emptyMap());
        assertThrows(IllegalArgumentException.class, executable);
        // Test with empty subtype
        executable = () -> new MediaType("application", "", Collections.emptyMap());
        assertThrows(IllegalArgumentException.class, executable);
        // Test with type containing invalid characters
        executable = () -> new MediaType("invalid/type", "json", Collections.emptyMap());
        assertThrows(IllegalArgumentException.class, executable);
        // Test with subtype containing invalid characters
        executable = () -> new MediaType("application", "invalid/subtype", Collections.emptyMap());
        assertThrows(IllegalArgumentException.class, executable);
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
