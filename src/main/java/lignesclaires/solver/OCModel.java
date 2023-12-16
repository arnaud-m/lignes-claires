/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.solver;

import java.util.Arrays;
import java.util.Optional;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.DomOverWDeg;
import org.chocosolver.solver.variables.IntVar;

import lignesclaires.bigraph.BipartiteGraph;
import lignesclaires.choco.PropBinaryDisjunction;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IOCModel;

public class OCModel implements IOCModel {

	private final IBipartiteGraph bigraph;

	private final Model model;

	private final IntVar[] positions;

	private final IntVar[] permutation;

	private final IntVar objective;

	public OCModel(IBipartiteGraph bigraph) {
		super();
		this.bigraph = bigraph;
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

	public void postOrderedAdjacentNodes() {
		Tuples tuples = bigraph.getCrossingCounts().getOrderedAdjacentNodes();
		final int n = bigraph.getFreeCount() - 1;
		for (int i = 0; i < n; i++) {
			model.table(permutation[i], permutation[i + 1], tuples).post();
		}
	}

	static boolean useCustomConstraint = false;

	public Optional<IntVar> createCostVariable(int i, int j) {
		final int cij = bigraph.getCrossingCounts().getCrossingCount(i, j);
		final int cji = bigraph.getCrossingCounts().getCrossingCount(j, i);
		assert (cij == 0 || cji == 0);
		if (cij == cji) {
			return Optional.empty();
		} else {
			final String name = "cost[" + i + "][" + j + "]";
			final IntVar cost = model.intVar(name, new int[] { cij, cji });
			if (useCustomConstraint) {
				final IntVar[] vars = cij == 0 ? new IntVar[] { positions[i], positions[j], cost }
						: new IntVar[] { positions[j], positions[i], cost };
				Constraint c = new Constraint("BinaryDisjunction", new PropBinaryDisjunction(vars));
				model.post(c);
			} else {
				positions[i].lt(positions[j]).iff(cost.eq(cij)).decompose().post();
			}
			return Optional.of(cost);
		}
	}

	public void postObjective() {
		final int n = bigraph.getFreeCount();
		IntVar[] costs = new IntVar[n * (n - 1) / 2 + 1];
		int k = 0;
		costs[k++] = model.intVar(bigraph.getCrossingCounts().getConstant());
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				Optional<IntVar> cost = createCostVariable(i, j);
				if (cost.isPresent()) {
					costs[k++] = cost.get();
				}
			}
		}
		model.sum(Arrays.copyOf(costs, k), "=", objective).post();
	}

	@Override
	public String toString() {
		return "OneSideModel [\n" + bigraph + "\n" + model + "]";
	}

}