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
import lignesclaires.specs.IGraphDimension;

public class UGraph extends AbstractGraph implements IGraphDimension {

	public UGraph(final int nodeCount, final int edgeCount) {
		super(TListUtil.createArrayOfTLists(nodeCount, 3 * edgeCount / nodeCount));
	}

	@Override
	public boolean isDirected() {
		return false;
	}

	@Override
	public void addEdge(final int i, final int j) {
		adjLists[i].add(j);
		adjLists[j].add(i);
		edgeCount++;
	}

	@Override
	public final void forEachEdge(final IEdgeConsumer consumer) {
		final int n = getNodeCount();
		for (int i = 0; i < n; i++) {
			final TIntIterator iter = getNeighborIterator(i);
			while (iter.hasNext()) {
				int j = iter.next();
				if (j <= i) {
					consumer.accept(i, j);
				} else {
					break;
				}
			}
		}
	}

}