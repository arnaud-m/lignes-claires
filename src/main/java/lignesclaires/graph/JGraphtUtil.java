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
import java.util.LinkedHashMap;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.csv.CSVExporter;
import org.jgrapht.nio.csv.CSVFormat;
import org.jgrapht.nio.dot.DOTExporter;

import lignesclaires.specs.IEdgeConsumer;

public final class JGraphtUtil {

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

	public static Graph<Integer, DefaultEdge> unweightedUndirected() {
		return GraphTypeBuilder.<Integer, DefaultEdge>undirected().allowingMultipleEdges(false).allowingSelfLoops(false)
				.edgeClass(DefaultEdge.class).weighted(false).buildGraph();

	}

	public static DirectedAcyclicGraph<Integer, DefaultEdge> directedAcyclic() {
		return new DirectedAcyclicGraph<>(DefaultEdge.class);
	}

	public static <V, E> DOTExporter<V, E> plainDotExporter() {
		final DOTExporter<V, E> exporter = new DOTExporter<>(Object::toString);
		exporter.setVertexAttributeProvider(v -> {
			Map<String, Attribute> map = new LinkedHashMap<>();
			map.put("shape", DefaultAttribute.createAttribute("plain"));
			return map;
		});
		return exporter;
	}

	public static final <E extends DefaultEdge> void forEachEdge(Graph<Integer, E> graph, IEdgeConsumer consumer) {
		graph.edgeSet().forEach(e -> consumer.accept(graph.getEdgeSource(e), graph.getEdgeTarget(e)));
	}

}
