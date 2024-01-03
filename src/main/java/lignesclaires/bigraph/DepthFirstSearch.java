/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.bigraph;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import lignesclaires.specs.IGenericGraph;

/**
 * https://en.wikipedia.org/wiki/Bridge_(graph_theory)
 * 
 * Tarjan's bridge-finding algorithm
 * 
 */
public class DepthFirstSearch {

	// Input
	private IGenericGraph graph;

	// Output
	private NodeDFS[] data;

	// Temporary
	private int root;
	private int preNum;
	private int postNum;
	private Deque<StackIterator> stack = new ArrayDeque<>();

	private void setUp(final IGenericGraph graph) {
		this.graph = graph;
		final int n = graph.getNodeCount();
		data = new NodeDFS[n];
		root = 0;
		preNum = 0;
		postNum = 0;
		stack.clear();
	}

	private boolean findRoot() {
		while (root < data.length) {
			if (data[root] == null) {
				pushNode(root, root);
				return true;
			}
			root++;
		}
		return false;
	}

	private void pushNode(final int node, final int parent) {
		data[node] = new NodeDFS(node, parent, preNum++);
		stack.push(new StackIterator(node, graph.getNeighborIterator(node)));
	}

	private void popNode(final int node) {
		stack.pop();
		data[node].setPostorder(postNum++);
	}

	public ForestDFS execute(final IGenericGraph graph) {
		setUp(graph);
		while (findRoot()) {
			while (!stack.isEmpty()) {
				final StackIterator elt = stack.peek();
				final int node = elt.node;
				if (elt.hasNext()) {
					final int child = elt.next();
					if (data[child] == null) {
						pushNode(child, node);
					} else {
						data[node].awakeOnOutEdge(data[child]);
					}
				} else {
					popNode(node);
					data[data[node].getParent()].awakeOnInEdge(data[node]);
				}

			}
		}
		return new ForestDFS(graph, data);
	}

	protected static final class StackIterator implements TIntIterator {
		public final int node;
		private final TIntIterator iter;

		public StackIterator(final int index, final IGenericGraph graph) {
			super();
			this.node = index;
			this.iter = graph.getNeighborIterator(index);
		}

		public StackIterator(final int index, final TIntIterator iter) {
			super();
			this.node = index;
			this.iter = iter;
		}

		public int getNode() {
			return node;
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public void remove() {
			iter.remove();
		}

		@Override
		public int next() {
			return iter.next();
		}

	}

	public static <E> String toString(Stream<E> stream, CharSequence delimiter) {
		return stream.map(Object::toString).collect(Collectors.joining(delimiter));
	}

	public static final String toString(int[] values, CharSequence delimiter) {
		return toString(IntStream.of(values), delimiter);
	}

	public static final String toString(IntStream stream, CharSequence delimiter) {
		return stream.mapToObj(Integer::toString).collect(Collectors.joining(delimiter));
	}

	public static String toString(TIntCollection collection, CharSequence delimiter) {
		final StringBuilder b = new StringBuilder();
		for (TIntIterator it = collection.iterator(); it.hasNext();) {
			b.append(it.next()).append(delimiter);
		}
		b.delete(b.length() - delimiter.length(), b.length());
		return b.toString();
	}

	public static String toString(Object[] o, CharSequence delimiter) {
		return toString(Stream.of(o), delimiter);
	}

	@Override
	public String toString() {
		return "DepthFirstSearch:\n" + toString(data, "\n");
	}

}