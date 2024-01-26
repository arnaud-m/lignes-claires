/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.solver;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.generate.ComplementGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.nio.dot.DOTExporter;

import lignesclaires.LignesClaires;
import lignesclaires.graph.CrossingCounts;
import lignesclaires.graph.JGraphtUtil;
import lignesclaires.graph.TListUtil;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IEdgeConsumer;

public class ReductionRules2 {

	private final IBipartiteGraph graph;
	private final CrossingCounts counts;

	private final DirectedAcyclicGraph<Integer, DefaultEdge> ordered;
	private final Graph<Integer, DefaultEdge> incomparable;
	private final IReductionRule[] rules;

	public ReductionRules2(IBipartiteGraph graph, boolean useRule1, boolean useRule2, boolean useRule3) {
		super();
		this.graph = graph;
		this.counts = graph.getCrossingCounts();
		ordered = JGraphtUtil.directedAcyclic();
		this.incomparable = JGraphtUtil.unweightedUndirected();
		rules = buildRules(useRule1, useRule2, useRule3);
		buildOrderedGraph();
		buildIncomparableGraph();
	}

	public final IBipartiteGraph getBiGraph() {
		return graph;
	}

	public final Graph<Integer, DefaultEdge> getOrderedGraph() {
		return ordered;
	}

	public final Graph<Integer, DefaultEdge> getIncomparableGraph() {
		return incomparable;
	}

	public void forEachOrderedEdge(IEdgeConsumer consumer) {
		JGraphtUtil.forEachEdge(ordered, consumer);
	}

	public void forEachIncomparableEdge(IEdgeConsumer consumer) {
		JGraphtUtil.forEachEdge(incomparable, consumer);
	}

	public final void exportGraph(final String filePathNoExt) {
		LignesClaires.toDotty(ordered, filePathNoExt + "-ordered.dot");
		LignesClaires.toDotty(incomparable, filePathNoExt + "-incomparable.dot");
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
	}

	private void buildOrderedGraph() {
		final int n = graph.getFreeCount();
		JGraphtUtil.addVertices(ordered, n);
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				applyRules(i, j);
			}
		}
		TransitiveReduction.INSTANCE.reduce(ordered);
	}

	private void buildIncomparableGraph() {
		final DirectedAcyclicGraph<Integer, DefaultEdge> closure = new DirectedAcyclicGraph<>(DefaultEdge.class);
		Graphs.addGraph(closure, ordered);
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(closure);
		final ComplementGraphGenerator<Integer, DefaultEdge> complement = new ComplementGraphGenerator<>(
				Graphs.undirectedGraph(closure));
		complement.generateGraph(incomparable);
	}

	interface IReductionRule {

		boolean apply(int i, int j);

	}

	public class ReductionRule1 implements IReductionRule {

		@Override
		public boolean apply(int i, int j) {
			if (counts.getCrossingCount(i, j) > 0) {
				if (counts.getCrossingCount(j, i) == 0) {
					ordered.addEdge(j, i);
					return true;
				}
			} else if (counts.getCrossingCount(j, i) > 0) {
				ordered.addEdge(i, j);
				return true;
			}
			return false;
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
			if (graph.getFreeDegree(i) == 2 && graph.getFreeDegree(j) == 2) {
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

	@Override
	public String toString() {
		DOTExporter<Integer, DefaultEdge> exporter = new DOTExporter<>();
		Writer writer = new StringWriter();
		exporter.exportGraph(ordered, writer);
		System.out.println(writer.toString());
		return super.toString();
	}
}
