/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.bigraph;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import gnu.trove.TCollections;
import gnu.trove.iterator.TIntIterator;
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
	public final int getEdgeCount() {
		return edgeCount;
	}

	public final TIntList getFreeNeighbors(final int free) {
		return TCollections.unmodifiableList(freeAdjLists[free]);
	}

	public final int getFreeNeighborsCount(final int free) {
		return freeAdjLists[free].size();
	}

	@Override
	public TIntList getFixedNeighbors(int fixed) {
		return TCollections.unmodifiableList(fixedAdjLists[fixed]);
	}

	@Override
	public int getFixedNeighborsCount(int fixed) {
		return fixedAdjLists[fixed].size();
	}

	public static final boolean isEqual(TIntList adjList1, TIntList adjList2) {
		if (adjList1.size() == adjList2.size()) {
			final TIntIterator it1 = adjList1.iterator();
			final TIntIterator it2 = adjList2.iterator();
			while (it1.hasNext()) {
				if (it1.next() != it2.next())
					return false;
			}
			return true;
		}
		return false;
	}

	public static double getMedian(TIntArrayList adjList) {
		if (adjList.isEmpty()) {
			return 0;
		}
		final int n = adjList.size();
		if (n % 2 == 0) {
			// For an even number of elements, average the middle two elements
			final int middleRight = n / 2;
			final int middleLeft = middleRight - 1;
			return (adjList.getQuick(middleLeft) + adjList.getQuick(middleRight)) / 2.0;
		} else {
			// For an odd number of elements, return the middle element
			return adjList.getQuick(n / 2);
		}
	}

	public int[] getFreeMedians() {
		return Stream.of(freeAdjLists).mapToDouble(BipartiteGraph::getMedian).mapToInt(x -> (int) x).toArray();
	}

	private static double getBarycenter(TIntList adjList) {
		return adjList.isEmpty() ? 0 : 1.0 * adjList.sum() / adjList.size();
	}

	public <E> E[] permutateMedians(E[] objects) {
		return permutate(objects, BipartiteGraph::getMedian);
	}

	public <E> E[] permutateBarycenters(E[] objects) {
		return permutate(objects, BipartiteGraph::getBarycenter);
	}

	public <E> E[] permutate(E[] vars, ToDoubleFunction<TIntArrayList> func) {
		final int n = vars.length;
		final Integer[] indices = new Integer[n];
		final double[] values = new double[n];
		for (int i = 0; i < n; i++) {
			indices[i] = Integer.valueOf(i);
			values[i] = func.applyAsDouble(freeAdjLists[i]);
		}
		Arrays.sort(indices, (Integer arg0, Integer arg1) -> Double.compare(values[arg0], values[arg1]));
		return Stream.of(indices).map(i -> vars[i]).toArray(m -> (E[]) Array.newInstance(vars[0].getClass(), m));
	}

	protected int getCrossingCount(int left, int right) {
		int count = 0;
		final int nl = freeAdjLists[left].size();
		final int nr = freeAdjLists[right].size();
		int l = 0;
		int r = 0;
		while (l < nl && r < nr) {
			final int lf = freeAdjLists[left].getQuick(l);
			final int rf = freeAdjLists[right].getQuick(r);
			if (lf <= rf) {
				l++;
			} else {
				r++;
				count += nl - l;
			}
		}
		return count;
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
				Double.parseDouble(args[2]), Long.parseLong(args[0]));
		System.out.println(bigraph.toInputString());
	}

}