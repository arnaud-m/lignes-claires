/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgrapht.Graph;
import org.jgrapht.nio.GraphExporter;
import org.jgrapht.nio.ImportException;

import lignesclaires.cmd.OptionsParser;
import lignesclaires.cmd.Verbosity;
import lignesclaires.config.LignesClairesConfig;
import lignesclaires.graph.GraphLogger;
import lignesclaires.graph.JGraphtUtil;
import lignesclaires.parser.PaceInputParser;
import lignesclaires.solver.HeuristicSolver;
import lignesclaires.solver.OCSearchFlag;
import lignesclaires.solver.OCSolution;
import lignesclaires.solver.OCSolver;
import lignesclaires.solver.OCSolverException;
import lignesclaires.solver.Status;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IBipartiteGraphDimension;
import lignesclaires.specs.IOCSolver;

public final class LignesClaires {

	public static final Logger LOGGER = Logger.getLogger(LignesClaires.class.getName());

	private static final String FAIL = " [FAIL]";

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
			LOGGER.log(Level.INFO, "Read configuration [OK]\n{0}", config);
			final Optional<IBipartiteGraph> optGraph = parse(config.getInputFile(), config.getInputName());
			if (optGraph.isPresent()) {
				if (config.isReport()) {
					exportBlockCutGraph(optGraph.get(), config.getInputName());
				}
				final OCSolution solution = solve(optGraph.get(), config);
				exportPaceOutput(config.getOutputFile(), solution);
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
			JULogUtil.configureLoggers(Level.FINER);
			break;
		case TRACE:
			JULogUtil.configureLoggers(Level.ALL);
			break;
		default:
			break;
		}
	}

	private static IOCSolver buildSolver(final LignesClairesConfig config) {
		return new OCSolver();
	}

	private static void logOnInputGraph(final String inputName, final IBipartiteGraph inputGraph) {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.log(Level.INFO, "Parse graph [OK]\ni {0}\n{1}", new Object[] { inputName, toDimacs(inputGraph) });
			GraphLogger.logOnGraphMetrics(inputGraph.getGraph());
			LOGGER.log(Level.FINER, "Display graph:\n{0}", inputGraph);
		}
	}

	private static Optional<IBipartiteGraph> parse(Optional<String> inputFile, String inputName) {
		try {
			final PaceInputParser parser = new PaceInputParser();
			final IBipartiteGraph bigraph = inputFile.isPresent() ? parser.parse(inputFile.get())
					: parser.parse(new InputStreamReader(System.in));
			logOnInputGraph(inputName, bigraph);
			return Optional.of(bigraph);
		} catch (ImportException | FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, e, () -> "Parse file " + inputFile + FAIL);
		}
		return Optional.empty();
	}

	public static <E> void exportPlainDotGraph(final Graph<Integer, E> graph, final String filePath) {
		exportGraph(graph, JGraphtUtil.plainDotExporter(), filePath);
	}

	public static <V, E> void exportGraph(final Graph<V, E> graph, GraphExporter<V, E> exporter, final String path) {
		try {
			exporter.exportGraph(graph, new File(path));
			LOGGER.log(Level.INFO, "Export graph {0} [OK]", path);
		} catch (ImportException e) {
			LOGGER.log(Level.WARNING, e, () -> "Export graph " + path + FAIL);
		}
	}

	private static void exportBlockCutGraph(IBipartiteGraph graph, final String graphfile) {
		exportGraph(graph.getBlockCutGraph(), JGraphtUtil.blockCutExporter(),
				ToStringUtil.getFilenameWithoutExtension(graphfile) + "-blockcut.dot");

	}

	private static OCSolution solve(final IBipartiteGraph bigraph, final LignesClairesConfig config) {
		try {
			final IOCSolver heuristics = new HeuristicSolver();
			final OCSolution initialSolution = config.contains(OCSearchFlag.HEURISTICS)
					? heuristics.solve(bigraph, config)
					: OCSolution.getUnknownInstance();

			final IOCSolver solver = buildSolver(config);
			OCSolution solution = solver.solve(bigraph, initialSolution, config);
			LOGGER.log(Level.INFO, "Solve OCM:\n{0}", solution);
			return solution;
		} catch (OCSolverException e) {
			LOGGER.log(Level.SEVERE, "Solve OCM [FAIL]", e);
		}
		return OCSolution.getErrorInstance();
	}

	private static void exportPaceOutput(final Optional<String> solfile, final OCSolution solution) {
		if (solution.getStatus() == Status.OPTIMUM) {
			if (solfile.isPresent()) {
				try (FileWriter fileWriter = new FileWriter(new File(solfile.get()), false)) {
					fileWriter.append(solution.toPaceOutputString());
					LOGGER.log(Level.INFO, "Export solution to file {0} [OK]", solfile);
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, e, () -> "Export solution to file " + solfile + FAIL);
				}
			} else if (LignesClaires.LOGGER.getLevel() == Level.WARNING) {
				System.out.println(solution.toPaceOutputString());
			}
		}
	}

	public static String toDimacs(final IBipartiteGraphDimension dim) {
		return String.format("c FIXED %d%nc FREE %d%nc EDGES %d", dim.getFixedCount(), dim.getFreeCount(),
				dim.getEdgeCount());
	}

}
