/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.specs;

import gnu.trove.list.TIntList;
import lignesclaires.bigraph.CrossingCounts;

public interface IBipartiteGraph extends IBipartiteGraphDimension, IGenericGraph {

	CrossingCounts getCrossingCounts();

	CrossingCounts getReducedCrossingCounts();

	TIntList getFreeNeighbors(int free);

	int getFreeNode(int free);

	int getDegree(int free);

}