/*
 *  This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.url;

import java.net.URISyntaxException;

import org.apache.commons.httpclient.URIException;
import org.archive.url.UsableURI;

import junit.framework.TestCase;

public class UURITest extends TestCase {
    public void testHasScheme() {
        assertTrue(UsableURI.hasScheme("http://www.archive.org"));
        assertTrue(UsableURI.hasScheme("http:"));
        assertFalse(UsableURI.hasScheme("ht/tp://www.archive.org"));
        assertFalse(UsableURI.hasScheme("/tmp"));
    }
    
    public void testGetFileName() throws URISyntaxException {
        final String filename = "x.arc.gz";
        assertEquals(filename,
            UsableURI.parseFilename("/tmp/one.two/" + filename));
        assertEquals(filename,
            UsableURI.parseFilename("http://archive.org/tmp/one.two/" +
                    filename));
        assertEquals(filename,
            UsableURI.parseFilename("rsync://archive.org/tmp/one.two/" +
                    filename)); 
    }
    
    public void testSchemalessRelative() throws URIException {
        UsableURI base = new UsableURI("http://www.archive.org/a", true, "UTF-8");
        UsableURI relative = new UsableURI("//www.facebook.com/?href=http://www.archive.org/a", true, "UTF-8");
        assertEquals(null, relative.getScheme());
        assertEquals("www.facebook.com", relative.getAuthority());
        UsableURI test = new UsableURI(base, relative);
        assertEquals("http://www.facebook.com/?href=http://www.archive.org/a", test.toString());
    }
}
