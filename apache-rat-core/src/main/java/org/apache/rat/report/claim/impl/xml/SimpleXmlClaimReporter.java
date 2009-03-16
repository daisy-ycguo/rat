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

import java.io.IOException;

import org.apache.rat.document.IResource;
import org.apache.rat.report.RatReportFailedException;
import org.apache.rat.report.claim.IClaim;
import org.apache.rat.report.claim.IClaimReporter;
import org.apache.rat.report.claim.impl.ArchiveFileTypeClaim;
import org.apache.rat.report.claim.impl.FileTypeClaim;
import org.apache.rat.report.claim.impl.LicenseApprovalClaim;
import org.apache.rat.report.claim.impl.LicenseFamilyClaim;
import org.apache.rat.report.claim.impl.LicenseHeaderClaim;
import org.apache.rat.report.xml.writer.IXmlWriter;

public class SimpleXmlClaimReporter implements IClaimReporter {
    public static final String LICENSE_APPROVAL_PREDICATE = "license-approval";
    public static final String LICENSE_FAMILY_PREDICATE = "license-family";
    public static final String HEADER_SAMPLE_PREDICATE = "header-sample";
    public static final String HEADER_TYPE_PREDICATE = "header-type";
    public static final String FILE_TYPE_PREDICATE = "type";
    public static final String ARCHIVE_TYPE_PREDICATE = "archive-type";
    public static final String ARCHIVE_TYPE_UNREADABLE = "unreadable";
    public static final String ARCHIVE_TYPE_READABLE = "readable";

    private static final String NAME = "name";
    private final IXmlWriter writer;
    private IResource lastSubject;
    
    public SimpleXmlClaimReporter(final IXmlWriter writer) {
        this.writer = writer;
    }

    protected void handleClaim(ArchiveFileTypeClaim pClaim)
            throws IOException, RatReportFailedException {
        handleClaim((FileTypeClaim) pClaim);
        writeClaim(ARCHIVE_TYPE_PREDICATE, pClaim.isReadable() ? ARCHIVE_TYPE_READABLE : ARCHIVE_TYPE_UNREADABLE, false);
    }

    protected void handleClaim(FileTypeClaim pClaim)
            throws IOException, RatReportFailedException {
        writeClaim(FILE_TYPE_PREDICATE, pClaim.getType().getName(), false);
    }

    protected void handleClaim(LicenseApprovalClaim pClaim)
            throws IOException, RatReportFailedException {
        writeClaim(LICENSE_APPROVAL_PREDICATE, Boolean.toString(pClaim.isApproved()), false);
    }

    protected void handleClaim(LicenseFamilyClaim pClaim)
            throws IOException, RatReportFailedException {
        handleClaim((LicenseHeaderClaim) pClaim);
        writeClaim(LICENSE_FAMILY_PREDICATE, pClaim.getLicenseFamilyName().getName(), false);
    }

    protected void handleClaim(LicenseHeaderClaim pClaim)
            throws IOException, RatReportFailedException {
        writeClaim(HEADER_SAMPLE_PREDICATE, pClaim.getHeaderSample(), true);
        writeClaim(HEADER_TYPE_PREDICATE, pClaim.getLicenseFamilyCode().getName(), false);
    }

    protected void handleClaim(CustomClaim pClaim)
            throws IOException, RatReportFailedException {
        writeClaim(pClaim.getPredicate(), pClaim.getObject(), pClaim.isLiteral());
    }

    /**
     * Writes a single claim to the XML file.
     * @param pPredicate The claims predicate.
     * @param pObject The claims object.
     * @param pLiteral Whether to write the object as an element (true),
     *   or an attribute (false).
     * @throws IOException An I/O error occurred while writing the claim.
     * @throws RatReportFailedException Another error occurred while writing the claim.
     */
    protected void writeClaim(String pPredicate, String pObject, boolean pLiteral)
            throws IOException, RatReportFailedException {
        if (pLiteral) {
            writer.openElement(pPredicate).content(pObject).closeElement();
        } else {
            writer.openElement(pPredicate).attribute(NAME, pObject).closeElement();
        }
    }
    
    protected void handleClaim(IClaim pClaim) throws IOException, RatReportFailedException {
        if (pClaim instanceof ArchiveFileTypeClaim) {
            handleClaim((ArchiveFileTypeClaim) pClaim);
        } else if (pClaim instanceof FileTypeClaim) {
            handleClaim((FileTypeClaim) pClaim);
        } else if (pClaim instanceof LicenseApprovalClaim) {
            handleClaim((LicenseApprovalClaim) pClaim);
        } else if (pClaim instanceof LicenseFamilyClaim) {
            handleClaim((LicenseFamilyClaim) pClaim);
        } else if (pClaim instanceof LicenseHeaderClaim) {
            handleClaim((LicenseHeaderClaim) pClaim);
        } else if (pClaim instanceof CustomClaim) {
            handleClaim((CustomClaim) pClaim);
        } else {
            throw new IllegalStateException("Invalid claim type: " + pClaim.getClass().getName());
        }
    }

    public void claim(IClaim pClaim) throws RatReportFailedException {
        final IResource subject = pClaim.getSubject();
        try {
            if (!(subject.equals(lastSubject))) {
                if (lastSubject != null) {
                    writer.closeElement();
                }
                writer.openElement("resource").attribute(NAME, subject.getName());
            }
            handleClaim(pClaim);
            lastSubject = subject;
        } catch (IOException e) {
            throw new RatReportFailedException("XML writing failure: " + e.getMessage()
                    + " subject: " + subject + " claim type: "
                    + pClaim.getClass().getName(), e);
        }
    }

}
