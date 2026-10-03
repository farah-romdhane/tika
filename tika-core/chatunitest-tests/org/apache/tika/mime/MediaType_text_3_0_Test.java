package org.apache.tika.mime;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
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

@ExtendWith(MockitoExtension.class)
public class MediaType_text_3_0_Test {

    @Test
    public void testTextMethod() {
        // Test with valid input
        MediaType result = MediaType.text("plain");
        assertNotNull(result);
        assertEquals("text/plain", result.toString());
        // Test with empty input
        result = MediaType.text("");
        assertNotNull(result);
        assertEquals("text/", result.toString());
        // Test with null input
        result = MediaType.text(null);
        assertNull(result);
    }
}
