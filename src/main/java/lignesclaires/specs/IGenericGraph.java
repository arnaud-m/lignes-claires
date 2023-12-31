/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.specs;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;

public interface IGenericGraph extends IGraphDimension {

	boolean isIsolated(int node);

	boolean isLeaf(int node);

	TIntList getNeighbors(int node);

	int getOutDegree(int node);

	TIntIterator getNeighborIterator(int node);

	void forEachEdge(IEdgeConsumer consumer);

}