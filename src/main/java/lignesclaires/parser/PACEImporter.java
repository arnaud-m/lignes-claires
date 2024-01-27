/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.parser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jgrapht.Graph;
import org.jgrapht.alg.util.Triple;
import org.jgrapht.nio.BaseEventDrivenImporter;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.GraphImporter;
import org.jgrapht.nio.ImportException;
import org.jgrapht.nio.dimacs.DIMACSImporter;

/**
 * Imports a graph specified in PACE 2024 format.
 * 
 * @see DIMACSImporter
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 */
public class PACEImporter<V, E> extends BaseEventDrivenImporter<V, E> implements GraphImporter<V, E> {
	/**
	 * Default key used for vertex ID.
	 */
	public static final String DEFAULT_VERTEX_ID_KEY = "ID";

	private Function<Integer, V> vertexFactory;
	private final double defaultWeight;

	private int fixedCount;

	private int freeCount;

	/**
	 * Construct a new DIMACSImporter
	 * 
	 * @param defaultWeight default edge weight
	 */
	public PACEImporter(double defaultWeight) {
		super();
		this.defaultWeight = defaultWeight;
	}

	/**
	 * Construct a new DIMACSImporter
	 */
	public PACEImporter() {
		this(Graph.DEFAULT_EDGE_WEIGHT);
	}

	public final int getFixedCount() {
		return fixedCount;
	}

	public final void setFixedCount(int fixedCount) {
		this.fixedCount = fixedCount;
	}

	public final int getFreeCount() {
		return freeCount;
	}

	public final void setFreeCount(int freeCount) {
		this.freeCount = freeCount;
	}

	/**
	 * Get the user custom vertex factory. This is null by default and the graph
	 * supplier is used instead.
	 * 
	 * @return the user custom vertex factory
	 */
	public Function<Integer, V> getVertexFactory() {
		return vertexFactory;
	}

	/**
	 * Set the user custom vertex factory. The default behavior is being null in
	 * which case the graph vertex supplier is used.
	 * 
	 * If supplied the vertex factory is called every time a new vertex is
	 * encountered in the file. The method is called with parameter the vertex
	 * identifier from the file and should return the actual graph vertex to add to
	 * the graph.
	 * 
	 * @param vertexFactory a vertex factory
	 */
	public void setVertexFactory(Function<Integer, V> vertexFactory) {
		this.vertexFactory = vertexFactory;
	}

	/**
	 * Import a graph.
	 * 
	 * <p>
	 * The provided graph must be able to support the features of the graph that is
	 * read. For example if the file contains self-loops then the graph provided
	 * must also support self-loops. The same for multiple edges.
	 * 
	 * <p>
	 * If the provided graph is a weighted graph, the importer also reads edge
	 * weights. Otherwise edge weights are ignored.
	 * 
	 * @param graph the output graph
	 * @param input the input reader
	 * @throws ImportException in case an error occurs, such as I/O or parse error
	 */
	@Override
	public void importGraph(Graph<V, E> graph, Reader input) throws ImportException {
		PACEEventDrivenImporter genericImporter = new PACEEventDrivenImporter().renumberVertices(false)
				.zeroBasedNumbering(false);
		Consumers consumers = new Consumers(graph);
		genericImporter.addPartitionCountConsumer(consumers.partitionCountConsumer);
		genericImporter.addVertexCountConsumer(consumers.nodeCountConsumer);
		genericImporter.addEdgeConsumer(consumers.edgeConsumer);
		genericImporter.importInput(input);
	}

	private class Consumers {
		private Graph<V, E> graph;
		private List<V> list;

		public Consumers(Graph<V, E> graph) {
			this.graph = graph;
			this.list = new ArrayList<>();
		}

		public final BiConsumer<Integer, Integer> partitionCountConsumer = (fixed, free) -> {
			fixedCount = fixed;
			freeCount = free;
		};

		public final Consumer<Integer> nodeCountConsumer = n -> {
			for (int i = 1; i <= n; i++) {
				V v;
				if (vertexFactory != null) {
					v = vertexFactory.apply(i);
					graph.addVertex(v);
				} else {
					v = graph.addVertex();
				}

				list.add(v);

				/*
				 * Notify the first time we create the node.
				 */
				notifyVertex(v);
				notifyVertexAttribute(v, DEFAULT_VERTEX_ID_KEY, DefaultAttribute.createAttribute(i));
			}
		};

		public final Consumer<Triple<Integer, Integer, Double>> edgeConsumer = t -> {
			int source = t.getFirst();
			V from = getElement(list, source - 1);
			if (from == null) {
				throw new ImportException("Node " + source + " does not exist");
			}

			int target = t.getSecond();
			V to = getElement(list, target - 1);
			if (to == null) {
				throw new ImportException("Node " + target + " does not exist");
			}

			E e = graph.addEdge(from, to);
			if (graph.getType().isWeighted()) {
				double weight = t.getThird() == null ? defaultWeight : t.getThird();
				graph.setEdgeWeight(e, weight);
			}

			notifyEdge(e);
		};

	}

	private static <E> E getElement(List<E> list, int index) {
		return index < list.size() ? list.get(index) : null;
	}
}
