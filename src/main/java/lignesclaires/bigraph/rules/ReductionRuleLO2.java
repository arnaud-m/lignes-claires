/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.bigraph.rules;

import org.chocosolver.solver.constraints.extension.Tuples;

import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IReductionConsumer;

public class ReductionRuleLO2 extends BiGraphWithCounts implements IReductionConsumer {

	private final Tuples tuples;

	public ReductionRuleLO2(IBipartiteGraph graph) {
		super(graph);
		this.tuples = new Tuples();
	}

	@Override
	public void accept(int i, int j) {
		if (counts.getCrossingCount(i, j) <= counts.getCrossingCount(j, i)) {
			tuples.add(i, j);
		} else {
			tuples.add(j, i);
		}
	}

	public final Tuples getTuples() {
		return tuples;
	}

}