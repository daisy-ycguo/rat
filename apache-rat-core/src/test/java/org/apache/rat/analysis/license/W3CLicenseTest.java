/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */ 
package org.apache.rat.analysis.license;

import junit.framework.TestCase;

import org.apache.rat.report.claim.ISubject;
import org.apache.rat.report.claim.MockSubject;
import org.apache.rat.report.claim.impl.xml.MockClaimReporter;

public class W3CLicenseTest extends TestCase {

    public static final String COPYRIGHT_URL 
    = "http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231";
    
    public static final String COPYRIGHT_URL_COMMENTED
    = "# http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231 #";
    
    public static final String COPYRIGHT_URL_XML
    = "<!-- http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231 -->";
    
    W3CLicense license;
    MockClaimReporter reporter;
    
    protected void setUp() throws Exception {
        super.setUp();
        license = new W3CLicense();
        reporter = new MockClaimReporter();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMatch() throws Exception {
        final ISubject subject = new MockSubject("subject");
        assertTrue("Expected matcher to return license", license.match(subject, COPYRIGHT_URL, reporter));
        assertTrue("Expected matcher to return license", license.match(subject, COPYRIGHT_URL_COMMENTED, reporter));
        assertTrue("Expected matcher to return license", license.match(subject, COPYRIGHT_URL_XML, reporter));
        assertFalse("Return null if the license isn't matched", license.match(subject, "Bogus", reporter));
    }
}
