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

import gnu.trove.TCollections;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import lignesclaires.specs.IBipartiteGraph;

public class BipartiteGraph implements IBipartiteGraph {

	private static final int MIN_CAPACITY = 5;

	private final TIntArrayList[] fixedAdjLists;
	private final TIntArrayList[] freeAdjLists;
	private int edgeCount = 0;

	Optional<CrossingCounts> reducedCrossingCounts;
	Optional<CrossingCounts> crossingCounts;

	protected BipartiteGraph(int fixedCount, int freeCount, int edgeCount) {
		fixedAdjLists = new TIntArrayList[fixedCount];
		freeAdjLists = new TIntArrayList[freeCount];
		final int capacity = Math.max(MIN_CAPACITY, 3 * edgeCount / (freeCount + fixedCount));
		for (int i = 0; i < fixedAdjLists.length; i++) {
			fixedAdjLists[i] = new TIntArrayList(capacity);
		}
		for (int i = 0; i < freeAdjLists.length; i++) {
			freeAdjLists[i] = new TIntArrayList(capacity);
		}
		reducedCrossingCounts = Optional.empty();
		crossingCounts = Optional.empty();
	}

	protected void sort() {
		for (int i = 0; i < fixedAdjLists.length; i++) {
			fixedAdjLists[i].sort();
		}
		for (int i = 0; i < freeAdjLists.length; i++) {
			freeAdjLists[i].sort();
		}
	}

	@Override
	public final int getFixedCount() {
		return fixedAdjLists.length;
	}

	@Override
	public final int getFreeCount() {
		return freeAdjLists.length;
	}

	@Override
	public int getNodeCount() {
		return getFixedCount() + getNodeCount();
	}

	@Override
	public final int getEdgeCount() {
		return edgeCount;
	}

	@Override
	public final TIntList getFreeNeighbors(final int free) {
		return TCollections.unmodifiableList(freeAdjLists[free]);
	}

	@Override
	public final int getFreeNeighborsCount(final int free) {
		return freeAdjLists[free].size();
	}

	public <E> E[] permutateMedians(E[] objects) {
		return AdjListUtil.permutate(objects, AdjListUtil::getMedian, freeAdjLists, 0);
	}

	public <E> E[] permutateBarycenters(E[] objects) {
		return AdjListUtil.permutate(objects, AdjListUtil::getBarycenter, freeAdjLists, 0);
	}

	protected int getCrossingCount(int left, int right) {
		return AdjListUtil.getCrossingCount(freeAdjLists[left], freeAdjLists[right]);
	}

	protected void computeCrossingCounts() {
		final int n = freeAdjLists.length;
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
		final BipartiteGraph.Builder builder = new BipartiteGraph.Builder(fixedCount, freeCount, expectedEdgeCount);
		for (int i = 0; i < fixedCount; i++) {
			for (int j = 0; j < freeCount; j++) {
				if (rnd.nextDouble() < density) {
					builder.addEdge(i, j);
				}
			}
		}
		return builder.build();
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("BipartiteGraph [");
		final int n = getFreeCount();
		b.append("fixed:").append(getFixedCount());
		b.append(", free:").append(n);
		b.append(", edges:").append(getEdgeCount());
		b.append("][Free Layer]\n");
		for (int i = 0; i < n; i++) {
			b.append(i).append(": ").append(freeAdjLists[i]).append('\n');
		}
		b.deleteCharAt(b.length() - 1);
		return b.toString();
	}

	public static final class Builder {

		private BipartiteGraph bigraph;

		public Builder(int fixedCount, int freeCount, int edgeCount) {
			bigraph = new BipartiteGraph(fixedCount, freeCount, edgeCount);
		}

		public void addEdge(final int fixed, final int free) {
			bigraph.fixedAdjLists[fixed].add(free);
			bigraph.freeAdjLists[free].add(fixed);
			bigraph.edgeCount++;
		}

		public void addGrEdge(final int fixed, final int free) {
			addEdge(fixed - 1, free - bigraph.getFixedCount() - 1);
		}

		public BipartiteGraph build() {
			bigraph.sort();
			return bigraph;
		}
	}

	public String toInputString() {
		final StringBuilder b = new StringBuilder();
		b.append("p ocr");
		b.append(" ").append(getFixedCount());
		b.append(" ").append(getFreeCount());
		b.append(" ").append(getEdgeCount());
		b.append('\n');
		final int n = getFixedCount();
		for (int i = 0; i < n; i++) {
			final int fixed = i + 1;
			fixedAdjLists[i].forEach(j -> {
				b.append(fixed).append(" ").append(j + n + 1).append('\n');
				return true;
			});
		}
		b.deleteCharAt(b.length() - 1);
		return b.toString();
	}

	public static void main(String[] args) {
		BipartiteGraph bigraph = generate(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
				Double.parseDouble(args[2]), Long.parseLong(args[3]));
		System.out.println(bigraph.toInputString());
	}

}