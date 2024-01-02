/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.bigraph;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

import gnu.trove.iterator.TIntIterator;
import lignesclaires.specs.IDotty;
import lignesclaires.specs.IGenericGraph;

public class ForestDFS implements IDotty {

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
		this.forest = Optional.empty();
		this.preorder = Optional.empty();
		this.postorder = Optional.empty();
		this.roots = Optional.empty();
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
		if (forest.isEmpty()) {
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

	private void toDottyIn(DottyFactory f) {
		f.beginBlock("shape=record", "style=bold");
		Stream.of(data).forEach(n -> n.toDotty(f));
		f.endBlock();
	}

	private void toDottyOut(DottyFactory f) {
		f.beginBlock(Optional.empty(), Optional.of("style=dashed"));
		graph.forEachEdge((i, j) -> {
			if (!isIn(i, j))
				f.addEdge(i, j);

		});
		f.endBlock();
	}

	@Override
	public String toDotty() {
		DottyFactory f = new DottyFactory(graph.isDirected());
		f.beginGraph();
		toDottyIn(f);
		toDottyOut(f);
		f.endGraph();
		return f.toString();
	}

	public String toString() {
		return DepthFirstSearch.toString(data);
	}
}