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
package org.apache.rat.document.impl.util;

import org.apache.rat.document.MockDocument;
import org.apache.rat.document.MockDocumentMatcher;
import junit.framework.TestCase;

public class MatchNegatorTest extends TestCase {

    MatchNegator negator;
    MockDocumentMatcher matcher;
    MockDocument document;
    
    protected void setUp() throws Exception {
        super.setUp();
        matcher = new MockDocumentMatcher();
        negator = new MatchNegator(matcher);
        document = new MockDocument();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMatch() throws Exception {
        matcher.returnValue = true;
        assertFalse("Negate match return value", negator.matches(document));
    }

    public void testNoMatch() throws Exception {
        matcher.returnValue = false;
        assertTrue("Negate match return value", negator.matches(document));
    }
}
