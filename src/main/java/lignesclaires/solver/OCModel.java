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
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.DomOverWDeg;
import org.chocosolver.solver.variables.IntVar;

import lignesclaires.bigraph.BipartiteGraph;
import lignesclaires.bigraph.CrossingCounts;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IOCModel;

public class OCModel implements IOCModel {

	private final IBipartiteGraph bigraph;

	private final CrossingCounts counts;

	private final Model model;

	private final IntVar[] positions;

	private final IntVar[] permutation;

	private final IntVar objective;

	public OCModel(IBipartiteGraph bigraph) {
		super();
		this.bigraph = bigraph;
		counts = new CrossingCounts(bigraph.getCrossingCounts());
		model = new Model("OCM");
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
	}

	public void configureSearch(OCSearch search) {
		BipartiteGraph gr = (BipartiteGraph) bigraph;
		switch (search) {
		case MEDIAN: {
			IntValueSelector valueSelector = new CenteredValueSelector(positions, gr.getFreeMedians());
			getSolver().setSearch(Search.intVarSearch(new DomOverWDeg<>(positions, 0), valueSelector, positions));
			break;
		}
		case PMEDIAN: {
			getSolver().setSearch(Search.inputOrderLBSearch(gr.permutateMedians(positions)));
			break;
		}
		case BARYCENTER: {
			getSolver().setSearch(Search.inputOrderLBSearch(gr.permutateBarycenters(positions)));
			break;
		}
		case DEFAULT:
			break;
		default:
			break;
		}
	}

	@Override
	public final Model getModel() {
		return model;
	}

	public final Solution createSolution() {
		return new Solution(model, permutation);
	}

	public final OCSolution recordSolution(final Solution s) {
		int[] values = new int[permutation.length];
		for (int i = 0; i < permutation.length; i++) {
			values[i] = s.getIntVal(permutation[i]);
		}
		return new OCSolution(bigraph, values);
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