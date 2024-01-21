/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.bigraph.rules;

import java.util.ArrayList;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import lignesclaires.bigraph.CrossingCounts;
import lignesclaires.bigraph.DGraph;
import lignesclaires.bigraph.TListUtil;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IGenericGraph;

public class ReductionRulesBis {

	private final IBipartiteGraph graph;
	private final CrossingCounts counts;
	private final DGraph ordered;
	private final DGraph incomparable;
	private final DGraph reduced;
	private final IReductionRule[] rules;

	public ReductionRulesBis(IBipartiteGraph graph, boolean useRule1, boolean useRule2, boolean useRule3) {
		super();
		this.graph = graph;
		this.counts = graph.getCrossingCounts();
		final int n = graph.getFreeCount();
		this.ordered = new DGraph(graph.getFreeCount(), n);
		this.incomparable = new DGraph(graph.getFreeCount(), n);
		this.reduced = new DGraph(graph.getFreeCount(), n);
		rules = buildRules(useRule1, useRule2, useRule3);
		buildOrderedGraph();
		buildReducedGraph();
	}

	public final IBipartiteGraph getBiGraph() {
		return graph;
	}

	public final IGenericGraph getOrderedGraph() {
		return ordered;
	}

	public final IGenericGraph getReducedGraph() {
		return reduced;
	}

	public final IGenericGraph getIncomparableGraph() {
		return incomparable;
	}

	private IReductionRule[] buildRules(boolean useRule1, boolean useRule2, boolean useRule3) {
		ArrayList<IReductionRule> r = new ArrayList<>();
		if (useRule1) {
			r.add(new ReductionRule1());
		}
		if (useRule2) {
			r.add(new ReductionRule2());
		}
		if (useRule3) {
			r.add(new ReductionRule3());
		}
		return r.toArray(new IReductionRule[r.size()]);
	}

	private void applyRules(int i, int j) {
		for (IReductionRule rule : rules) {
			if (rule.apply(i, j)) {
				return;
			}
		}
		incomparable.addEdge(i, j);
	}

	private void buildOrderedGraph() {
		final int n = graph.getFreeCount();
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				applyRules(i, j);
			}
		}
	}

	private void buildReducedGraph() {
		final int n = graph.getFreeCount();
		for (int i = 0; i < n; i++) {
			final TIntArrayList neighbors = new TIntArrayList(ordered.getNeighbors(i));
			TIntIterator it = ordered.getNeighborIterator(i);
			while (it.hasNext() && !neighbors.isEmpty()) {
				TListUtil.difference(neighbors, ordered.getNeighbors(it.next()));
			}
			it = neighbors.iterator();
			while (it.hasNext()) {
				reduced.addEdge(i, it.next());
			}
		}
	}

	interface IReductionRule {

		boolean apply(int i, int j);

	}

	public class ReductionRule1 implements IReductionRule {

		@Override
		public boolean apply(int i, int j) {
			if (counts.getCrossingCount(i, j) > 0) {
				if (counts.getCrossingCount(j, i) == 0)
					ordered.addEdge(j, i);
			} else if (counts.getCrossingCount(j, i) > 0) {
				ordered.addEdge(i, j);
			} else {
				return false;
			}
			return true;
		}
	}

	public class ReductionRule2 implements IReductionRule {

		@Override
		public boolean apply(int i, int j) {
			if (TListUtil.isEqual(graph.getFreeNeighbors(i), graph.getFreeNeighbors(j))) {
				ordered.addEdge(i, j);
				return true;
			}
			return false;
		}
	}

	public class ReductionRule3 implements IReductionRule {

		@Override
		public boolean apply(int i, int j) {
			if (graph.getDegree(i) == 2 && graph.getDegree(j) == 2) {
				if (counts.getCrossingCount(i, j) == 1 && counts.getCrossingCount(j, i) == 2) {
					ordered.addEdge(i, j);
					return true;
				} else if (counts.getCrossingCount(i, j) == 2 && counts.getCrossingCount(j, i) == 1) {
					ordered.addEdge(j, i);
					return true;
				}
			}
			return false;
		}
	}
}
