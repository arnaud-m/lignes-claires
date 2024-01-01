/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.bigraph;

import java.util.Optional;
import java.util.Random;

import gnu.trove.list.TIntList;
import lignesclaires.specs.IBipartiteGraph;

public class BipartiteGraph extends UGraph implements IBipartiteGraph {

	private final int fixedCount;
	private final int freeCount;
	private final int freeOffset;

	Optional<CrossingCounts> reducedCrossingCounts;
	Optional<CrossingCounts> crossingCounts;

	public BipartiteGraph(int fixedCount, int freeCount, int edgeCount) {
		super(fixedCount + freeCount + 1, edgeCount);
		this.fixedCount = fixedCount;
		this.freeCount = freeCount;
		this.freeOffset = fixedCount + 1;
		reducedCrossingCounts = Optional.empty();
		crossingCounts = Optional.empty();
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
	public final TIntList getFreeNeighbors(final int free) {
		return getNeighbors(freeOffset + free);
	}

	@Override
	public final int getDegree(final int free) {
		return getOutDegree(freeOffset + free);
	}

	public <E> E[] permutateMedians(E[] objects) {
		return AdjListUtil.permutate(objects, AdjListUtil::getMedian, adjLists, freeOffset);
	}

	public <E> E[] permutateBarycenters(E[] objects) {
		return AdjListUtil.permutate(objects, AdjListUtil::getBarycenter, adjLists, freeOffset);
	}

	protected int getCrossingCount(int left, int right) {
		return AdjListUtil.getCrossingCount(adjLists[freeOffset + left], adjLists[freeOffset + right]);
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

	public static BipartiteGraph generate(int fixedCount, int freeCount, double density, long seed) {
		final Random rnd = new Random(seed);
		final int expectedEdgeCount = (int) Math.ceil(density * fixedCount * freeCount);
		final BipartiteGraph graph = new BipartiteGraph(fixedCount, freeCount, expectedEdgeCount);
		for (int i = 1; i <= fixedCount; i++) {
			for (int j = 1; j <= freeCount; j++) {
				if (rnd.nextDouble() < density) {
					graph.addEdge(i, i + j);
				}
			}
		}
		return graph;
	}

	private void appendEdgeList(final StringBuilder b, final int i) {
		adjLists[i].forEach(j -> {
			b.append(i).append(" ").append(j).append('\n');
			return true;
		});
	}

	public String toPaceInputString() {
		final StringBuilder b = new StringBuilder();
		b.append("p ocr");
		b.append(" ").append(getFixedCount());
		b.append(" ").append(getFreeCount());
		b.append(" ").append(getEdgeCount());
		b.append('\n');
		final int n = getFixedCount();
		for (int i = 1; i <= n; i++) {
			appendEdgeList(b, i);
		}
		b.deleteCharAt(b.length() - 1);
		return b.toString();
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("BipartiteGraph [");
		b.append("fixed:").append(getFixedCount());
		b.append(", free:").append(getFreeCount());
		b.append(", edges:").append(getEdgeCount());
		b.append("][Free Layer]\n");
		for (int i = freeOffset; i < getNodeCount(); i++) {
			b.append(i).append(": ").append(adjLists[i]).append('\n');
		}
		b.deleteCharAt(b.length() - 1);
		return b.toString();
	}

	public static void main(String[] args) {
		BipartiteGraph bigraph = generate(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
				Double.parseDouble(args[2]), Long.parseLong(args[3]));
		System.out.println(bigraph.toPaceInputString());
	}

}