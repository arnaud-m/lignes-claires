/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.bigraph;

import gnu.trove.iterator.TIntIterator;
import lignesclaires.specs.IEdgeConsumer;

public class DGraph extends AbstractGraph {

	public DGraph(int nodeCount, int edgeCount) {
		super(AdjListUtil.createArrayOfTLists(nodeCount, 3 * edgeCount / nodeCount));
	}

	@Override
	public boolean isDirected() {
		return true;
	}

	@Override
	public void addEdge(int i, int j) {
		adjLists[i].add(j);
		edgeCount++;
	}

	@Override
	public final void forEachEdge(IEdgeConsumer consumer) {
		final int n = getNodeCount();
		for (int i = 0; i < n; i++) {
			final TIntIterator iter = getNeighborIterator(i);
			while (iter.hasNext()) {
				consumer.accept(i, iter.next());
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Graph [");
		final int n = getNodeCount();
		b.append("n:").append(n);
		b.append(", e:").append(getEdgeCount());
		b.append("]\n");
		for (int i = 0; i < n; i++) {
			b.append(i).append(": ").append(adjLists[i]).append('\n');
		}
		b.deleteCharAt(b.length() - 1);
		return b.toString();
	}

}