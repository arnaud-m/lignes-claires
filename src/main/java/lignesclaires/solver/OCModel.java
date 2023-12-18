/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.solver;

import java.awt.Point;
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
import lignesclaires.bigraph.CrossingCounts;
import lignesclaires.bigraph.rules.ReductionRules;
import lignesclaires.bigraph.rules.ReductionRules.Builder;
import lignesclaires.choco.PropBinaryDisjunction;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IOCModel;

public class OCModel implements IOCModel {

	private final IBipartiteGraph bigraph;

	private final Model model;

	private final IntVar[] positions;

	private final IntVar[] permutation;

	private final IntVar objective;

	private final int modelMask;

	private static final int RR1 = 1;
	private static final int RR2 = 2;
	private static final int RR3 = 4;
	private static final int RRLO2 = 8;
	private static final int DISJ = 16;
	private static final int DEBUG = 32;

	public OCModel(IBipartiteGraph bigraph, int modelMask) {
		super();
		this.bigraph = bigraph;
		this.modelMask = modelMask;
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

	private interface CostConstraintBuilder {

		Constraint buildCostConstraint(IntVar pi, IntVar pj, IntVar c);

	}

	private final class ObjectiveBuilder {

		private final CrossingCounts counts;

		private final IntVar[] costs;
		private int index;

		private int constant;

		private final CostConstraintBuilder builder;

		public ObjectiveBuilder(boolean decompose) {
			super();
			this.counts = bigraph.getReducedCrossingCounts();
			final int n = bigraph.getFreeCount();
			costs = new IntVar[n * (n - 1) / 2 + 1];
			this.index = 1;
			this.constant = counts.getConstant();
			if (decompose) {
				builder = (pi, pj, c) -> pi.lt(pj).iff(c.eq(0)).decompose();
			} else {
				builder = (pi, pj, c) -> new Constraint("BinaryDisjunction",
						new PropBinaryDisjunction(new IntVar[] { pi, pj, c }));
			}
		}

		public final void addOrdered(Point p) {
			addOrdered(p.x, p.y);
		}

		public void addOrdered(int i, int j) {
			constant += counts.getCrossingCount(i, j);
			positions[i].lt(positions[j]).post();
		}

		private IntVar createCostVar(int i, int j, int cij, int cji) {
			return model.intVar("cost[" + i + "][" + j + "]", new int[] { cij, cji });
		}

		public void addUnordered(int i, int j) {
			final int cij = counts.getCrossingCount(i, j);
			final int cji = counts.getCrossingCount(j, i);
			if (cij != cji) {
				costs[index] = createCostVar(i, j, cij, cji);
				Constraint c = cij == 0 ? builder.buildCostConstraint(positions[i], positions[j], costs[index])
						: builder.buildCostConstraint(positions[j], positions[i], costs[index]);
				model.post(c);
				index++;
			}
		}

		public void postObjective() {
			costs[0] = model.intVar(constant);
			model.sum(Arrays.copyOf(costs, index), "=", objective).post();

		}
	}

	private boolean hasFlag(final int flag) {
		return (modelMask & flag) != 0;
	}

	private ReductionRules buildReductionRules() {
		Builder builder = new ReductionRules.Builder(bigraph);
		if (hasFlag(RR1))
			builder.withReductionRule1();
		if (hasFlag(RR2))
			builder.withReductionRule2();
		if (hasFlag(RR3))
			builder.withReductionRule3();
		if (hasFlag(RRLO2))
			builder.withReductionRuleLO2();
		return builder.build();
	}

	@Override
	public void buildModel() {
		final int n = bigraph.getFreeCount();
//		
		if (hasFlag(DEBUG)) {
			postOrderedAdjacentNodes();
			postObjective();
		} else {
			ReductionRules rules = buildReductionRules();
			ObjectiveBuilder objBuilder = new ObjectiveBuilder(hasFlag(DISJ));
			for (int i = 0; i < n; i++) {
				for (int j = i + 1; j < n; j++) {
					final Optional<Point> order = rules.apply(i, j);
					if (order.isPresent()) {
						objBuilder.addOrdered(order.get());
					} else {
						objBuilder.addUnordered(i, j);
					}
				}
			}
			rules.getTuplesLO2().ifPresent(this::postPermutationBinaryTable);
			objBuilder.postObjective();

		}
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

	public void postPermutationBinaryTable(Tuples tuples) {
		final int n = bigraph.getFreeCount() - 1;
		for (int i = 0; i < n; i++) {
			model.table(permutation[i], permutation[i + 1], tuples).post();
		}
	}

	public void postOrderedAdjacentNodes() {
		Tuples tuples = bigraph.getReducedCrossingCounts().getTableReducedRuleLO2();
		final int n = bigraph.getFreeCount() - 1;
		for (int i = 0; i < n; i++) {
			model.table(permutation[i], permutation[i + 1], tuples).post();
		}
	}

	public Optional<IntVar> createCostVariable(int i, int j) {
		final int cij = bigraph.getReducedCrossingCounts().getCrossingCount(i, j);
		final int cji = bigraph.getReducedCrossingCounts().getCrossingCount(j, i);
		if (cij == cji) {
			return Optional.empty();
		} else {
			final String name = "cost[" + i + "][" + j + "]";
			final IntVar cost = model.intVar(name, new int[] { cij, cji });
			if (hasFlag(DISJ)) {
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
		costs[k++] = model.intVar(bigraph.getReducedCrossingCounts().getConstant());
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