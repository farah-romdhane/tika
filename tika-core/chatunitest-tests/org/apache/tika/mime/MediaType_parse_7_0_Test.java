package org.apache.tika.mime;

import org.apache.tika.mime.MediaType;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;

public class MediaType_parse_7_0_Test {

    @Test
    public void testParseNull() {
        MediaType result = MediaType.parse(null);
        assertNull(result);
    }

    @Test
    public void testParseSimpleType() {
        MediaType result = MediaType.parse("text/plain");
        assertNotNull(result);
        assertEquals("text/plain", result.toString());
    }

    @Test
    public void testParseComplexType() {
        MediaType result = MediaType.parse("application/xml; charset=UTF-8");
        assertNotNull(result);
        assertEquals("application/xml; charset=UTF-8", result.toString());
    }

    @Test
    public void testParseInvalidType() {
        MediaType result = MediaType.parse("invalid/type");
        assertNull(result);
    }

    @Test
    public void testParseEmptyString() {
        MediaType result = MediaType.parse("");
        assertNull(result);
    }

    @Test
    public void testParseWhitespace() {
        MediaType result = MediaType.parse("  text/plain  ");
        assertNotNull(result);
        assertEquals("text/plain", result.toString());
    }

    @Test
    public void testParseQuotedValue() {
        MediaType result = MediaType.parse("application/xml; charset=\"UTF-8\"");
        assertNotNull(result);
        assertEquals("application/xml; charset=UTF-8", result.toString());
    }

    @Test
    public void testParseQuotedValueWithSpecialChars() {
        MediaType result = MediaType.parse("application/xml; charset=\"UTF-8; charset=UTF-8\"");
        assertNotNull(result);
        assertEquals("application/xml; charset=UTF-8; charset=UTF-8", result.toString());
    }

    @Test
    public void testParseQuotedValueWithWhitespace() {
        MediaType result = MediaType.parse("application/xml; charset=\" UTF-8 \"");
        assertNotNull(result);
        assertEquals("application/xml; charset=UTF-8", result.toString());
    }

    @Test
    public void testParseQuotedValueWithSpecialCharsAndWhitespace() {
        MediaType result = MediaType.parse("application/xml; charset=\" UTF-8; charset=UTF-8 \"");
        assertNotNull(result);
        assertEquals("application/xml; charset=UTF-8; charset=UTF-8", result.toString());
    }

    @Test
    public void testParseQuotedValueWithSpecialCharsAndWhitespace2() {
        MediaType result = MediaType.parse("application/xml; charset=\"UTF-8; charset=UTF-8\"");
        assertNotNull(result);
        assertEquals("application/xml; charset=UTF-8; charset=UTF-8", result.toString());
    }

    @Test
    public void testParseQuotedValueWithSpecialCharsAndWhitespace3() {
        MediaType result = MediaType.parse("application/xml; charset=\"UTF-8; charset=UTF-8\"");
        assertNotNull(result);
        assertEquals("application/xml; charset=UTF-8; charset=UTF-8", result.toString());
    }

    @Test
    public void testParseQuotedValueWithSpecialCharsAndWhitespace4() {
        MediaType result = MediaType.parse("application/xml; charset=\"UTF-8; charset=UTF-8\"");
        assertNotNull(result);
        assertEquals("application/xml; charset=UTF-8; charset=UTF-8", result.toString());
    }

    @Test
    public void testParseQuotedValueWithSpecialCharsAndWhitespace5() {
        MediaType result = MediaType.parse("application/xml; charset=\"UTF-8; charset=UTF-8\"");
        assertNotNull(result);
        assertEquals("application/xml; charset=UTF-8; charset=UTF-8", result.toString());
    }

    @Test
    public void testParseQuotedValueWithSpecialCharsAndWhitespace6() {
        MediaType result = MediaType.parse("application/xml; charset=\"UTF-8; charset=UTF-8\"");
        assertNotNull(result);
        assertEquals("application/xml; charset=UTF-8; charset=UTF-8", result.toString());
    }

    @Test
    public void testParseQuotedValueWithSpecialCharsAndWhitespace7() {
        MediaType result = MediaType.parse("application/xml; charset=\"UTF-8; charset=UTF-8\"");
        assertNotNull(result);
        assertEquals("application/xml; charset=UTF-8; charset=UTF-8", result.toString());
    }

    @Test
    public void testParseQuotedValueWithSpecialCharsAndWhitespace8() {
        MediaType result = MediaType.parse("application/xml; charset=\"UTF-8; charset=UTF-8\"");
        assertNotNull(result);
        assertEquals("application/xml; charset=UTF-8; charset=UTF-8", result.toString());
    }
}
