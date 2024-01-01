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
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import gnu.trove.list.array.TIntArrayList;
import lignesclaires.bigraph.DepthFirstSearch.StackIterator;

public class BlockDecomposition {

	// Input
	private ForestDFS forest;

	// Output
	private List<TIntArrayList> blocks;

	// Temporary
	private boolean[] decomposed;
	private Deque<StackIterator> stack = new ArrayDeque<>();
	private TIntArrayList block;

	private void setUp(ForestDFS forest) {
		final int n = forest.getGraph().getNodeCount();
		this.forest = forest;
		this.blocks = new ArrayList<>(n);
		decomposed = new boolean[n];
	}

	private void push(int node) {
		block.add(node);
		stack.push(new StackIterator(node, forest.getForest()));
	}

	private void setUp(NodeDFS cutChild) {
		stack.clear();
		block = new TIntArrayList();
		block.add(cutChild.getParent());
		push(cutChild.getNode());
		decomposed[cutChild.getNode()] = true;
	}

	public TIntArrayList searchBlock(NodeDFS cutChild) {
		setUp(cutChild);
		while (!stack.isEmpty()) {
			final StackIterator elt = stack.peek();
			if (elt.hasNext()) {
				final int child = elt.next();
				if (!decomposed[child]) {
					push(child);
				}
			} else {
				stack.pop();
			}
		}
		return block;
	}

	public BlockCutTree execute(ForestDFS forest) {
		setUp(forest);
		for (NodeDFS v : forest.getPostorder()) {
			if (v.isRoot()) {

			} else {
				NodeDFS p = forest.getParent(v);
				if (v.getLowest() >= p.getPreorder()) {
					blocks.add(searchBlock(v));
				}
			}
		}
		return new BlockCutTree(forest, blocks);
	}

}
