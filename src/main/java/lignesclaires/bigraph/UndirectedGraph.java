/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.bigraph;

import gnu.trove.TCollections;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import lignesclaires.specs.IEdgeConsumer;
import lignesclaires.specs.IGraphDimension;

public class UndirectedGraph implements IGraphDimension {

	private static final int MIN_CAPACITY = 5;

	private final TIntArrayList[] adjLists;
	private int edgeCount = 0;

	public UndirectedGraph(int nodeCount, int edgeCount) {
		adjLists = new TIntArrayList[nodeCount];
		final int capacity = Math.max(MIN_CAPACITY, 3 * edgeCount / nodeCount);
		for (int i = 0; i < adjLists.length; i++) {
			adjLists[i] = new TIntArrayList(capacity);
		}
	}

	public void addEdge(int i, int j) {
		adjLists[i].add(j);
		adjLists[j].add(i);
		edgeCount++;
	}

	public void sort() {
		for (TIntArrayList adjList : adjLists) {
			adjList.sort();
		}
	}

	@Override
	public final int getNodeCount() {
		return adjLists.length;
	}

	@Override
	public final int getEdgeCount() {
		return edgeCount;
	}

	public final boolean isIsolated(int node) {
		return adjLists[node].isEmpty();
	}

	public final boolean isLeaf(int node) {
		return adjLists[node].size() == 1;
	}

	public final TIntList getNeighbors(int node) {
		return TCollections.unmodifiableList(adjLists[node]);
	}

	public final int getFreeNeighborsCount(int node) {
		return adjLists[node].size();
	}

	public final TIntIterator getNeighborIterator(int node) {
		return TCollections.unmodifiableList(adjLists[node]).iterator();
	}

	public final void forEachEdge(IEdgeConsumer consumer) {
		final int n = getNodeCount();
		for (int i = 0; i < n; i++) {
			final TIntIterator iter = getNeighborIterator(i);
			while (iter.hasNext()) {
				int j = iter.next();
				if (j <= i) {
					consumer.accept(i, j);
				} else {
					break;
				}
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Graph [");
		final int n = getNodeCount();
		b.append("n:").append(n);
		b.append(", e:").append(getEdgeCount());
		b.append("]\n");
		for (int i = 0; i < n; i++) {
			b.append(i).append(": ").append(adjLists[i]).append('\n');
		}
		b.deleteCharAt(b.length() - 1);
		return b.toString();
	}

	public static UndirectedGraph buildGraph1() {
		UndirectedGraph g = new UndirectedGraph(13, 11);

		g.addEdge(1, 7);
		g.addEdge(1, 8);
		g.addEdge(2, 7);
		g.addEdge(2, 8);

		g.addEdge(3, 8);
		g.addEdge(3, 9);
		g.addEdge(3, 10);

		g.addEdge(4, 10);

		g.addEdge(5, 10);
		g.addEdge(5, 11);
		g.addEdge(5, 12);

		g.addEdge(6, 11);
		g.addEdge(6, 12);

		g.sort();
		return g;
	}

	/**
	 * https://en.wikipedia.org/wiki/Bridge_(graph_theory)
	 */
	public static UndirectedGraph buildGraph2() {
		UndirectedGraph g = new UndirectedGraph(17, 11);

		g.addEdge(1, 2);
		g.addEdge(3, 4);
		g.addEdge(3, 7);

		g.addEdge(5, 9);
		g.addEdge(5, 10);

		g.addEdge(7, 8);

		g.addEdge(9, 10);
		g.addEdge(9, 14);

		g.addEdge(10, 11);
		g.addEdge(10, 14);

		g.addEdge(11, 12);
		g.addEdge(11, 15);

		g.addEdge(12, 16);

		g.addEdge(13, 14);

		g.addEdge(15, 16);

		g.sort();
		return g;
	}

	/**
	 * https://en.wikipedia.org/wiki/Biconnected_component
	 * 
	 * @return
	 */
	public static UndirectedGraph buildGraph3() {
		UndirectedGraph g = new UndirectedGraph(19, 11);

		g.addEdge(1, 2);
		g.addEdge(2, 3);
		g.addEdge(2, 4);
		g.addEdge(2, 5);
		g.addEdge(2, 6);
		g.addEdge(3, 4);
		g.addEdge(5, 6);
		g.addEdge(5, 7);
		g.addEdge(6, 7);
		g.addEdge(7, 8);
		g.addEdge(7, 11);
		g.addEdge(8, 9);
		g.addEdge(8, 11);
		g.addEdge(8, 12);
		g.addEdge(8, 14);
		g.addEdge(8, 15);
		g.addEdge(9, 10);
		g.addEdge(9, 11);
		g.addEdge(10, 11);
		g.addEdge(10, 16);
		g.addEdge(10, 17);
		g.addEdge(10, 18);
		g.addEdge(12, 13);
		g.addEdge(13, 14);
		g.addEdge(13, 15);
		g.addEdge(17, 18);
		g.sort();
		return g;
	}

	public static void main(String[] args) {
		UndirectedGraph[] graphs = new UndirectedGraph[] { buildGraph3() };
		DepthFirstSearch dfs = new DepthFirstSearch();
		for (UndirectedGraph g : graphs) {
			System.out.println(g);
			ForestDFS f = dfs.execute(g);
			System.out.println(f);

			System.out.println(dfs);

			System.out.println(f.toDotty());
		}
	}
}