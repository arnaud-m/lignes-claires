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

public class ReductionRule1 extends BiGraphWithCounts implements IReductionRule {

	public ReductionRule1(IBipartiteGraph graph) {
		super(graph);
	}

	@Override
	public Optional<Point> apply(int i, int j) {
		if (counts.getCrossingCount(i, j) > 0) {
			if (counts.getCrossingCount(j, i) == 0)
				return Optional.of(new Point(j, i));
		} else if (counts.getCrossingCount(j, i) > 0) {
			return Optional.of(new Point(i, j));
		}
		return Optional.empty();
	}
}