package org.apache.tika.mime;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ExtendWith(MockitoExtension.class)
public class MediaType_audio_1_0_Test {

    @Mock
    private MediaType mockMediaType;

    @BeforeEach
    public void setUp() {
        when(mockMediaType.getType()).thenReturn("audio");
        when(mockMediaType.getSubtype()).thenReturn("test");
        when(mockMediaType.getParameters()).thenReturn(Collections.emptyMap());
    }

    @Test
    public void testAudio() {
        // Test with valid subtype
        MediaType result = MediaType.audio("test");
        assertEquals("audio/test", result.toString());
        assertEquals("audio", result.getType());
        assertEquals("test", result.getSubtype());
        assertTrue(result.getParameters().isEmpty());
        // Test with null subtype
        assertThrows(NullPointerException.class, () -> MediaType.audio(null));
        // Test with empty subtype
        assertThrows(IllegalArgumentException.class, () -> MediaType.audio(""));
    }
}
