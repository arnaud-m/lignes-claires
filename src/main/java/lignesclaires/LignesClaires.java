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
import lignesclaires.config.PaceConfig;
import lignesclaires.parser.BiGraphParser;
import lignesclaires.parser.InvalidGraphFormatException;
import lignesclaires.solver.CryptaChocoException;
import lignesclaires.solver.PaceSolver;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IPaceSolver;

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
			final OptionsParser optparser = new OptionsParser(LignesClaires.class, new PaceConfig(), "FILE");
			final OptionalInt parserExitCode = optparser.parseOptions(args);
			if (parserExitCode.isPresent()) {
				return parserExitCode.getAsInt();
			}
			final PaceConfig config = optparser.getConfig();
			final IPaceSolver solver = buildSolver(config);
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

	private static IPaceSolver buildSolver(final PaceConfig config) {
		return new PaceSolver();
	}

	private static int solve(final String graphfile, final BiGraphParser parser, final IPaceSolver solver,
			final PaceConfig config) {
		try {
			final IBipartiteGraph bigraph = parser.parse(graphfile);
			LOGGER.log(Level.INFO, "Parse file [OK]\n{0}:\n{1}", new Object[] { graphfile, bigraph });
			final boolean solved = solver.solve(bigraph, config);
			LOGGER.log(Level.INFO, "Solve OCM [{1}]\n{0}", new Object[] { graphfile, solved });
			return 0;
		} catch (InvalidGraphFormatException | FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, e, () -> "Parse file " + graphfile + " [FAIL]");
		} catch (CryptaChocoException e) {
			LOGGER.log(Level.SEVERE, "Choco OCM [FAIL]", e);
		}
		return 1;
	}

}
