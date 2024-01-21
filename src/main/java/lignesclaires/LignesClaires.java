/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.logging.Level;
import java.util.logging.Logger;

import lignesclaires.cmd.OptionsParser;
import lignesclaires.cmd.Verbosity;
import lignesclaires.config.LignesClairesConfig;
import lignesclaires.graph.BlockCutForest;
import lignesclaires.graph.BlockDecomposition;
import lignesclaires.graph.DepthFirstSearch;
import lignesclaires.graph.ForestDFS;
import lignesclaires.parser.InvalidGraphFormatException;
import lignesclaires.parser.PaceInputParser;
import lignesclaires.solver.OCSolution;
import lignesclaires.solver.OCSolver;
import lignesclaires.solver.OCSolverException;
import lignesclaires.solver.Status;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IBipartiteGraphDimension;
import lignesclaires.specs.IOCSolver;

public final class LignesClaires {

	public static final Logger LOGGER = Logger.getLogger(LignesClaires.class.getName());

	private static final String FAIL = " [FAIL";

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

			final Optional<IBipartiteGraph> optGraph = parse(config.getGraphFile());
			if (optGraph.isPresent()) {
				if (config.exportBlockCutTree()) {
					exportBlockCutTree(optGraph.get(), config.getGraphFile());
				}
				final OCSolution solution = solve(optGraph.get(), config);
				final Optional<String> optsol = config.getSolutionFile();
				if (optsol.isPresent()) {
					exportSolution(optsol.get(), solution);
				}
				return solution.getStatus() == Status.ERROR ? 1 : 0;
			} else {
				return 1;
			}

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

	private static Optional<IBipartiteGraph> parse(final String graphfile) {
		try {
			final PaceInputParser parser = new PaceInputParser();
			final File file = new File(graphfile);
			final IBipartiteGraph bigraph = parser.parse(file);
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.log(Level.INFO, "Parse graph [OK]\ni {0}\n{1}",
						new Object[] { getFilenameWithoutExtension(file), toDimacs(bigraph) });
				if (LOGGER.isLoggable(Level.CONFIG)) {
					LOGGER.log(Level.CONFIG, "Display graph:\n{0}", bigraph);
				}
			}
			return Optional.of(bigraph);
		} catch (InvalidGraphFormatException | FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, e, () -> "Parse file " + graphfile + FAIL);
		}
		return Optional.empty();
	}

	public static void writeString(final String content, final String filePath) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			// Write the string to the file
			writer.write(content);
			LOGGER.log(Level.INFO, "Export file {0} [OK]", filePath);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, e, () -> "Write file " + filePath + FAIL);
		}
	}

	public static String getFilenameWithoutExtension(String path) {
		return getFilenameWithoutExtension(new File(path));
	}

	public static String getFilenameWithoutExtension(File file) {
		final String name = file.getName();
		final int idx = name.lastIndexOf('.');
		return idx < 0 ? name : name.substring(0, idx);
	}

	private static void exportBlockCutTree(IBipartiteGraph graph, final String graphfile) {
		final String graphname = getFilenameWithoutExtension(graphfile);
		final DepthFirstSearch dfs = new DepthFirstSearch();
		final ForestDFS f = dfs.execute(graph);
		writeString(f.toDotty(), graphname + "-forest.dot");

		final BlockDecomposition bdec = new BlockDecomposition();
		final BlockCutForest d = bdec.execute(f);
		writeString(d.toDotty(), graphname + "-bctree.dot");
	}

	private static OCSolution solve(final IBipartiteGraph bigraph, final LignesClairesConfig config) {
		try {
			final IOCSolver solver = buildSolver(config);
			final OCSolution solution = solver.solve(bigraph, config);
			LOGGER.log(Level.INFO, "Solve OCM [{0}]", solution.getStatus());
			return solution;
		} catch (OCSolverException e) {
			LOGGER.log(Level.SEVERE, "Solve OCM [FAIL]", e);
		}
		return new OCSolution();
	}

	private static void exportSolution(final String solfile, final OCSolution solution) {
		try (FileWriter fileWriter = new FileWriter(new File(solfile), false)) {
			fileWriter.append(solution.toString());
			LOGGER.log(Level.INFO, "Export solution to file {0} [OK]", solfile);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e, () -> "Export solution to file " + solfile + " [FAIL]");
		}
	}

	public static String toDimacs(final IBipartiteGraphDimension dim) {
		return String.format("c FIXED %d%nc FREE %d%nc EDGES %d", dim.getFixedCount(), dim.getFreeCount(),
				dim.getEdgeCount());
	}

}
