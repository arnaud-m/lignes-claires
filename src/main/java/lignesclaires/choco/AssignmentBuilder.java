/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.choco;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import org.chocosolver.solver.variables.IntVar;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm.Matching;
import org.jgrapht.alg.matching.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.nio.dot.DOTExporter;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import lignesclaires.graph.CrossingCounts;
import lignesclaires.graph.JGraphtUtil;
import lignesclaires.parser.PACEImporter;
import lignesclaires.specs.IBipartiteGraph;

public class AssignmentBuilder {

	private final Set<Integer> setR;
	private final Set<Integer> setC;

	private final Graph<Integer, DefaultWeightedEdge> cbigraph;

	private final AssignmentRowBuilder[] builders;

	private final Integer[] indices;
	private final TIntArrayList[] eventsLB;
	private final TIntArrayList[] eventsUB;

	public AssignmentBuilder(IBipartiteGraph bigraph) {
		super();
		final int n = bigraph.getFreeCount();
		cbigraph = buildCompleteBipartiteGraph(n);
		setR = buildPartitionSet(n, 0);
		setC = buildPartitionSet(n, n);
		builders = new AssignmentRowBuilder[n];
		indices = new Integer[n];
		eventsLB = new TIntArrayList[n];
		eventsUB = new TIntArrayList[n];
		final CrossingCounts counts = bigraph.getReducedCrossingCounts();
		for (int i = 0; i < n; i++) {
			indices[i] = i;
			builders[i] = counts.getHRowBuilder(i);
			eventsLB[i] = new TIntArrayList();
			eventsUB[i] = new TIntArrayList();

		}

	}

	private static Set<Integer> buildPartitionSet(final int n, final int offset) {
		final Set<Integer> s1 = new HashSet<>();
		for (int i = offset; i < offset + n; i++) {
			s1.add(i);
		}
		return s1;
	}

	private static Graph<Integer, DefaultWeightedEdge> buildCompleteBipartiteGraph(final int n) {
		final Graph<Integer, DefaultWeightedEdge> graph = GraphTypeBuilder.<Integer, DefaultWeightedEdge>undirected()
				.allowingMultipleEdges(false).allowingSelfLoops(false).edgeClass(DefaultWeightedEdge.class)
				.weighted(true).buildGraph();
		for (int i = 0; i < n; i++) {
			graph.addVertex(i);
			graph.addVertex(n + i);
		}
		for (int i = 0; i < n; i++) {
			for (int j = n; j < 2 * n; j++) {
				graph.addEdge(i, j);
			}
		}
		return graph;
	}

	public void setUp() {
		final int n = builders.length;
		final int costUB = n * n * n;
		for (int i = 0; i < n; i++) {
			for (int j = n; j < 2 * n; j++) {
				cbigraph.setEdgeWeight(i, j, costUB);
			}
			builders[i].setUp();
			eventsLB[i].clear();
			eventsUB[i].clear();
		}
	}

	public void setUp(IntVar[] positions) {
		setUp();
		for (int i = 0; i < positions.length; i++) {
			eventsLB[positions[i].getLB()].add(i);
			eventsUB[positions[i].getUB()].add(i);
		}
	}

	private void updateBuilders(int col) {
		final TIntIterator it = eventsUB[col].iterator();
		while (it.hasNext()) {
			int k = it.next();
			for (int j = 0; j < k; j++) {
				builders[j].remove(k);
			}
			for (int j = k + 1; j < builders.length; j++) {
				builders[j].remove(k);
			}
		}
	}

	private void nextBuilders() {
		for (AssignmentRowBuilder builder : builders) {
			builder.next();
		}
	}

	private void updateMatrix(int col, TIntList rows) {
		final int n = builders.length;
		TIntIterator it = rows.iterator();
		while (it.hasNext()) {
			int row = it.next();
			cbigraph.setEdgeWeight(row, n + col, builders[row].getCrossingCount());
		}
	}

	public void buildAssignmentGraph(IntVar[] positions) {
		setUp(positions);
		final TIntLinkedList rows = new TIntLinkedList();
		for (int i = 0; i < positions.length; i++) {
			updateBuilders(i);
			rows.addAll(eventsLB[i]);
			updateMatrix(i, rows);
			rows.removeAll(eventsUB[i]);
			nextBuilders();
		}
	}

	public final Graph<Integer, DefaultWeightedEdge> getAssignmentGraph() {
		return cbigraph;
	}

	public Matching<Integer, DefaultWeightedEdge> solveAssignment() {
		KuhnMunkresMinimalWeightBipartitePerfectMatching<Integer, DefaultWeightedEdge> hungarian = new KuhnMunkresMinimalWeightBipartitePerfectMatching<Integer, DefaultWeightedEdge>(
				cbigraph, setR, setC);
		return hungarian.getMatching();

	}

	public static void main(String[] args) {
		PACEImporter<Integer, DefaultEdge> importer = new PACEImporter<>();
		importer.setVertexFactory(i -> i);
		Graph<Integer, DefaultEdge> graph = JGraphtUtil.unweightedUndirected();
		importer.importGraph(graph,
				new StringReader("c this is a comment\n" + "p ocr 3 3 3\n" + "1 5\n" + "2 4\n" + "3 6"));
		System.out.println(graph);
		System.out.println(JGraphtUtil.toString(graph));
		DOTExporter<Integer, DefaultEdge> exporter = JGraphtUtil.plainDotExporter();
		StringWriter writer = new StringWriter();
		exporter.exportGraph(graph, writer);
		System.out.println(writer);
	}

}
