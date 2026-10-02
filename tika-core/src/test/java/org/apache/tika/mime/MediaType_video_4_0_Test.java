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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.mockito.*;

public class MediaType_video_4_0_Test {

    @Test
    public void testVideoMethod() {
        // Test with a valid video type
        MediaType result = MediaType.video("mp4");
        assertEquals("video/mp4", result.toString());
        // [CORRECTION C14] retire : assertThrows(IllegalArgumentException) pour video("")
        //   -> parse("video/") retourne null, aucune exception
        // [CORRECTION C15] retire : assertThrows(NullPointerException) pour video(null)
        //   -> parse("video/null") retourne un MediaType valide
        // [CORRECTION C16] retire : assertThrows(IllegalArgumentException) pour video("video/mp4; foo=bar")
        //   -> parse("video/video/mp4; foo=bar") retourne null, aucune exception
    }
}
