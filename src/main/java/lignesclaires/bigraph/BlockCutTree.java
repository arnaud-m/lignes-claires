/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.bigraph;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.OptionalInt;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import lignesclaires.specs.IDotty;

public class BlockCutTree implements IDotty {

	private final ForestDFS forest;

	private final List<TIntArrayList> blocks;

	private Optional<TIntSet> cuts;

	private OptionalInt localCrossingsLB;

	public BlockCutTree(final ForestDFS forest, final List<TIntArrayList> blocks) {
		super();
		this.forest = forest;
		this.blocks = blocks;
		this.cuts = Optional.empty();
		this.localCrossingsLB = OptionalInt.empty();
	}

	protected final TIntArrayList[] computeNodeBlocks() {
		final TIntArrayList[] nblocks = TListUtil.createArrayOfTLists(forest.getGraph().getNodeCount());
		final ListIterator<TIntArrayList> itB = blocks.listIterator();
		while (itB.hasNext()) {
			final TIntIterator itN = itB.next().iterator();
			final int idx = itB.previousIndex();
			while (itN.hasNext()) {
				nblocks[itN.next()].add(idx);
			}
		}
		return nblocks;
	}

	protected final int[] computeBlockEdgeCounts() {
		final int[] edges = new int[blocks.size()];
		TIntArrayList[] nblocks = computeNodeBlocks();
		forest.getGraph().forEachEdge((i, j) -> {
			final int k = TListUtil.intersectSingloton(nblocks[i], nblocks[j]);
			edges[k]++;
		});
		return edges;
	}

	public final int getLocalCrossingsLB() {
		if (localCrossingsLB.isEmpty()) {
			final int[] edges = computeBlockEdgeCounts();
			localCrossingsLB = OptionalInt.of(Arrays.stream(edges).map(x -> (x - 1) / 3).sum());
		}
		return localCrossingsLB.getAsInt();
	}

	public final int getBlockCount() {
		return blocks.size();
	}

	public final TIntSet getCuts() {
		if (cuts.isEmpty()) {
			final TIntSet s = new TIntHashSet();
			blocks.forEach(b -> {
				final int cut = b.getQuick(0);
				if (!forest.getNode(cut).isRoot() || forest.getForest().getOutDegree(cut) > 1) {
					s.add(cut);
				}
			});
			cuts = Optional.of(s);
		}
		return cuts.get();
	}

	private void toDottyCuts(final DottyFactory f) {
		f.beginBlock("shape=plain");
		getCuts().forEach(cut -> {
			f.addNode(cut);
			return true;
		});
		f.endBlock();
	}

	private void toDottyBlocks(final DottyFactory f) {
		final TIntSet s = getCuts();
		f.beginBlock("shape=box");
		int idx = 1;
		for (TIntArrayList block : blocks) {
			TIntIterator iter = block.iterator();
			final String bid = "b" + idx;
			while (iter.hasNext()) {
				final int node = iter.next();
				if (s.contains(node)) {
					f.addEdge(String.valueOf(node), bid);
				}
			}
			f.addAttributes(bid, "label=\"" + DepthFirstSearch.toString(block, " ") + "\"");
			idx++;
		}
		f.endBlock();
	}

	@Override
	public final String toDotty() {
		final DottyFactory f = new DottyFactory(false);
		f.beginGraph();
		toDottyCuts(f);
		toDottyBlocks(f);
		f.endGraph();
		return f.toString();
	}

	@Override
	public String toString() {
		return DepthFirstSearch.toString(blocks.stream(), "\n");
	}

}