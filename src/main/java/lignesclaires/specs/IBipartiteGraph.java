/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.specs;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import gnu.trove.list.TIntList;
import lignesclaires.graph.CrossingCounts;

public interface IBipartiteGraph extends IBipartiteGraphDimension, IGraph {

	Graph<Integer, DefaultEdge> getGraph();

	CrossingCounts getCrossingCounts();

	CrossingCounts getReducedCrossingCounts();

	TIntList getFreeNeighbors(int free);

	int getFreeNode(int free);

	int getFreeDegree(int free);

}