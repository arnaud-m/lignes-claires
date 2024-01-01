/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.bigraph.rules;

import java.awt.Point;
import java.util.Optional;

import lignesclaires.bigraph.AdjListUtil;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IReductionRule;

public class ReductionRule2 extends BiGraphWithCounts implements IReductionRule {

	public ReductionRule2(IBipartiteGraph graph) {
		super(graph);
	}

	@Override
	public Optional<Point> apply(int i, int j) {
		return AdjListUtil.isEqual(graph.getFreeNeighbors(i), graph.getFreeNeighbors(j))
				? Optional.of(new Point(i, j))
				: Optional.empty();
	}
}