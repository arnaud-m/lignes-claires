/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires;

import java.io.FileNotFoundException;
import java.util.OptionalInt;
import java.util.logging.Level;
import java.util.logging.Logger;

import lignesclaires.cmd.OptionsParser;
import lignesclaires.cmd.Verbosity;
import lignesclaires.config.LignesClairesConfig;
import lignesclaires.parser.BiGraphParser;
import lignesclaires.parser.InvalidGraphFormatException;
import lignesclaires.solver.OCSolver;
import lignesclaires.solver.OCSolverException;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IOCSolver;

public final class LignesClaires {

	public static final Logger LOGGER = Logger.getLogger(LignesClaires.class.getName());

	private LignesClaires() {
	}

	public static void main(final String[] args) {
		JULogUtil.configureDefaultLoggers();
		final int exitCode = doMain(args);
		System.exit(exitCode);
	}

	public static int doMain(final String[] args) {
		try {
			final OptionsParser optparser = new OptionsParser(LignesClaires.class, new LignesClairesConfig(), "FILE");
			final OptionalInt parserExitCode = optparser.parseOptions(args);
			if (parserExitCode.isPresent()) {
				return parserExitCode.getAsInt();
			}
			final LignesClairesConfig config = optparser.getConfig();
			configureVerbosity(config.getVerbosity());
			final IOCSolver solver = buildSolver(config);
			final BiGraphParser parser = new BiGraphParser();

			int exitCode = 0;
			for (String filepath : config.getArguments()) {
				exitCode += solve(filepath, parser, solver, config);
			}
			return exitCode;
		} finally {
			JULogUtil.flushLogs();
		}
	}

	private static void configureVerbosity(Verbosity verbosity) {
		switch (verbosity) {
		case SILENT:
			JULogUtil.configureLoggers(Level.OFF);
			break;
		case QUIET:
			JULogUtil.configureLoggers(Level.WARNING);
			break;
		case NORMAL:
			JULogUtil.configureLoggers(Level.INFO);
			break;
		case VERBOSE:
			JULogUtil.configureLoggers(Level.CONFIG);
			break;
		case VERY_VERBOSE:
			JULogUtil.configureLoggers(Level.FINE);
			break;
		case DEBUG:
			JULogUtil.configureLoggers(Level.ALL);
			break;
		default:
			break;
		}
	}

	private static IOCSolver buildSolver(final LignesClairesConfig config) {
		return new OCSolver();
	}

	private static int solve(final String graphfile, final BiGraphParser parser, final IOCSolver solver,
			final LignesClairesConfig config) {
		try {
			final IBipartiteGraph bigraph = parser.parse(graphfile);
			LOGGER.log(Level.INFO, "Parse graph file [OK]\n{0}:\n{1}", new Object[] { graphfile, bigraph });
			final boolean solved = solver.solve(bigraph, config);
			LOGGER.log(Level.INFO, "Solve OCM [{1}]\n{0}", new Object[] { graphfile, solved });
			return 0;
		} catch (InvalidGraphFormatException | FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, e, () -> "Parse file " + graphfile + " [FAIL]");
		} catch (OCSolverException e) {
			LOGGER.log(Level.SEVERE, "Choco OCM [FAIL]", e);
		}
		return 1;
	}

}
