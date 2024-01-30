/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.graph;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import lignesclaires.specs.IGraph;

public abstract class AbstractGraph implements IGraph {

	protected final Graph<Integer, DefaultEdge> graph;

	protected AbstractGraph(final Graph<Integer, DefaultEdge> graph) {
		this.graph = graph;
	}

	public final Graph<Integer, DefaultEdge> getGraph() {
		return graph;
	}

	@Override
	public final int getNodeCount() {
		return graph.vertexSet().size();
	}

	@Override
	public final int getEdgeCount() {
		return graph.edgeSet().size();
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Graph [");
		final int n = getNodeCount();
		b.append("n:").append(n);
		b.append(", e:").append(getEdgeCount());
		b.append(",\n").append(JGraphtUtil.toString(graph));
		b.append("]");
		return b.toString();
	}

}