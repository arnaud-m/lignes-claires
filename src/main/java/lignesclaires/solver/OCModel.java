/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.solver;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import lignesclaires.LignesClaires;
import lignesclaires.choco.PropAssignmentLowerBound;
import lignesclaires.choco.PropBinaryDisjunction;
import lignesclaires.graph.BGraph;
import lignesclaires.graph.CrossingCounts;
import lignesclaires.graph.GraphTriangles;
import lignesclaires.graph.JGraphtUtil;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IOCModel;

public class OCModel implements IOCModel {

	private final IBipartiteGraph bigraph;

	private final Model model;

	private final IntVar[] positions;

	private final IntVar[] permutation;

	private final IntVar objective;

	private final int modelMask;

	private Optional<String> rrPath;

	public static final int RR1 = 1;
	public static final int RR2 = 2;
	public static final int RR3 = 4;
	public static final int RRLO2 = 8;
	public static final int DISJ = 16;
	public static final int LB = 32;
	public static final int TRANS = 64;

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
		this.rrPath = Optional.empty();
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
	public IntVar getCrossingCountVar() {
		return objective;
	}

	public void setExportPath(String path) {
		rrPath = Optional.of(path);
	}

	private interface CostConstraintBuilder {

		Constraint buildCostConstraint(IntVar pi, IntVar pj, IntVar c);

	}

	static class DisjunctiveEdge extends DefaultWeightedEdge {

		private static final long serialVersionUID = -5854833531505777218L;

		private final transient IntVar cost;

		public DisjunctiveEdge(IntVar cost) {
			super();
			this.cost = cost;
		}

		public final IntVar getCost() {
			return cost;
		}

	}

	public static Graph<Integer, DisjunctiveEdge> disjunctiveGraph() {
		return GraphTypeBuilder.<Integer, DisjunctiveEdge>undirected().allowingMultipleEdges(false)
				.allowingSelfLoops(false).edgeClass(DisjunctiveEdge.class).weighted(true).buildGraph();
	}

	private final class ObjectiveBuilder {

		private final CrossingCounts counts;

		private final Graph<Integer, DisjunctiveEdge> disjGraph;

		private final IntVar[] costs;
		private int index;

		private int constant;

		private final CostConstraintBuilder builder;

		public ObjectiveBuilder(final boolean useBinaryDisjunction) {
			super();
			this.counts = bigraph.getReducedCrossingCounts();
			final int n = bigraph.getFreeCount();
			this.disjGraph = disjunctiveGraph();
			JGraphtUtil.addVertices(disjGraph, n);
			costs = new IntVar[n * (n - 1) / 2 + 1];
			this.index = 1;
			this.constant = counts.getConstant();
			if (useBinaryDisjunction) {
				builder = (pi, pj, c) -> new Constraint("BinaryDisjunction",
						new PropBinaryDisjunction(new IntVar[] { pi, pj, c }));
			} else {
				builder = (pi, pj, c) -> pi.lt(pj).iff(c.eq(0)).decompose();
			}
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
				DisjunctiveEdge e = new DisjunctiveEdge(costs[index]);
				disjGraph.addEdge(i, j, e);
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

	private void postLowerBound() {
		final int lb = bigraph.getEdgeCount() - bigraph.getNodeCount() + 1;
		objective.ge(lb).decompose().post();
	}

	private void postAssignmentLowerBound() {
		model.post(new Constraint("AssignmentLowerBound", new PropAssignmentLowerBound(bigraph, positions, objective)));
	}

	@Override
	public void buildModel() {
		final ObjectiveBuilder objBuilder = new ObjectiveBuilder(hasFlag(DISJ));
		final ReductionRules rules = new ReductionRules(bigraph, hasFlag(RR1), hasFlag(RR2), hasFlag(RR3));
		rrPath.ifPresent(rules::exportGraph);
		rules.forEachOrderedEdge(objBuilder::addOrdered);
		rules.forEachIncomparableEdge(objBuilder::addIncomparable);
		LignesClaires.LOGGER.log(Level.INFO, "Reduction rules:\nd ORDERED {0}\nd INCPOMPARABLE {1}", new Object[] {
				rules.getOrderedGraph().edgeSet().size(), rules.getIncomparableGraph().edgeSet().size() });

		if (hasFlag(TRANS)) {
			GraphTriangles.forEachTriangle(objBuilder.disjGraph, (i, j, k) -> {
				DisjunctiveEdge ij = objBuilder.disjGraph.getEdge(i, j);
				DisjunctiveEdge jk = objBuilder.disjGraph.getEdge(j, k);
				DisjunctiveEdge ki = objBuilder.disjGraph.getEdge(k, i);

				model.table(new IntVar[] { ij.getCost(), jk.getCost(), ki.getCost() },
						getGraph().getReducedCrossingCounts().getForbiddenCycles(i, j, k)).post();
			});
		}

		if (hasFlag(RRLO2)) {
			postPermutationBinaryTable(bigraph.getCrossingCounts().getTuplesLO2());
		}
		objBuilder.postObjective();
		if (hasFlag(LB)) {
			postLowerBound();
			postAssignmentLowerBound();
		}

	}

	public void configureSearch(final OCSearch search) {
		BGraph gr = (BGraph) bigraph;
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
		final StringBuilder b = new StringBuilder();
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
