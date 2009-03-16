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

package org.apache.rat.report.analyser;

import java.io.StringReader;

import junit.framework.TestCase;

import org.apache.rat.analysis.license.ApacheSoftwareLicense20;
import org.apache.rat.document.IResource;
import org.apache.rat.document.MockLocation;
import org.apache.rat.report.claim.impl.xml.MockClaimReporter;

public class HeaderCheckWorkerTest extends TestCase {
	MockClaimReporter reporter;
    
	protected void setUp() throws Exception {
		super.setUp();
        reporter = new MockClaimReporter();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testIsFinished() throws Exception {
        final IResource subject = new MockLocation("subject");
		HeaderCheckWorker worker = new HeaderCheckWorker(new StringReader(""), new ApacheSoftwareLicense20(), reporter, subject);
		assertFalse(worker.isFinished());
		worker.read();
		assertTrue(worker.isFinished());
	}
}
