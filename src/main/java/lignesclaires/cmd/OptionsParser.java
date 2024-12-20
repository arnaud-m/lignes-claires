/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.cmd;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Comparator;
import java.util.OptionalInt;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.OptionHandlerFilter;
import org.kohsuke.args4j.ParserProperties;
import org.kohsuke.args4j.spi.OptionHandler;

import lignesclaires.config.LignesClairesConfig;

public class OptionsParser {

	private static final OptionalInt ERROR_CODE = OptionalInt.of(64);
	private static final OptionalInt EXIT_CODE = OptionalInt.of(0);
	private static final OptionalInt EMPTY_CODE = OptionalInt.empty();

	private final Class<?> mainClass;

	protected final LignesClairesConfig config;

	private final String argumentName;

	public OptionsParser(final Class<?> mainClass, final LignesClairesConfig config, final String argumentName) {
		super();
		this.mainClass = mainClass;
		this.config = config;
		this.argumentName = argumentName;
	}

	public final Logger getLogger() {
		return Logger.getLogger(mainClass.getName());
	}

	private String getCommandName() {
		return mainClass.getName();
	}

	private String getArgumentName() {
		return argumentName;
	}

	protected void configureLoggers() {
		// TODO
	}

	protected boolean checkConfiguration() {
		return true;
	}

	protected boolean checkArguments() {
		return config.getArguments().size() < 3;
	}

	public final LignesClairesConfig getConfig() {
		return config;
	}

	public final OptionalInt parseOptions(final String[] args) {
		final ParserProperties properties = ParserProperties.defaults().withOptionSorter(new DefaultOptionSorter());
		final CmdLineParser parser = new CmdLineParser(config, properties);
		try {
			// parse the arguments.
			parser.parseArgument(args);
			configureLoggers();
			if (config.isDisplayHelp()) {
				logManual(parser);
				return EXIT_CODE;
			} else if (checkConfiguration()) {
				getLogger().log(Level.CONFIG, "Parse options [OK]");
				if (checkArguments()) {
					getLogger().log(Level.CONFIG, "Parse arguments [OK]");
					return EMPTY_CODE;
				} else {
					getLogger().log(Level.SEVERE, "Parse arguments [FAIL]\n{0}", config.getArguments());
				}
			} else {
				getLogger().log(Level.SEVERE, "Check options [FAIL]");
			}

		} catch (CmdLineException e) {
			getLogger().log(Level.SEVERE, "Parse options [FAIL]", e);
		}
		logHelp(parser);
		return ERROR_CODE;
	}

	private String printUsage(final CmdLineParser parser, final OptionHandlerFilter filter) {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		parser.printUsage(new OutputStreamWriter(os), null, filter);
		return os.toString();
	}

	private void appendExample(final StringBuilder b, final CmdLineParser parser, final OptionHandlerFilter filter) {
		b.append("java ").append(getCommandName()).append(' ');
		final String opts = parser.printExample(filter);
		if (opts.length() > 0) {
			b.append(opts).append(" ");
		}
		if (filter == OptionHandlerFilter.REQUIRED) {
			b.append("[options...] ");
		}
		b.append(getArgumentName()).append('\n');
	}

	private void appendHeader(final StringBuilder b) {
		b.append("Help of ").append(getCommandName()).append("\n");
	}

	private void appendMessage(final StringBuilder b) {
		// appendResource(b, "help/" + getCommandName() + ".txt");
	}

	private void appendExamples(final StringBuilder b) {
		// appendResource(b, "help/" + getCommandName() + "-examples.txt");
	}

	private void appendResource(final StringBuilder b, final String resourceName) {
		final InputStream in = mainClass.getClassLoader().getResourceAsStream(resourceName);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		reader.lines().forEach(x -> b.append(x).append('\n'));
	}

	private void appendFooter(final StringBuilder b) {
		b.append("\nReport bugs: <https://github.com/arnaud-m/lignes-claires/issues>\n");
		b.append("Lignes-claires home page: <https://github.com/arnaud-m/lignes-claires>\n");
	}

	private void appendShortFooter(final StringBuilder b) {
		b.append("\nTry the option '--help' for more information.\n");
	}

	private void logHelp(final CmdLineParser parser) {
		if (getLogger().isLoggable(Level.INFO)) {
			final StringBuilder b = new StringBuilder();
			appendHeader(b);
			appendExample(b, parser, OptionHandlerFilter.REQUIRED);
			b.append("\n");
			appendMessage(b);
			b.append("\nPublic options:\n");
			b.append(printUsage(parser, OptionHandlerFilter.PUBLIC));
			appendShortFooter(b);
			getLogger().info(b.toString());
		}
	}

	private void logManual(final CmdLineParser parser) {
		if (getLogger().isLoggable(Level.INFO)) {
			final StringBuilder b = new StringBuilder();
			appendHeader(b);
			b.append("SYNOPSIS\n");
			appendExample(b, parser, OptionHandlerFilter.REQUIRED);
			b.append("\n");
			appendExample(b, parser, OptionHandlerFilter.PUBLIC);
			appendExample(b, parser, OptionHandlerFilter.ALL);
			b.append("\nDESCRIPTION\n");
			appendMessage(b);
			b.append("\nOPTIONS\n");
			b.append(printUsage(parser, OptionHandlerFilter.ALL));
			b.append("\nEXAMPLE\n");
			appendExamples(b);
			appendFooter(b);
			getLogger().info(b.toString());
		}

	}

	private static class DefaultOptionSorter implements Comparator<OptionHandler> {

		private boolean longOption(final OptionDef x) {
			return x.toString().charAt(1) == '-';
		}

		@Override
		public int compare(final OptionHandler arg0, final OptionHandler arg1) {
			final OptionDef x = arg0.option;
			final OptionDef y = arg1.option;
			// Required option
			int comp = Boolean.compare(x.required(), y.required());
			if (comp == 0) {
				// Hidden options
				comp = Boolean.compare(x.hidden(), y.hidden());
				if (comp == 0) {
					// Long options
					comp = Boolean.compare(longOption(x), longOption(y));
					if (comp == 0) {
						// Option strings
						comp = x.toString().compareTo(y.toString());
					}
				}
			}
			return comp;
		}
	}

}
