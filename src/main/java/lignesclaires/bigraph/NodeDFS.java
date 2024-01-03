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

	public NodeDFS(final int node, final int parent, final int preorder) {
		super();
		this.node = node;
		this.parent = parent;
		this.preorder = preorder;
		this.postorder = -1;
		this.lowest = preorder;
		this.highest = 0;
		this.descendants = 1;
	}

	public int getNode() {
		return node;
	}

	public int getParent() {
		return parent;
	}

	public int getPreorder() {
		return preorder;
	}

	public int getPostorder() {
		return postorder;
	}

	protected void setPostorder(final int postorder) {
		this.postorder = postorder;
	}

	public int getLowest() {
		return lowest;
	}

	public int getHighest() {
		return highest;
	}

	public int getDescendants() {
		return descendants;
	}

	protected void awakeOnOutEdge(final NodeDFS dest) {
		if (this.parent != dest.node) {
			// System.out.println("OUT " + this.node + " -> " + dest.node);
			if (dest.preorder < lowest) {
				lowest = dest.preorder;
			} else if (dest.preorder > highest) {
				highest = dest.preorder;
			}
		}
	}

	protected void awakeOnInEdge(final NodeDFS dest) {
		if (this.node != dest.node) {
			// System.out.println("IN " + this.node + " -> " + dest.node);
			if (dest.lowest < lowest) {
				lowest = dest.lowest;
			}

			if (dest.highest > highest) {
				highest = dest.highest;
			}

			descendants += dest.descendants;
		}
	}

	public boolean isRoot() {
		return parent == node;
	}

	public boolean isBridge() {
		return lowest == preorder && highest < preorder + descendants;
	}

	@Override
	public String toString() {
		return String.format("[ %-3d pa:%-3d pr:%-3d po:%-3d]", node, parent, preorder, postorder);
	}

	protected void toDotty(final DottyFactory f) {
		final String label = String.format("label=\"{{%d|%d|%d}|{%d|%d}}\"", node, preorder, postorder, lowest,
				highest);
		f.addAttributes(node, label);
		final Optional<String> attrs = isBridge() ? Optional.of("color=firebrick") : Optional.empty();
		f.addEdge(parent, node, attrs);
	}

	public static int[] toNodes(final NodeDFS[] nodes) {
		return Stream.of(nodes).mapToInt(NodeDFS::getNode).toArray();
	}

}