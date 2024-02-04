/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.graph;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.BlockCutpointGraph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.shortestpath.GraphMeasurer;
import org.jgrapht.graph.AsSubgraph;

import lignesclaires.LignesClaires;
import lignesclaires.specs.IBipartiteGraph;

public final class GraphLogger {

	private static final Logger LOGGER = LignesClaires.LOGGER;

	private GraphLogger() {
		super();
	}

	public static <V, E> void logOnDegreeDistribution(Graph<V, E> graph) {
		if (LOGGER.isLoggable(Level.CONFIG)) {
			final IntFrequency freq = new IntFrequency();
			graph.vertexSet().forEach(v -> freq.add(graph.degreeOf(v)));
			LOGGER.log(Level.CONFIG, "Degree Distribution:\nc DEGREE_DIST {0}", freq);
		}
	}

	private static <V, E> void addMeasures(Graph<V, E> graph, StringBuilder radius, StringBuilder diameter) {
		GraphMeasurer<V, E> measurer = new GraphMeasurer<>(graph);
		radius.append(String.format(" %2.0f", measurer.getRadius()));
		diameter.append(String.format(" %2.0f", measurer.getDiameter()));
	}

	public static <V, E> void logOnConnectedComponents(Graph<V, E> graph) {
		if (LOGGER.isLoggable(Level.CONFIG)) {
			ConnectivityInspector<V, E> inspector = new ConnectivityInspector<>(graph);

			StringBuilder radius = new StringBuilder();
			StringBuilder diameter = new StringBuilder();

			if (inspector.isConnected()) {
				addMeasures(graph, radius, diameter);
			} else {
				for (Set<V> cc : inspector.connectedSets()) {
					addMeasures(new AsSubgraph<V, E>(graph, cc), radius, diameter);
				}
			}
			LOGGER.log(Level.CONFIG, "Connected Components:\nc CONNECTED {0}\nc RADIUS  {1}\nc DIAMETER{2}",
					new Object[] { inspector.connectedSets().size(), radius, diameter });
		}
	}

	public static void logOnCrossingCounts(CrossingCounts counts, String title, String prefix) {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.log(Level.INFO,
					() -> String.format("%s Lower Bound:%nc %s_LB %s", title, prefix, counts.getConstant()));
			LOGGER.log(Level.CONFIG,
					() -> String.format("%s Distribution:%nc %s_DIST %s", title, prefix, counts.getDistribution()));
			LOGGER.log(Level.FINE, () -> String.format("%s Patterns:%n%s", title, counts.getPatterns()));
			LOGGER.log(Level.FINER, () -> String.format("%s Matrix:%n%s", title, counts));

		}
	}

	public static void logOnCrossingCounts(IBipartiteGraph graph) {
		if (LOGGER.isLoggable(Level.INFO)) {
			final CrossingCounts rcounts = graph.getReducedCrossingCounts();
			LOGGER.log(Level.INFO, "Reduced Crossing Counts:\nc RCCOUNTS_LB {0}", rcounts.getConstant());
			if (LOGGER.isLoggable(Level.CONFIG)) {
				LOGGER.log(Level.CONFIG, "Reduced Crossing Count Patterns:\nc RCCOUNTS_PATTERNS {0}",
						rcounts.getPatterns());
				LOGGER.log(Level.FINER, "Reduced Crossing Count Matrix:\n{0}", rcounts);
				final CrossingCounts counts = graph.getCrossingCounts();
				LOGGER.log(Level.CONFIG, "Crossing Count Distribution:\nc CCOUNTS_DIST {0}", counts.getDistribution());
				LOGGER.log(Level.FINE, "Crossing Count Patterns:\n{0}", counts.getPatterns());
				LOGGER.log(Level.FINER, "Crossing Count Matrix:\n{0}", counts);

			}

		}
		// logOnCrossingCounts(graph.getReducedCrossingCounts(), "Reduced Crossing
		// Count", "RCCOUNTS");
		// logOnCrossingCounts(graph.getCrossingCounts(), "Crossing Count", "CCOUNTS");

	}

	public static <V, E> void logOnBlockCutGraph(BlockCutpointGraph<V, E> graph) {
		if (LOGGER.isLoggable(Level.CONFIG)) {
			LOGGER.log(Level.CONFIG, "Block-Cut Graph\nc BLOCKS {0}\nc CUTPOINTS {1}",
					new Object[] { graph.getBlocks().size(), graph.getCutpoints().size() });

		}
	}
}
