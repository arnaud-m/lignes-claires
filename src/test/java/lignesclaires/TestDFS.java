/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Scanner;

import org.junit.Test;

import gnu.trove.set.hash.TIntHashSet;
import lignesclaires.bigraph.BlockCutTree;
import lignesclaires.bigraph.BlockDecomposition;
import lignesclaires.bigraph.DepthFirstSearch;
import lignesclaires.bigraph.ForestDFS;
import lignesclaires.bigraph.NodeDFS;
import lignesclaires.parser.EdgeListParser;
import lignesclaires.parser.InvalidGraphFormatException;
import lignesclaires.specs.IGenericGraph;
import lignesclaires.specs.IGraphParser;

public class TestDFS {

	private IGenericGraph g;

	private final DepthFirstSearch dfs = new DepthFirstSearch();
	private ForestDFS f;

	private final BlockDecomposition bdec = new BlockDecomposition();
	private BlockCutTree d;

	private final IGenericGraph getResourceGraph(final String resourcePath) throws InvalidGraphFormatException {
		final IGraphParser<IGenericGraph> parser = new EdgeListParser(false);
		final InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath);
		return parser.parse(new Scanner(in));
	}

	private void assertNodes(NodeDFS[] nodes, int... expected) {
		assertArrayEquals(expected, NodeDFS.toNodes(nodes));
	}

	private void assertCuts(int... expected) {
		assertEquals(new TIntHashSet(expected), d.getCuts());
	}

	private void setupGraph(String resourcePath, int nodeCount, int edgeCount) throws InvalidGraphFormatException {
		g = getResourceGraph(resourcePath);
		assertEquals(nodeCount, g.getNodeCount());
		assertEquals(edgeCount, g.getEdgeCount());
		assertNotNull(g.toDotty());
	}

	private void execDFS() {
		f = dfs.execute(g);
		assertNotNull(f.toDotty());
	}

	private void execBDEC() {
		d = bdec.execute(f);
		assertNotNull(g.toDotty());
	}

	@Test
	public void testGraph1() throws InvalidGraphFormatException {
		setupGraph("graphs/graph1.edgelist", 13, 13);
		execDFS();
		assertNodes(f.getRoots(), 0, 1);

		assertNodes(f.getPreorder(), 0, 1, 7, 2, 8, 3, 9, 10, 4, 5, 11, 6, 12);
		assertNodes(f.getPostorder(), 0, 9, 4, 12, 6, 11, 5, 10, 3, 8, 2, 7, 1);

		execBDEC();
		assertEquals(7, d.getBlockCount());
		assertCuts(8, 3, 10, 5);
	}

	@Test
	public void testGraph2() throws InvalidGraphFormatException {
		setupGraph("graphs/graph2.edgelist", 17, 15);
		execDFS();
		assertNodes(f.getRoots(), 0, 1, 3, 5, 6);

		assertNodes(f.getPreorder(), 0, 1, 2, 3, 4, 7, 8, 5, 9, 10, 11, 12, 16, 15, 14, 13, 6);
		assertNodes(f.getPostorder(), 0, 2, 1, 4, 8, 7, 3, 15, 16, 12, 11, 13, 14, 10, 9, 5, 6);

		execBDEC();
		assertEquals(8, d.getBlockCount());
		assertCuts(3, 7, 10, 11, 14);
	}

	@Test
	public void testGraph3() throws InvalidGraphFormatException {
		setupGraph("graphs/graph3.edgelist", 19, 26);
		execDFS();
		assertNodes(f.getRoots(), 0, 1);

		assertNodes(f.getPreorder(), 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 16, 17, 18, 12, 13, 14, 15);
		assertNodes(f.getPostorder(), 0, 4, 3, 11, 16, 18, 17, 10, 9, 14, 15, 13, 12, 8, 7, 6, 5, 2, 1);

		execBDEC();
		assertEquals(7, d.getBlockCount());
		assertCuts(2, 7, 8, 10);
	}

	@Test
	public void testGraph3bis() throws InvalidGraphFormatException {
		setupGraph("graphs/graph3bis.edgelist", 19, 25);
		execDFS();
		assertNodes(f.getRoots(), 0, 1);

		assertNodes(f.getPreorder(), 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 16, 17, 18, 12, 13, 14, 15);
		assertNodes(f.getPostorder(), 0, 4, 3, 11, 16, 18, 17, 10, 9, 14, 15, 13, 12, 8, 7, 6, 5, 2, 1);

		execBDEC();
		assertEquals(8, d.getBlockCount());
		assertCuts(2, 7, 8, 10);
	}

	@Test
	public void testGraph4() throws InvalidGraphFormatException {
		setupGraph("graphs/graph4.edgelist", 13, 13);
		execDFS();
		assertNodes(f.getRoots(), 0, 1, 10);

		assertNodes(f.getPreorder(), 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
		assertNodes(f.getPostorder(), 0, 3, 2, 5, 4, 9, 8, 7, 6, 1, 12, 11, 10);

		execBDEC();
		assertEquals(7, d.getBlockCount());
		assertCuts(1, 2, 6, 7);
	}

}
