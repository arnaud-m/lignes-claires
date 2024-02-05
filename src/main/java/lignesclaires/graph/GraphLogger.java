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
			LOGGER.log(Level.CONFIG, "Degree distribution:\nc DEGREE_DIST {0}", freq);
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
			LOGGER.log(Level.CONFIG, "Connected components:\nc CONNECTED {0}\nc RADIUS  {1}\nc DIAMETER{2}",
					new Object[] { inspector.connectedSets().size(), radius, diameter });
		}
	}

	public static void logOnCrossingCounts(IBipartiteGraph graph) {
		if (LOGGER.isLoggable(Level.INFO)) {
			final CrossingCounts rcounts = graph.getReducedCrossingCounts();
			LOGGER.log(Level.INFO, "Reduced crossing counts:\nc RCCOUNTS_LB {0,number,#}", rcounts.getConstant());
			if (LOGGER.isLoggable(Level.CONFIG)) {
				LOGGER.log(Level.CONFIG, "Reduced crossing count patterns:\nc RCCOUNTS_PATTERNS {0}",
						rcounts.getPatterns());
				LOGGER.log(Level.FINER, "Reduced crossing count matrix:\n{0}", rcounts);
				final CrossingCounts counts = graph.getCrossingCounts();
				LOGGER.log(Level.CONFIG, "Crossing count distribution:\nc CCOUNTS_DIST {0}", counts.getDistribution());
				LOGGER.log(Level.FINE, "Crossing count patterns:\n{0}", counts.getPatterns());
				LOGGER.log(Level.FINER, "Crossing count matrix:\n{0}", counts);

			}

		}
	}

	public static <V, E> void logOnBlockCutGraph(BlockCutpointGraph<V, E> graph) {
		if (LOGGER.isLoggable(Level.CONFIG)) {
			LOGGER.log(Level.CONFIG, "Block-Cut graph:\nc BLOCKS {0}\nc CUTPOINTS {1}",
					new Object[] { graph.getBlocks().size(), graph.getCutpoints().size() });

		}
	}
}
