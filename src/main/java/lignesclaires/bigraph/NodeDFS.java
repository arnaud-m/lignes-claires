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
import java.util.stream.Stream;

public final class NodeDFS {

	private final int node;
	private final int parent;
	private final int preorder;
	private int postorder;
	private int lowest;
	private int highest;
	private int descendants;

	public NodeDFS(int node, int parent, int preorder) {
		super();
		this.node = node;
		this.parent = parent;
		this.preorder = preorder;
		this.postorder = -1;
		this.lowest = preorder;
		this.highest = 0;
		this.descendants = 1;
	}

	public final int getNode() {
		return node;
	}

	public final int getParent() {
		return parent;
	}

	public final int getPreorder() {
		return preorder;
	}

	public final int getPostorder() {
		return postorder;
	}

	protected final void setPostorder(int postorder) {
		this.postorder = postorder;
	}

	public final int getLowest() {
		return lowest;
	}

	public final int getHighest() {
		return highest;
	}

	public final int getDescendants() {
		return descendants;
	}

	protected void awakeOnOutEdge(NodeDFS dest) {
		if (this.parent != dest.node) {
			// System.out.println("OUT " + this.node + " -> " + dest.node);
			if (dest.preorder < lowest)
				lowest = dest.preorder;
			else if (dest.preorder > highest)
				highest = dest.preorder;
		}
	}

	protected void awakeOnInEdge(NodeDFS dest) {
		if (this.node != dest.node) {
			// System.out.println("IN " + this.node + " -> " + dest.node);
			if (dest.lowest < lowest)
				lowest = dest.lowest;

			if (dest.highest > highest)
				highest = dest.highest;

			descendants += dest.descendants;
		}
	}

	public final boolean isRoot() {
		return parent == node;
	}

	public final boolean isBridge() {
		return lowest == preorder && highest < preorder + descendants;
	}

	@Override
	public String toString() {
		return String.format("[ %-3d pa:%-3d pr:%-3d po:%-3d]", node, parent, preorder, postorder);
	}

	protected void toDotty(DottyFactory f) {
		final String label = String.format("label=\"{{%d|%d|%d}|{%d|%d}}\"", node, preorder, postorder, lowest,
				highest);
		f.addAttributes(node, label);
		final Optional<String> attrs = isBridge() ? Optional.of("color=firebrick") : Optional.empty();
		f.addEdge(parent, node, attrs);
	}

	public static int[] toNodes(NodeDFS[] nodes) {
		return Stream.of(nodes).mapToInt(NodeDFS::getNode).toArray();
	}
//	public String toDotty() {
//		final StringBuilder b = new StringBuilder();
//		b.append(String.format("%d [label=\"{{%d|%d|%d}|{%d|%d}}\"];", node, node, preorder, postorder, lowest,
//				highest));
//		b.append('\n');
//		final String attrs = isBridge() ? " [color=firebrick]" : "";
//		b.append(String.format("%d -- %d%s;", parent, node, attrs));
//		return b.toString();
//	}

}