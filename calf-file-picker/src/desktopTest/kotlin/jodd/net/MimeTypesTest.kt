// Copyright (c) 2003-present, Jodd Team (http://jodd.org)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.
package jodd.net

import jodd.net.MimeTypes.findExtensionsByMimeTypes
import jodd.net.MimeTypes.getMimeType
import jodd.net.MimeTypes.lookupMimeType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class MimeTypesTest {
    @Test
    fun testSimpleMime() {
        assertEquals("application/atom+xml", getMimeType("atom"))
        assertEquals("audio/x-wav", getMimeType("wav"))
        assertEquals("image/jpeg", getMimeType("jpg"))
        assertEquals("text/x-asm", getMimeType("asm"))
        assertEquals("video/mp4", getMimeType("mp4"))

        assertEquals("image/jpeg", lookupMimeType("jpg"))
        assertEquals("application/octet-stream", getMimeType("xxx"))
        assertNull(lookupMimeType("xxx"))
    }

    @Test
    fun testFind() {
        val extensionList: List<String> = findExtensionsByMimeTypes("image/jpeg", false)

        assertEquals(3, extensionList.size)

        assertTrue(extensionList.contains("jpe"))
        assertTrue(extensionList.contains("jpg"))
        assertTrue(extensionList.contains("jpeg"))

        val extensionList2: List<String> = findExtensionsByMimeTypes("image/png", false)
        val extensionList3: List<String> = findExtensionsByMimeTypes("image/jpeg, image/png", false)

        assertEquals(extensionList3.size, extensionList2.size + extensionList.size)
    }

    @Test
    fun testFindWithWildcards() {
        val extensionList: List<String> = findExtensionsByMimeTypes("image/*", true)

        assertTrue(extensionList.size > 3)

        assertTrue(extensionList.contains("jpe"))
        assertTrue(extensionList.contains("jpg"))
        assertTrue(extensionList.contains("jpeg"))
        assertTrue(extensionList.contains("bmp"))
        assertTrue(extensionList.contains("png"))
    }
}