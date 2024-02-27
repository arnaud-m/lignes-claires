/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.graph;

import java.util.Optional;
import java.util.function.IntToDoubleFunction;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import lignesclaires.specs.IBipartiteGraph;

public class BGraph extends DefaultGraph<Integer, DefaultEdge> implements IBipartiteGraph {

	private final int fixedCount;
	private final int freeCount;
	private final int freeOffset;

	private Optional<TIntArrayList[]> freeAdjLists;

	private Optional<CrossingCounts> crossingCounts;

	private Optional<CrossingCounts> reducedCrossingCounts;

	public BGraph(Graph<Integer, DefaultEdge> graph, int fixedCount, int freeCount) {
		super(graph);
		this.fixedCount = fixedCount;
		this.freeCount = freeCount;
		this.freeOffset = fixedCount + 1;
		freeAdjLists = Optional.empty();
		crossingCounts = Optional.empty();
		reducedCrossingCounts = Optional.empty();
	}

	@Override
	public final int getFixedCount() {
		return fixedCount;
	}

	@Override
	public final int getFreeCount() {
		return freeCount;
	}

	@Override
	public final int getFreeNode(final int free) {
		return freeOffset + free;
	}

	private TIntArrayList getNeighbors(int node) {
		final TIntArrayList neighbors = new TIntArrayList(graph.outDegreeOf(node));
		for (Integer v : Graphs.neighborListOf(graph, node)) {
			neighbors.add(v);
		}
		neighbors.sort();
		return neighbors;
	}

	private void buildFreeAdjacencyLists() {
		final TIntArrayList[] adjLists = new TIntArrayList[freeCount];
		for (int i = 0; i < freeCount; i++) {
			adjLists[i] = getNeighbors(freeOffset + i);
		}
		freeAdjLists = Optional.of(adjLists);
	}

	protected TIntArrayList[] getFreeAdjacencyLists() {
		if (freeAdjLists.isEmpty()) {
			buildFreeAdjacencyLists();
		}
		return freeAdjLists.get();
	}

	public void computeCutwidth() {
		final int n = getFixedCount();
		final int m = getFreeCount();
		final int[] cut = new int[m];
		TIntArrayList[] adjLists = getFreeAdjacencyLists();
		TIntSet freeIn = new TIntHashSet();
		TIntSet freeCut = new TIntHashSet();
		TIntArrayList fixedCut = new TIntArrayList();
		for (int i = 1; i <= n; i++) {
			for (int free : Graphs.successorListOf(graph, i)) {
				int j = free - freeOffset;
				if (cut[j] == 0) {
					cut[j] = adjLists[j].size();
					freeCut.add(j);
				}
				cut[j]--;
				if (cut[j] == 0) {
					freeCut.remove(j);
					freeIn.add(j);
				}

			}
			System.out.println(i + "\n" + freeIn + "\n" + freeCut + "\n");
		}
		System.err.println("Test");
	}

	@Override
	public final int getFreeDegree(final int free) {
		return graph.degreeOf(freeOffset + free);
	}

	// TODO Many possible redundant computations of medians and barycenters
	public final IntToDoubleFunction getFreeMedians() {
		final TIntArrayList[] adjLists = getFreeAdjacencyLists();
		return i -> TListUtil.getMedian(adjLists[i]);
	}

	public final IntToDoubleFunction getFreeBarycenters() {
		final TIntArrayList[] adjLists = getFreeAdjacencyLists();
		return i -> TListUtil.getBarycenter(adjLists[i]);
	}

	public Integer[] permutateMedians() {
		final TIntArrayList[] adjLists = getFreeAdjacencyLists();
		return TListUtil.permutate(getFreeCount(), i -> TListUtil.getMedian(adjLists[i]));
	}

	public <E> E[] permutateMedians(final E[] objects) {
		final TIntArrayList[] adjLists = getFreeAdjacencyLists();
		return TListUtil.permutate(objects, i -> TListUtil.getMedian(adjLists[i]));
	}

	public <E> E[] permutateBarycenters(final E[] objects) {
		final TIntArrayList[] adjLists = getFreeAdjacencyLists();
		return TListUtil.permutate(objects, i -> TListUtil.getBarycenter(adjLists[i]));
	}

	public Integer[] permutateBarycenters() {
		final TIntArrayList[] adjLists = getFreeAdjacencyLists();
		return TListUtil.permutate(getFreeCount(), i -> TListUtil.getBarycenter(adjLists[i]));
	}

	protected void buildCrossingCounts() {
		final int n = getFreeCount();
		final int[][] counts = new int[n][n];
		final int[][] redCounts = new int[n][n];
		int constant = 0;
		final TIntArrayList[] adjLists = getFreeAdjacencyLists();
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				counts[i][j] = TListUtil.getCrossingCount(adjLists[i], adjLists[j]);
				counts[j][i] = TListUtil.getCrossingCount(adjLists[j], adjLists[i]);
				final int min = Math.min(counts[i][j], counts[j][i]);
				constant += min;
				redCounts[i][j] = counts[i][j] - min;
				redCounts[j][i] = counts[j][i] - min;
			}
		}
		this.crossingCounts = Optional.of(new CrossingCounts(counts, 0));
		this.reducedCrossingCounts = Optional.of(new CrossingCounts(redCounts, constant));
	}

	@Override
	public final CrossingCounts getReducedCrossingCounts() {
		if (reducedCrossingCounts.isEmpty()) {
			buildCrossingCounts();
			GraphLogger.logOnCrossingCounts(this);
		}
		return reducedCrossingCounts.get();
	}

	@Override
	public final CrossingCounts getCrossingCounts() {
		if (crossingCounts.isEmpty()) {
			buildCrossingCounts();
			GraphLogger.logOnCrossingCounts(this);
		}
		return crossingCounts.get();
	}

}