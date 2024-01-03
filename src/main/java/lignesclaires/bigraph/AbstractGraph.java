/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.bigraph;

import gnu.trove.TCollections;
import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import lignesclaires.specs.IGenericGraph;

public abstract class AbstractGraph implements IGenericGraph {

	protected final TIntArrayList[] adjLists;

	protected int edgeCount = 0;

	protected AbstractGraph(final TIntArrayList[] adjLists) {
		super();
		this.adjLists = adjLists;
	}

	protected AbstractGraph(final int n, final int capacity) {
		this(TListUtil.createArrayOfTLists(n, capacity));
	}

	protected AbstractGraph(final int n) {
		this(n, Constants.DEFAULT_CAPACITY);
	}

	public abstract void addEdge(final int i, final int j);

	public final void sort() {
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

	@Override
	public final boolean isIsolated(final int node) {
		return adjLists[node].isEmpty();
	}

	@Override
	public final boolean isLeaf(final int node) {
		return adjLists[node].size() == 1;
	}

	@Override
	public final TIntList getNeighbors(final int node) {
		return TCollections.unmodifiableList(adjLists[node]);
	}

	@Override
	public final int getOutDegree(final int node) {
		return adjLists[node].size();
	}

	@Override
	public final TIntIterator getNeighborIterator(final int node) {
		return TCollections.unmodifiableList(adjLists[node]).iterator();
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

	@Override
	public String toDotty() {
		DottyFactory f = new DottyFactory(isDirected());
		f.beginGraph();
		f.beginBlock("shape=plain");
		forEachEdge(f::addEdge);
		f.endBlock();
		f.endGraph();
		return f.toString();
	}

}