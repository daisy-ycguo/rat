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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintStream;
import java.io.Writer;

import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.rat.api.RatException;
import org.apache.rat.report.IReportable;
import org.apache.rat.report.RatReport;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.xml.XmlReportFactory;
import org.apache.rat.report.xml.writer.IXmlWriter;
import org.apache.rat.report.xml.writer.impl.base.XmlWriter;
import org.apache.rat.walker.ArchiveWalker;
import org.apache.rat.walker.DirectoryWalker;


public class Report {
    private static final char EXCLUDE_CLI = 'e';
    private static final char STYLESHEET_CLI = 's';

    //@SuppressWarnings("unchecked")
    public static final void main(String args[]) throws Exception {
        final ReportConfiguration configuration = new ReportConfiguration();
        Options opts = buildOptions();

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
                configuration.setAddingLicenses(true);
                configuration.setAddingLicensesForced(cl.hasOption('f'));
                configuration.setCopyrightMessage(cl.getOptionValue("c"));
            }

            if (cl.hasOption(EXCLUDE_CLI)) {
                String[] excludes = cl.getOptionValues(EXCLUDE_CLI);
                if (excludes != null) {
                    final FilenameFilter filter = new NotFileFilter(new WildcardFileFilter(excludes));
                    report.setInputFileFilter(filter);
                }
            }

            if (cl.hasOption('x')) {
                report.report(System.out);
            } else {
                if (!cl.hasOption(STYLESHEET_CLI)) {
                    report.styleReport(System.out);
                } else {
                    String[] style = cl.getOptionValues(STYLESHEET_CLI);
                    if (style.length != 1) {
                        System.err.println("please specify a single stylesheet");
                        System.exit(1);
                    }
                    try {
                        configuration.setHeaderMatcher(Defaults.createDefaultMatcher());
                        report(System.out,
                                report.getDirectory(System.out),
                                new FileInputStream(style[0]),
                                configuration);
                    } catch (FileNotFoundException fnfe) {
                        System.err.println("stylesheet " + style[0]
                                           + " doesn't exist");
                        System.exit(1);
                    }
                }
            }
        }
    }

    private static Options buildOptions() {
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
        "Forces any changes in files to be written directly to the source files (i.e. new files are not created)");
        opts.addOption(write);

        Option copyright = new Option(
                "c",
                "copyright",
                true,
        "The copyright message to use in the licence headers, usually in the form of \"Copyright 2008 Foo\"");
        opts.addOption(copyright);

        final Option exclude = OptionBuilder
                            .withArgName("expression")
                            .withLongOpt("exclude")
                            .hasArgs()
                            .withDescription("Excludes files matching <expression>. " +
                                    "Note that --dir is required when using this parameter. " +
                                    "Allows multiple arguments.")
                            .create(EXCLUDE_CLI);
        opts.addOption(exclude);

        Option dir = new Option(
                "d",
                "dir",
                false,
        "Used to indicate source when using --exclude");
        opts.addOption(dir);

        OptionGroup outputType = new OptionGroup();

        Option xml = new Option(
                "x",
                "xml",
                false,
                "Output the report in raw XML format.  Not compatible with -s");
        outputType.addOption(xml);

        Option xslt = new Option(String.valueOf(STYLESHEET_CLI),
                                 "stylesheet",
                                 true,
                                 "XSLT stylesheet to use when creating the"
                                 + " report.  Not compatible with -x");
        outputType.addOption(xslt);
        opts.addOptionGroup(outputType);

        return opts;
    }

    private static final void printUsage(Options opts) {
        HelpFormatter f = new HelpFormatter();
        String header = "Options";

        StringBuffer footer = new StringBuffer("\n");
        footer.append("NOTE:\n");
        footer.append("RAT is really little more than a grep ATM\n");
        footer.append("RAT is also rather memory hungry ATM\n");
        footer.append("RAT is very basic ATM\n");
        footer.append("RAT highlights possible issues\n");
        footer.append("RAT reports require intepretation\n");
        footer.append("RAT often requires some tuning before it runs well against a project\n");
        footer.append("RAT relies on heuristics: it may miss issues\n");

        f.printHelp("java rat.report [options] [DIR|TARBALL]",
                header, opts, footer.toString(), false);
        System.exit(0);
    }

    private final String baseDirectory;

    private FilenameFilter inputFileFilter = null;

    private Report(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    /**
     * Gets the current filter used to select files.
     * @return current file filter, or null when no filter has been set
     */
    public FilenameFilter getInputFileFilter() {
        return inputFileFilter;
    }

    /**
     * Sets the current filter used to select files.
     * @param inputFileFilter filter, or null when no filter has been set
     */
    public void setInputFileFilter(FilenameFilter inputFileFilter) {
        this.inputFileFilter = inputFileFilter;
    }

    public ClaimStatistic report(PrintStream out) throws Exception {
        final IReportable base = getDirectory(out);
        if (base != null) {
            final ReportConfiguration configuration = new ReportConfiguration();
            configuration.setHeaderMatcher(Defaults.createDefaultMatcher());
            return report(base, new OutputStreamWriter(out), configuration);
        }
        return null;
    }

    private IReportable getDirectory(PrintStream out) {
        File base = new File(baseDirectory);
        if (!base.exists()) {
            out.print("ERROR: ");
            out.print(baseDirectory);
            out.print(" does not exist.\n");
            return null;
        } 

        if (base.isDirectory()) {
            return new DirectoryWalker(base, inputFileFilter);
        }

        try {
                return new ArchiveWalker(base, inputFileFilter);
        } catch (IOException ex) {
            out.print("ERROR: ");
            out.print(baseDirectory);
            out.print(" is not valid gzip data.\n");
            return null;
        }
    }

    /**
     * Output a report in the default style and default licence
     * header matcher. 
     * 
     * @param out - the output stream to recieve the styled report
     * @throws Exception
     */
    public void styleReport(PrintStream out) throws Exception {
        final IReportable base = getDirectory(out);
        if (base != null) {
            InputStream style = Defaults.getDefaultStyleSheet();
            final ReportConfiguration configuration = new ReportConfiguration();
            configuration.setHeaderMatcher(Defaults.createDefaultMatcher());
            report(out, base, style, configuration);
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
     * @throws RatException
     */
    public static void report(PrintStream out, IReportable base, final InputStream style,
                              ReportConfiguration pConfiguration) 
            throws IOException, TransformerConfigurationException,  InterruptedException, RatException {
        report(new OutputStreamWriter(out), base, style, pConfiguration);
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
     * @throws RatException
     */
    public static ClaimStatistic report(Writer out, IReportable base, final InputStream style, 
            ReportConfiguration pConfiguration) 
    throws IOException, TransformerConfigurationException, FileNotFoundException, InterruptedException, RatException {
        PipedReader reader = new PipedReader();
        PipedWriter writer = new PipedWriter(reader);
        ReportTransformer transformer = new ReportTransformer(out, style, reader);
        Thread transformerThread = new Thread(transformer);
        transformerThread.start();
        final ClaimStatistic statistic = report(base, writer, pConfiguration);
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
     * @throws RatException
     */
    public static ClaimStatistic report(final IReportable container, final Writer out,
            ReportConfiguration pConfiguration) throws IOException, RatException {
        IXmlWriter writer = new XmlWriter(out);
        final ClaimStatistic statistic = new ClaimStatistic();
        RatReport report = XmlReportFactory.createStandardReport(writer, statistic, pConfiguration);
        report.startReport();
        container.run(report);
        report.endReport();
        writer.closeDocument();
        return statistic;
    }
}
