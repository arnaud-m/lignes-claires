/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.bigraph;

import gnu.trove.iterator.TIntIterator;
import lignesclaires.specs.IEdgeConsumer;
import lignesclaires.specs.IGraphDimension;

public class UGraph extends AbstractGraph implements IGraphDimension {

	public UGraph(int nodeCount, int edgeCount) {
		super(AdjListUtil.createArrayOfTLists(nodeCount, 3 * edgeCount / nodeCount));
	}

	public boolean isDirected() {
		return false;
	}

	@Override
	public void addEdge(int i, int j) {
		adjLists[i].add(j);
		adjLists[j].add(i);
		edgeCount++;
	}

	@Override
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

	public static UGraph buildGraph1() {
		UGraph g = new UGraph(13, 11);

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
	public static UGraph buildGraph2() {
		UGraph g = new UGraph(17, 11);

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
	public static UGraph buildGraph3() {
		UGraph g = new UGraph(19, 11);

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

	public static UGraph buildGraph3Bis() {
		UGraph g = new UGraph(19, 11);

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
		// g.addEdge(7, 11); // Delete edge to Create a bridge
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

	public static UGraph buildGraph4() {
		UGraph g = new UGraph(13, 11);

		g.addEdge(1, 2);
		g.addEdge(2, 3);

		g.addEdge(1, 4);
		g.addEdge(4, 5);
		g.addEdge(5, 1);

		g.addEdge(1, 6);
		g.addEdge(6, 7);
		g.addEdge(7, 8);
		g.addEdge(8, 9);
		g.addEdge(9, 7);

		g.addEdge(10, 11);
		g.addEdge(11, 12);
		g.addEdge(12, 10);

		g.sort();
		return g;
	}

	public static void main(String[] args) {
		UGraph[] graphs = new UGraph[] { buildGraph3Bis() };
		DepthFirstSearch dfs = new DepthFirstSearch();
		for (UGraph g : graphs) {
			System.out.println(g);
			System.out.println(g.toDotty());

			ForestDFS f = dfs.execute(g);
//			System.out.println(f);
//
//			System.out.println(dfs);
//
			System.out.println(f.toDotty());
//
//			BlockDecomposition bdec = new BlockDecomposition();
//			BlockCutTree r = bdec.execute(f);
//			System.out.println(r);
//			System.out.println(r.toDotty());
//			System.out.println(r.getLocalCrossingsLB());
//			System.out.println(f.getOrderInducingBridges(4));

		}
	}
}