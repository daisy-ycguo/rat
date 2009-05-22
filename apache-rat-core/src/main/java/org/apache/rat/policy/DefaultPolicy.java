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
package org.apache.rat.policy;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.rat.document.IDocument;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.report.RatReportFailedException;
import org.apache.rat.report.claim.IClaim;
import org.apache.rat.report.claim.IClaimReporter;
import org.apache.rat.report.claim.LicenseFamilyName;
import org.apache.rat.report.claim.impl.LicenseApprovalClaim;
import org.apache.rat.report.claim.impl.LicenseFamilyClaim;

public class DefaultPolicy implements IClaimReporter {
    private static final LicenseFamilyName[] APPROVED_LICENSES = {
        LicenseFamilyName.APACHE_SOFTWARE_LICENSE_NAME, LicenseFamilyName.OASIS_OPEN_LICENSE_NAME, 
        LicenseFamilyName.W3C_SOFTWARE_COPYRIGHT_NAME, LicenseFamilyName.W3C_DOCUMENT_COPYRIGHT_NAME,
        LicenseFamilyName.MODIFIED_BSD_LICENSE_NAME
    };
    
    private static final LicenseFamilyName[] toNames(final ILicenseFamily[] approvedLicenses) {
        LicenseFamilyName[] results = null;
        if (approvedLicenses != null) {
            final int length = approvedLicenses.length;
            results = new LicenseFamilyName[length];
            for (int i=0;i<length;i++) {
                results[i] = approvedLicenses[i].getFamilyName();
            }
        }
        return results;
    }
    
    private final IClaimReporter reporter;
    private final LicenseFamilyName[] approvedLicenseNames;
    private IDocument subject;
    
    public DefaultPolicy(final IClaimReporter reporter) {
        this(reporter, APPROVED_LICENSES);
    }
    
    public DefaultPolicy(final IClaimReporter reporter, final ILicenseFamily[] approvedLicenses) {
        this(reporter, toNames(approvedLicenses));
    }

    private static final Comparator licenseFamilyComparator = new Comparator(){
        public int compare(Object arg0, Object arg1) {
            return ((LicenseFamilyName) arg0).getName().compareTo(((LicenseFamilyName) arg1).getName());
        }
    };
    
    public DefaultPolicy(final IClaimReporter reporter, final LicenseFamilyName[] approvedLicenseNames) {
        this.reporter = reporter;
        if (approvedLicenseNames == null) {
            this.approvedLicenseNames = APPROVED_LICENSES;
        } else {
            final int length = approvedLicenseNames.length;
            this.approvedLicenseNames = new LicenseFamilyName[length];
            System.arraycopy(approvedLicenseNames, 0, this.approvedLicenseNames, 0, length);
        }
        Arrays.sort(this.approvedLicenseNames, licenseFamilyComparator);
    }

    public void claim(IClaim pClaim)
            throws RatReportFailedException {
        if (pClaim instanceof LicenseFamilyClaim) {
            final LicenseFamilyClaim lfc = (LicenseFamilyClaim) pClaim;
            final boolean isApproved = Arrays.binarySearch(approvedLicenseNames, lfc.getLicenseFamilyName(), licenseFamilyComparator) >= 0;
            reportLicenseApprovalClaim(subject, isApproved, reporter);
        }
    }

    public void reportLicenseApprovalClaim(final IDocument subject, final boolean isAcceptable, final IClaimReporter reporter) throws RatReportFailedException {
        reporter.claim(new LicenseApprovalClaim(subject, isAcceptable));
    }
    
    public void report(final IDocument subject) throws RatReportFailedException {
        this.subject = subject;
    }
}
