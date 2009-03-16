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
package org.apache.rat.report.xml;

import java.io.File;
import java.io.StringWriter;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.rat.DirectoryWalker;
import org.apache.rat.analysis.MockLicenseMatcher;
import org.apache.rat.report.RatReport;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.claim.FileType;
import org.apache.rat.report.xml.writer.IXmlWriter;
import org.apache.rat.report.xml.writer.impl.base.XmlWriter;
import org.apache.rat.test.utils.Resources;

public class XmlReportFactoryTest extends TestCase {

    private static final Pattern IGNORE_EMPTY = Pattern.compile(".svn|Empty.txt");
    
    StringWriter out;
    IXmlWriter writer;
    
    protected void setUp() throws Exception {
        super.setUp();
        out = new StringWriter();
        writer = new XmlWriter(out);
        writer.startDocument();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    

    private void report(DirectoryWalker directory, RatReport report) throws Exception {
        directory.run(report);
    }
    
    public void testStandardReport() throws Exception {
    	final String elementsPath = Resources.getResourceDirectory("elements/Source.java");
        final MockLicenseMatcher mockLicenseMatcher = new MockLicenseMatcher();
        DirectoryWalker directory = new DirectoryWalker(new File(elementsPath), IGNORE_EMPTY);
        final ClaimStatistic statistic = new ClaimStatistic();
        RatReport report = XmlReportFactory.createStandardReport(writer, mockLicenseMatcher, statistic);
        report.startReport();
        report(directory, report);
        report.endReport();
        writer.closeDocument();
        final String output = out.toString();
        assertEquals(
                "<?xml version='1.0'?>" +
                "<rat-report>" +
                "<resource name='" + elementsPath + "/Image.png'><type name='binary'/></resource>" +
                "<resource name='" + elementsPath + "/LICENSE'><type name='notice'/></resource>" +
                "<resource name='" + elementsPath + "/NOTICE'><type name='notice'/></resource>" +
                "<resource name='" + elementsPath + "/Source.java'><type name='standard'/>" +
                "</resource>" +
                "<resource name='" + elementsPath + "/Text.txt'><type name='standard'/>" +
                "</resource>" +
                "<resource name='" + elementsPath + "/Xml.xml'><type name='standard'/>" +
                "</resource>" +
                "<resource name='" + elementsPath + "/dummy.jar'><type name='archive'/><archive-type name='readable'/></resource>" +
                "</rat-report>", output);
        assertTrue("Is well formed", XmlUtils.isWellFormedXml(output));
        assertEquals("Binary files", new Integer(1), statistic.getFileTypeMap().get(FileType.BINARY));
        assertEquals("Notice files", new Integer(2), statistic.getFileTypeMap().get(FileType.NOTICE));
        assertEquals("Standard files", new Integer(3), statistic.getFileTypeMap().get(FileType.STANDARD));
        assertEquals("Archives", new Integer(1), statistic.getFileTypeMap().get(FileType.ARCHIVE));
    }
}
