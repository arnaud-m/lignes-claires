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

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import lignesclaires.specs.IBipartiteGraph;

public class BGraph extends AbstractGraph implements IBipartiteGraph {

	private final int fixedCount;
	private final int freeCount;
	private final int freeOffset;

	Optional<CrossingCounts> crossingCounts;
	Optional<CrossingCounts> reducedCrossingCounts;

	public BGraph(Graph<Integer, DefaultEdge> graph, int fixedCount, int freeCount) {
		super(graph);
		this.fixedCount = fixedCount;
		this.freeCount = freeCount;
		this.freeOffset = fixedCount + 1;
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

	@Override
	public final TIntList getFreeNeighbors(final int free) {
		return getNeighbors(freeOffset + free);
	}

	@Override
	public final int getFreeDegree(final int free) {
		return graph.degreeOf(freeOffset + free);
	}

	public <E> E[] permutateMedians(final E[] objects) {
		return TListUtil.permutate(objects, i -> TListUtil.getMedian(getNeighbors(freeOffset + i)));
	}

	public <E> E[] permutateBarycenters(final E[] objects) {
		return TListUtil.permutate(objects, i -> TListUtil.getBarycenter(getNeighbors(freeOffset + i)));
	}

	public TIntArrayList getNeighbors(int node) {
		final TIntArrayList neighbors = new TIntArrayList(graph.degreeOf(node));
		for (Integer v : Graphs.neighborListOf(graph, node)) {
			neighbors.add(v);
		}
		neighbors.sort();
		return neighbors;
	}

	protected int getCrossingCount(int left, int right) {
		return TListUtil.getCrossingCount(getNeighbors(freeOffset + left), getNeighbors(freeOffset + right));
	}

	protected void computeCrossingCounts() {
		final int n = getFreeCount();
		final int[][] counts = new int[n][n];
		final int[][] redCounts = new int[n][n];
		int constant = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				counts[i][j] = getCrossingCount(i, j);
				counts[j][i] = getCrossingCount(j, i);
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
			computeCrossingCounts();
		}
		return reducedCrossingCounts.get();
	}

	@Override
	public final CrossingCounts getCrossingCounts() {
		if (crossingCounts.isEmpty()) {
			computeCrossingCounts();
		}
		return crossingCounts.get();
	}

}