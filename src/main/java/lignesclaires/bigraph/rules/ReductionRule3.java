/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.bigraph.rules;

import java.awt.Point;
import java.util.Optional;

import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IReductionRule;

public class ReductionRule3 extends BiGraphWithCounts implements IReductionRule {

	public ReductionRule3(IBipartiteGraph graph) {
		super(graph);
	}

	@Override
	public Optional<Point> apply(int i, int j) {
		if (graph.getFreeNeighborsCount(i) == 2 && graph.getFreeNeighborsCount(j) == 2) {
			if (counts.getCrossingCount(i, j) == 1 && counts.getCrossingCount(j, i) == 2) {
				return Optional.of(new Point(i, j));
			} else if (counts.getCrossingCount(i, j) == 2 && counts.getCrossingCount(j, i) == 1) {
				return Optional.of(new Point(j, i));
			}
		}
		return Optional.empty();
	}
}