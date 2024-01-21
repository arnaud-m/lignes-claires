/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.graph;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import lignesclaires.specs.IEdgeConsumer;

public class DGraph extends AbstractGraph {

	public DGraph(final int nodeCount, final int edgeCount) {
		super(TListUtil.createArrayOfTLists(nodeCount, 3 * edgeCount / nodeCount));
	}

	@Override
	public boolean isDirected() {
		return true;
	}

	@Override
	public void addEdge(final int i, final int j) {
		adjLists[i].add(j);
		edgeCount++;
	}

	@Override
	public int getInDegree(int node) {
		int indeg = 0;
		for (TIntArrayList adjList : adjLists) {
			if (adjList.binarySearch(node) >= 0) {
				indeg++;
			}
		}
		return indeg;
	}

	@Override
	public final boolean isIsolated(final int node) {
		return adjLists[node].isEmpty() && getInDegree(node) == 0;
	}

	@Override
	public final boolean isLeaf(final int node) {
		final int outdeg = getInDegree(node);
		if (outdeg <= 1) {
			final int indeg = getInDegree(node);
			return outdeg + indeg == 1;
		}
		return false;
	}

	@Override
	public final void forEachEdge(final IEdgeConsumer consumer) {
		final int n = getNodeCount();
		for (int i = 0; i < n; i++) {
			final TIntIterator iter = getNeighborIterator(i);
			while (iter.hasNext()) {
				consumer.accept(i, iter.next());
			}
		}
	}

}