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
package org.apache.rat.report.claim.impl.xml;

import java.util.ArrayList;
import java.util.List;

import org.apache.rat.report.RatReportFailedException;
import org.apache.rat.report.claim.IClaimReporter;
import org.apache.rat.report.claim.IObject;
import org.apache.rat.report.claim.IPredicate;
import org.apache.rat.report.claim.ISubject;

public class MockClaimReporter implements IClaimReporter {

    public List claims = new ArrayList();
    
    public void claim(ISubject subject, IPredicate predicate,
            IObject object, boolean isLiteral)
            throws RatReportFailedException {
        claims.add(new Claim(subject, predicate, object, isLiteral));
    }

    public Claim getClaim(int index) {
        return (Claim) claims.get(index);
    }
    
    public static class Claim {
        public final ISubject subject;
        public final IPredicate predicate;
        public final IObject object;
        public final boolean isLiteral;

        public Claim(final ISubject subject, final IPredicate predicate, final IObject object, final boolean isLiteral) {
            this.subject = subject;
            this.predicate = predicate;
            this.object = object;
            this.isLiteral = isLiteral;
        }
        
        public boolean equals(Object obj) {
            boolean result = false;
            if (obj instanceof Claim) {
                Claim claim = (Claim) obj;
                result = subject.equals(claim.subject) && predicate.equals(claim.predicate) 
                && object.equals(claim.object) && isLiteral == claim.isLiteral;
            }
            return result;
        }
        
        
    }
}
