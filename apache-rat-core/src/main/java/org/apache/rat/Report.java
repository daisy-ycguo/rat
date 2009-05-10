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
package org.apache.rat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintStream;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.annotation.AbstractLicenceAppender;
import org.apache.rat.annotation.ApacheV2LicenceAppender;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.report.IReportable;
import org.apache.rat.report.RatReport;
import org.apache.rat.report.RatReportFailedException;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.xml.XmlReportFactory;
import org.apache.rat.report.xml.writer.IXmlWriter;
import org.apache.rat.report.xml.writer.impl.base.XmlWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Report {

	//@SuppressWarnings("unchecked")
  public static final void main(String args[]) throws Exception {
	  Options opts = new Options();

    Option help = new Option("h", "help", false,
        "Print help for the RAT command line interface and exit");
    opts.addOption(help);

    Option addLicence = new Option(
        "a",
        "addLicence",
        false,
        "Add the default licence header to any file with an unknown licence that is not in the exclusion list. By default new files will be created with the licence header, to force the modification of existing files use the --force option.");
    opts.addOption(addLicence);
    
    Option write = new Option(
        "f",
        "force",
        false,
        "Forces any changes in files to be written without confirmation");
    opts.addOption(write);

    Option copyright = new Option(
        "c",
        "copyright",
        true,
        "The copyright message to use in the licence headers, usually in the form of \"Copyright 2008 Foo\"");
    opts.addOption(copyright);
    
    Option xml = new Option(
        "x",
        "xml",
        false,
        "Output the report in XML format");
    opts.addOption(xml);
    
    PosixParser parser = new PosixParser();
    CommandLine cl = null;
    try {
      cl = parser.parse(opts, args);
    } catch (ParseException e) {
      System.err.println("Please use the \"--help\" option to see a list of valid commands and options");
      System.exit(1);
    }

    if (cl.hasOption('h')) {
      printUsage(opts);
    }
    
    
    args = cl.getArgs();
    if (args == null || args.length != 1) {
			printUsage(opts);
		} else {
      Report report = new Report(args[0]);
      
      if (cl.hasOption('a')) {
        OutputStream reportOutput = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(reportOutput, true);
        report.report(stream);
        
        AbstractLicenceAppender  appender;
        String copyrightMsg = cl.getOptionValue("c");
        if ( copyrightMsg != null) {
          appender = new ApacheV2LicenceAppender(copyrightMsg);
        } else {
          appender = new ApacheV2LicenceAppender();
        }
        if (cl.hasOption("f")) {
          appender.setForce(true);
        }
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        ByteArrayInputStream xmlStream = new ByteArrayInputStream(reportOutput.toString().getBytes("UTF-8"));
        Document doc = factory.newDocumentBuilder().parse(xmlStream);
        
        NodeList resourceHeaders = doc.getElementsByTagName("header-type");
        String value = null;
        for (int i = 0; i < resourceHeaders.getLength(); i++) {
          Node headerType = resourceHeaders.item(i).getAttributes().getNamedItem("name");
          if(headerType != null) {
            value = headerType.getNodeValue();
          } else {
            value = null;
          }
          if (value != null &&value.equals("?????")) {
            Node resource = resourceHeaders.item(i).getParentNode();
            String filename = resource.getAttributes().getNamedItem("name").getNodeValue();
            File document = new File(filename);
            appender.append(document);
          }
        }
      }
      
      if (cl.hasOption('x')) {
			  report.report(System.out);
		  } else {
        report.styleReport(System.out);
		  }		  
		} 
	}
	
	private static final void printUsage(Options opts) {
	    HelpFormatter f = new HelpFormatter();
	    String header = "Options";
    
	    StringBuffer footer = new StringBuffer("\n");
	    footer.append("NOTE:\n");
	    footer.append("RAT is really little more than a grep ATM\n");
	    footer.append("RAT is also rather memory hungry ATM\n");
	    footer.append("RAT is very basic ATM\n");
	    footer.append("RAT ATM runs on unpacked releases\n");
	    footer.append("RAT highlights possible issues\n");
	    footer.append("RAT reports require intepretation\n");
	    footer.append("RAT often requires some tuning before it runs well against a project\n");
	    footer.append("RAT relies on heuristics: it may miss issues\n");

	    f.printHelp("java rat.report [options] [DIR]",
	            header, opts, footer.toString(), false);
	    System.exit(0);
	}
	
	private final String baseDirectory;
	
	private Report(String baseDirectory) {
		this.baseDirectory = baseDirectory;
	}
	
	public ClaimStatistic report(PrintStream out) throws Exception {
        DirectoryWalker base = getDirectory(out);
        if (base != null) {
            return report(base, new OutputStreamWriter(out), Defaults.createDefaultMatcher(), null);
        }
        return null;
	}
    
    private DirectoryWalker getDirectory(PrintStream out) {
        DirectoryWalker result = null;
        File base = new File(baseDirectory);
        if (!base.exists()) {
            out.print("ERROR: ");
            out.print(baseDirectory);
            out.print(" does not exist.\n");
        } else if (!base.isDirectory()) {
            out.print("ERROR: ");
            out.print(baseDirectory);
            out.print(" must be a directory.\n");
        } else {
            result = new DirectoryWalker(base);
        }
        return result;
    }
    
    /**
     * Output a report in the default style and default licence
     * header matcher. 
     * 
     * @param out - the output stream to recieve the styled report
     * @throws Exception
     */
    public void styleReport(PrintStream out) throws Exception {
        DirectoryWalker base = getDirectory(out);
        if (base != null) {
            InputStream style = Defaults.getDefaultStyleSheet();
            report(out, base, style, Defaults.createDefaultMatcher(), null);
        }
    }

    /**
     * Output a report that is styled using a defined stylesheet.
     * 
     * @param out the stream to write the report to
     * @param base the files or directories to report on
     * @param style an input stream representing the stylesheet to use for styling the report
     * @param matcher the header matcher for matching licence headers
     * @param approvedLicenseNames a list of licence families that are approved for use in the project
     * @throws IOException
     * @throws TransformerConfigurationException
     * @throws InterruptedException
     * @throws RatReportFailedException
     */
    public static void report(PrintStream out, IReportable base, final InputStream style, final IHeaderMatcher matcher,
            final ILicenseFamily[] approvedLicenseNames) 
           throws IOException, TransformerConfigurationException, 
           InterruptedException, RatReportFailedException {
        report(new OutputStreamWriter(out), base, style, matcher, approvedLicenseNames);
    }

    /**
     * 
     * Output a report that is styled using a defined stylesheet.
     * 
     * @param out the writer to write the report to
     * @param base the files or directories to report on
     * @param style an input stream representing the stylesheet to use for styling the report
     * @param matcher the header matcher for matching licence headers
     * @param approvedLicenseNames a list of licence families that are approved for use in the project
     * @throws IOException
     * @throws TransformerConfigurationException
     * @throws FileNotFoundException
     * @throws InterruptedException
     * @throws RatReportFailedException
     */
    public static ClaimStatistic report(Writer out, IReportable base, final InputStream style, 
            final IHeaderMatcher matcher, final ILicenseFamily[] approvedLicenseNames) 
                throws IOException, TransformerConfigurationException, FileNotFoundException, InterruptedException, RatReportFailedException {
        PipedReader reader = new PipedReader();
        PipedWriter writer = new PipedWriter(reader);
        ReportTransformer transformer = new ReportTransformer(out, style, reader);
        Thread transformerThread = new Thread(transformer);
        transformerThread.start();
        final ClaimStatistic statistic = report(base, writer, matcher, approvedLicenseNames);
        writer.flush();
        writer.close();
        transformerThread.join();
        return statistic;
    }
    
    /**
     * 
     * @param container the files or directories to report on
     * @param out the writer to write the report to
     * @param matcher the header matcher for matching licence headers
     * @param approvedLicenseNames a list of licence families that are approved for use in the project
     * @throws IOException
     * @throws RatReportFailedException
     */
    public static ClaimStatistic report(final IReportable container, final Writer out, final IHeaderMatcher matcher,
             final ILicenseFamily[] approvedLicenseNames) throws IOException, RatReportFailedException {
        IXmlWriter writer = new XmlWriter(out);
        final ClaimStatistic statistic = new ClaimStatistic();
        RatReport report = XmlReportFactory.createStandardReport(writer, matcher, approvedLicenseNames, statistic);  
        report.startReport();
        container.run(report);
        report.endReport();
        writer.closeDocument();
        return statistic;
    }
}
