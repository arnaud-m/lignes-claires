/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.bigraph;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gnu.trove.iterator.TIntIterator;
import lignesclaires.specs.IGenericGraph;

public class ForestDFS {

	private final IGenericGraph graph;
	private final NodeDFS[] data;

	private Optional<IGenericGraph> forest;
	private Optional<NodeDFS[]> preorder;
	private Optional<NodeDFS[]> postorder;

	private Optional<NodeDFS[]> roots;

	public ForestDFS(IGenericGraph graph, NodeDFS[] dataDFS) {
		super();
		this.graph = graph;
		this.data = dataDFS;
		this.preorder = Optional.empty();
		this.postorder = Optional.empty();
	}

	public final IGenericGraph getGraph() {
		return graph;
	}

	public final NodeDFS[] getData() {
		return data;
	}

	public final NodeDFS getNode(int node) {
		return data[node];
	}

	public final NodeDFS getParent(NodeDFS node) {
		return data[node.getParent()];
	}

	public final IGenericGraph getForest() {
		if (preorder.isEmpty()) {
			final DGraph f = new DGraph(graph.getNodeCount(), graph.getNodeCount());
			for (NodeDFS n : getPreorder()) {
				if (!n.isRoot()) {
					f.addEdge(n.getParent(), n.getNode());
				}
			}
			forest = Optional.of(f);
		}
		return forest.get();
	}

	int[] computeSubTreeArcCounts() {
		int[] counts = new int[graph.getNodeCount()];
		IGenericGraph f = getForest();
		for (NodeDFS n : getPostorder()) {
			final int i = n.getNode();
			counts[i] = graph.getOutDegree(i);
			TIntIterator it1 = f.getNeighborIterator(n.getNode());
			while (it1.hasNext()) {
				counts[i] += counts[it1.next()];
			}
		}
		return counts;
	}

	ArrayList<NodeDFS> getOrderInducingBridges(int threshold) {
		final ArrayList<NodeDFS> bridges = new ArrayList<>();
		int[] counts = computeSubTreeArcCounts();
		int[] r = new int[graph.getNodeCount()];
		for (NodeDFS n : getPreorder()) {
			final int i = n.getNode();
			r[i] = n.isRoot() ? i : r[n.getParent()];
			if (!n.isRoot() && n.isBridge()) {
				final int cl = (counts[r[i]] - counts[i] - 1) / 2;
				final int cr = (counts[i] - 1) / 2;
				if (cl >= threshold && cr >= threshold) {
					bridges.add(n);
				}
			}
		}
		return bridges;
	}

	public final NodeDFS[] getPreorder() {
		if (preorder.isEmpty()) {
			final NodeDFS[] pre = new NodeDFS[data.length];
			for (NodeDFS n : data) {
				pre[n.getPreorder()] = n;
			}
			preorder = Optional.of(pre);
		}
		return preorder.get();
	}

	public final NodeDFS[] getPostorder() {
		if (postorder.isEmpty()) {
			final NodeDFS[] post = new NodeDFS[data.length];
			for (NodeDFS n : data) {
				post[n.getPostorder()] = n;
			}
			postorder = Optional.of(post);
		}
		return postorder.get();
	}

	public final NodeDFS[] getRoots() {
		if (roots.isEmpty()) {
			NodeDFS[] r = Stream.of(data).filter(NodeDFS::isRoot).toArray(n -> new NodeDFS[n]);
			roots = Optional.of(r);
		}
		return roots.get();
	}

	public final boolean isIn(int i, int j) {
		return i == data[j].getParent() || j == data[i].getParent();
	}

	private void toDottyIn(StringBuilder b) {
		String spanningForest = Stream.of(data).map(NodeDFS::toDotty).collect(Collectors.joining("\n"));
		b.append("{\nnode[shape=record];\nedge[style=bold];\n");
		b.append(spanningForest);
		b.append("\n}\n");
	}

	private void toDottyOut(StringBuilder b) {
		b.append("{\nedge[style=dashed];\n");
		graph.forEachEdge((i, j) -> {
			if (!isIn(i, j)) {
				b.append(i).append(" -- ").append(j).append(";\n");
			}
		});
		b.append("}\n");
	}

	public String toDotty() {
		StringBuilder b = new StringBuilder();
		b.append("graph G{\n");
		toDottyIn(b);
		toDottyOut(b);
		b.append("}\n");
		return b.toString();
	}

	public String toString() {
		return DepthFirstSearch.toString(data);
	}
}