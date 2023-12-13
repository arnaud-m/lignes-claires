/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.solver;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;

import lignesclaires.bigraph.CrossingCounts;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IOCModel;

public class OneSideModel implements IOCModel {

	private final IBipartiteGraph bigraph;

	private final CrossingCounts counts;

	private final Model model;

	private final IntVar[] positions;

	private final IntVar[] permutation;

	private final IntVar objective;

	public OneSideModel(IBipartiteGraph bigraph) {
		super();
		this.bigraph = bigraph;
		counts = new CrossingCounts(bigraph.getCrossingCounts());
		model = new Model("OSCM");
		final int n = bigraph.getFreeCount();
		final int m = bigraph.getEdgeCount();

		this.positions = model.intVarArray("pos", n, 0, n - 1, false);
		this.permutation = model.intVarArray("seq", n, 0, n - 1, false);
		model.inverseChanneling(positions, permutation).post();
		objective = model.intVar("objective", 0, m * m);
		model.setObjective(false, objective);

	}

	@Override
	public IBipartiteGraph getGraph() {
		return bigraph;
	}

	@Override
	public IntVar[] getPositionVars() {
		return positions;
	}

	@Override
	public IntVar[] getPermutationVars() {
		return permutation;
	}

	@Override
	public IntVar getMinCrossingCounts() {
		return objective;
	}

	@Override
	public void buildModel() {
		postOrderedAdjacentNodes();
		postObjective();
		getSolver().showDecisions();
	}

	@Override
	public final Model getModel() {
		return model;
	}

	public final CrossingCounts getCounts() {
		return counts;
	}

	public void postOrderedAdjacentNodes() {
		Tuples tuples = counts.getOrderedAdjacentNodes();
		final int n = bigraph.getFreeCount() - 1;
		for (int i = 0; i < n; i++) {
			model.table(permutation[i], permutation[i + 1], tuples).post();
		}
	}

	public IntVar createCostVariable(int i, int j) {
		final int cij = counts.getCrossingCount(i, j);
		final int cji = counts.getCrossingCount(j, i);
		final String name = "cost[" + i + "][" + j + "]";
		if (cij == cji) {
			return model.intVar(name, cij);
		} else {
			final IntVar cost = model.intVar(name, new int[] { cij, cji });
			positions[i].lt(positions[j]).iff(cost.eq(cij)).decompose().post();
			return cost;
		}
	}

	public void postObjective() {
		final int n = bigraph.getFreeCount();
		IntVar[] costs = new IntVar[n * (n - 1) / 2 + 1];
		int k = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				costs[k++] = createCostVariable(i, j);
			}
		}
		costs[k] = model.intVar(counts.getConstant());
		model.sum(costs, "=", objective).post();
	}

	@Override
	public String toString() {
		return "OneSideModel [\n" + bigraph + "\n" + counts + "\n" + model + "]";
	}

}