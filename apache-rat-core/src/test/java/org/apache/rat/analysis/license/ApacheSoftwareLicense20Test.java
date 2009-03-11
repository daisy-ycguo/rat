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

public class ApacheSoftwareLicense20Test extends TestCase {

    MockClaimReporter reporter;
    
	protected void setUp() throws Exception {
		super.setUp();
        reporter = new MockClaimReporter();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testMatches() throws Exception {
		ApacheSoftwareLicense20 worker = new ApacheSoftwareLicense20();
		assertTrue(worker.matches(ApacheSoftwareLicense20.FIRST_LICENSE_LINE));
		assertTrue(worker.matches("    Licensed under the Apache License, Version 2.0 (the \"License\");"));
		assertTrue(worker.matches("Licensed under the Apache License, Version 2.0 (the \"License\");"));
		assertTrue(worker.matches(" * Licensed under the Apache License, Version 2.0 (the \"License\");"));
		assertTrue(worker.matches(" // Licensed under the Apache License, Version 2.0 (the \"License\");"));
		assertTrue(worker.matches(" /* Licensed under the Apache License, Version 2.0 (the \"License\");"));
		assertTrue(worker.matches("    Licensed under the Apache License, Version 2.0 (the \"License\");"));
		assertTrue(worker.matches(" ## Licensed under the Apache License, Version 2.0 (the \"License\");"));
		assertTrue(worker.matches(" ## Licensed under the Apache License, Version 2.0 (the \"License\") ##);"));
		assertFalse(worker.matches("'Behold, Telemachus! (nor fear the sight,)"));
	}
	
	public void testMatch() throws Exception {
		ApacheSoftwareLicense20 worker = new ApacheSoftwareLicense20();
		final ISubject subject = new MockSubject("subject");
		assertTrue(worker.match(subject, ApacheSoftwareLicense20.FIRST_LICENSE_LINE, reporter));
        assertTrue(worker.match(subject, "    Licensed under the Apache License, Version 2.0 (the \"License\");", reporter));
        assertTrue(worker.match(subject, "Licensed under the Apache License, Version 2.0 (the \"License\");", reporter));
        assertTrue(worker.match(subject, " * Licensed under the Apache License, Version 2.0 (the \"License\");", reporter));
        assertTrue(worker.match(subject, " // Licensed under the Apache License, Version 2.0 (the \"License\");", reporter));
        assertTrue(worker.match(subject, " /* Licensed under the Apache License, Version 2.0 (the \"License\");", reporter));
        assertTrue(worker.match(subject, "    Licensed under the Apache License, Version 2.0 (the \"License\");", reporter));
        assertTrue(worker.match(subject, " ## Licensed under the Apache License, Version 2.0 (the \"License\");", reporter));
        assertTrue(worker.match(subject, " ## Licensed under the Apache License, Version 2.0 (the \"License\") ##);", reporter));
        assertFalse(worker.match(subject, "'Behold, Telemachus! (nor fear the sight,)", reporter));
	}
    
}
