package org.apache.tika.mime;

import org.apache.tika.mime.MediaType;
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

public class MediaType_compareTo_20_0_Test {

    @Test
    public void testCompareTo() throws Exception {
        // Create two MediaType objects
        Map<String, String> params1 = new HashMap<>();
        params1.put("charset", "UTF-8");
        MediaType mediaType1 = new MediaType("text", "plain", params1);
        Map<String, String> params2 = new HashMap<>();
        params2.put("charset", "UTF-8");
        MediaType mediaType2 = new MediaType("text", "html", params2);
        // Compare the two MediaType objects
        int result = mediaType1.compareTo(mediaType2);
        // Assert that the result is as expected
        assertEquals(-1, result);
    }
}
