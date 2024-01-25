/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.choco;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import lignesclaires.specs.IBipartiteGraph;

public class PropAssignmentLowerBound extends Propagator<IntVar> {

	private final IntVar[] positions;
	private final IntVar cost;

	private final AssignmentBuilder builder;

	public PropAssignmentLowerBound(IBipartiteGraph bigraph, IntVar[] positions, IntVar cost) {
		super(ArrayUtils.concat(positions, cost), PropagatorPriority.VERY_SLOW, false);
		this.positions = positions;
		this.cost = cost;
		// System.out.println(bigraph.getReducedCrossingCounts());
		builder = new AssignmentBuilder(bigraph);
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return IntEventType.boundAndInst();
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		builder.buildAssignmentGraph(positions);
//		Graph<Integer, DefaultWeightedEdge> graph = GraphTypeBuilder.<Integer, DefaultWeightedEdge>undirected()
//				.allowingMultipleEdges(false).allowingSelfLoops(false).edgeClass(DefaultWeightedEdge.class)
//				.weighted(true).buildGraph();
		// int[][] costs = builder.getCostMatrix();
//		final int n = costs.length;
//		Set<Integer> s1 = new HashSet<>();
//		Set<Integer> s2 = new HashSet<>();
//		for (int i = 0; i < n; i++) {
//			graph.addVertex(i);
//			s1.add(i);
//			graph.addVertex(n + i);
//			s2.add(n + i);
//		}
//		for (int i = 0; i < n; i++) {
//			for (int j = 0; j < n; j++) {
//				DefaultWeightedEdge e = new DefaultWeightedEdge();
//				graph.addEdge(i, n + j, e);
//
//				graph.setEdgeWeight(e, costs[i][j]);
//			}
//		}
		// System.out.println(graph);
//		KuhnMunkresMinimalWeightBipartitePerfectMatching<Integer, DefaultWeightedEdge> hungarian = new KuhnMunkresMinimalWeightBipartitePerfectMatching<Integer, DefaultWeightedEdge>(
//				graph, s1, s2);

		System.err.println(builder.solveAssignment().getWeight());
		// System.out.println(Arrays.toString(positions));
		// System.out.println(builder);
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

}
