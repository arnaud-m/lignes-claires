/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
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
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;

import lignesclaires.LignesClaires;
import lignesclaires.bigraph.BipartiteGraph;
import lignesclaires.bigraph.CrossingCounts;
import lignesclaires.bigraph.rules.ReductionRules;
import lignesclaires.bigraph.rules.ReductionRules.Builder;
import lignesclaires.bigraph.rules.ReductionRulesBis;
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

	public static final int RR1 = 1;
	public static final int RR2 = 2;
	public static final int RR3 = 4;
	public static final int RRLO2 = 8;
	public static final int DISJ = 16;
	public static final int LB = 32;

	public OCModel(final IBipartiteGraph bigraph, final int modelMask) {
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

		public ObjectiveBuilder(final boolean decompose) {
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

		public final void addOrdered(final Point p) {
			addOrdered(p.x, p.y);
		}

		public void addOrdered(final int i, final int j) {
			constant += counts.getCrossingCount(i, j);
			positions[i].lt(positions[j]).post();
		}

		private IntVar createCostVar(final int i, final int j, final int cij, final int cji) {
			return model.intVar("cost[" + i + "][" + j + "]", new int[] { cij, cji });
		}

		public void addIncomparable(final int i, final int j) {
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
		if (hasFlag(RR1)) {
			builder.withReductionRule1();
		}
		if (hasFlag(RR2)) {
			builder.withReductionRule2();
		}
		if (hasFlag(RR3)) {
			builder.withReductionRule3();
		}
		if (hasFlag(RRLO2)) {
			builder.withReductionRuleLO2();
		}
		return builder.build();
	}

	private void postLowerBound() {
		final int lb = bigraph.getEdgeCount() - bigraph.getNodeCount() + 1;
		objective.ge(lb).decompose().post();
	}

	static boolean DEBUG = false;

	@Override
	public void buildModel() {
		final int n = bigraph.getFreeCount();
		ReductionRules rules = buildReductionRules();
		ObjectiveBuilder objBuilder = new ObjectiveBuilder(hasFlag(DISJ));
		ReductionRulesBis rulesBis = new ReductionRulesBis(bigraph, hasFlag(RR1), hasFlag(RR2), hasFlag(RR3));
		LignesClaires.writeString(rulesBis.getOrderedGraph().toDotty(), "digraph.dot");
		LignesClaires.writeString(rulesBis.getReducedGraph().toDotty(), "trgraph.dot");
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				final Optional<Point> order = rules.apply(i, j);
				if (order.isPresent()) {
					if (DEBUG)
						objBuilder.addOrdered(order.get());
				} else {
					if (DEBUG)
						objBuilder.addIncomparable(i, j);
				}
			}
		}

		if (!DEBUG) {
			rulesBis.getOrderedGraph().forEachEdge(objBuilder::addOrdered);
			rulesBis.getIncomparableGraph().forEachEdge(objBuilder::addIncomparable);
			if (hasFlag(RRLO2)) {
				postPermutationBinaryTable(bigraph.getCrossingCounts().getTuplesLO2());
			}
		} else {
			rules.getTuplesLO2().ifPresent(this::postPermutationBinaryTable);

		}

		objBuilder.postObjective();
		if (hasFlag(LB)) {
			postLowerBound();
		}
	}

	public void configureSearch(final OCSearch search) {
		BipartiteGraph gr = (BipartiteGraph) bigraph;
		switch (search) {
		case MEDIAN: {
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

	public void configureRestarts() {
		final int n = bigraph.getFreeCount();
		getSolver().setGeometricalRestart(n, 1.1, new FailCounter(model, 1), n);
		getSolver().setNoGoodRecordingFromRestarts();
	}

	@Override
	public final Model getModel() {
		return model;
	}

	public final Solution createSolution() {
		return new Solution(model, permutation);
	}

	public final int[] recordSolution(final Solution s) {
		final int[] values = new int[permutation.length];
		for (int i = 0; i < permutation.length; i++) {
			values[i] = bigraph.getFreeNode(s.getIntVal(permutation[i]));
		}
		return values;
	}

	public final String printSolution(final Solution s) {
		StringBuilder b = new StringBuilder();
		b.append('v');
		for (int i = 0; i < permutation.length; i++) {
			b.append(' ').append(s.getIntVal(permutation[i]));
		}
		return b.toString();
	}

	public void postPermutationBinaryTable(final Tuples tuples) {
		final int n = bigraph.getFreeCount() - 1;
		for (int i = 0; i < n; i++) {
			model.table(permutation[i], permutation[i + 1], tuples).post();
		}
	}

	@Override
	public String toString() {
		return "OneSideModel [\n" + bigraph + "\n" + model + "]";
	}

}