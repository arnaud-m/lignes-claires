/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.graph;

import java.util.Optional;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.BlockCutpointGraph;

import lignesclaires.specs.IGraph;

public class DefaultGraph<V, E> implements IGraph<V, E> {

	protected final Graph<V, E> graph;

	private Optional<BlockCutpointGraph<V, E>> blockCutGraph;

	protected DefaultGraph(final Graph<V, E> graph) {
		this.graph = graph;
		blockCutGraph = Optional.empty();
	}

	public final Graph<V, E> getGraph() {
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

	public final BlockCutpointGraph<V, E> getBlockCutGraph() {
		if (blockCutGraph.isEmpty()) {
			blockCutGraph = Optional.of(new BlockCutpointGraph<>(graph));
			GraphLogger.logOnBlockCutGraph(blockCutGraph.get());
		}
		return blockCutGraph.get();
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