package org.apache.tika.mime;

import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.function.Executable;
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

public class MediaType_video_4_0_Test {

    @Test
    public void testVideoMethod() {
        // Test with a valid video type
        MediaType result = MediaType.video("mp4");
        assertEquals("video/mp4", result.toString());
        // Test with an empty string
        Executable executable = () -> MediaType.video("");
        assertThrows(IllegalArgumentException.class, executable, "Expected IllegalArgumentException for empty string");
        // Test with a null string
        executable = () -> MediaType.video(null);
        assertThrows(NullPointerException.class, executable, "Expected NullPointerException for null string");
        // Test with a string containing special characters
        executable = () -> MediaType.video("video/mp4; foo=bar");
        assertThrows(IllegalArgumentException.class, executable, "Expected IllegalArgumentException for string with special characters");
    }
}
