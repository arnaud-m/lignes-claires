/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.graph;

import java.io.StringWriter;

import org.jgrapht.Graph;
import org.jgrapht.nio.csv.CSVExporter;
import org.jgrapht.nio.csv.CSVFormat;

public class JGraphtUtil {

	private JGraphtUtil() {
	}

	public static <V, E> String toString(final Graph<V, E> graph) {
		CSVExporter<V, E> exporter = new CSVExporter<>(CSVFormat.ADJACENCY_LIST);
		StringWriter writer = new StringWriter();
		exporter.exportGraph(graph, writer);
		return writer.toString();

	}

	public static <E> void addVertices(Graph<Integer, E> graph, final int n) {
		for (int i = 0; i < n; i++) {
			graph.addVertex(i);
		}
	}
}
