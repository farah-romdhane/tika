package org.apache.tika.mime;

import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.function.Executable;
import java.util.Collections;
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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
