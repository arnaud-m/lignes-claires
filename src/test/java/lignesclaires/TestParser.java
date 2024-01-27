/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.StringReader;

import org.jgrapht.nio.ImportException;
import org.junit.Test;

import lignesclaires.parser.PaceInputParser;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IGraph;
import lignesclaires.specs.IGraphParser;

public class TestParser {

	private static class GraphParserTest<E extends IGraph> {

		private final IGraphParser<E> parser;
		protected E graph;

		public GraphParserTest(IGraphParser<E> parser) {
			super();
			this.parser = parser;
		}

		public void assertNeighbors(int i, int... neighbors) {
			// TODO assertEquals(neighbors.length, graph.getOutDegree(i));
			// TODO assertEquals(graph.getNeighbors(i), (TIntList)
			// TListUtil.wrap(neighbors));
		}

		public void parse(String graphString) throws ImportException, FileNotFoundException {
			graph = parser.parse(new StringReader(graphString));
		}

		public void assertGraph(int nodes, int edges) {
			assertEquals(nodes, graph.getNodeCount());
			assertEquals(edges, graph.getEdgeCount());
			assertNotNull(graph.toString());

		}
	}

	private static class BiGraphParserTest extends GraphParserTest<IBipartiteGraph> {

		public BiGraphParserTest() {
			super(new PaceInputParser());
		}

		private void assertBiGraph(int fixed, int free, int edges) {
			assertEquals(fixed, graph.getFreeCount());
			assertEquals(free, graph.getFixedCount());
			assertGraph(fixed + free, edges);
		}

	}

	BiGraphParserTest pb = new BiGraphParserTest();

	@Test
	public void testEmptyBiGraph() throws ImportException, FileNotFoundException {
		pb.parse("c comment 1\n" + "p ocr 5 5 0\n");
		pb.assertBiGraph(5, 5, 0);
	}

	@Test
	public void testValidBiGraph1() throws ImportException, FileNotFoundException {
		pb.parse("p ocr 5 5 4\n" + "2 8\n" + "3 6\n" + "3 9\n" + "4 10\n");
		pb.assertBiGraph(5, 5, 4);
		pb.assertNeighbors(1);
		pb.assertNeighbors(2, 8);
		pb.assertNeighbors(3, 6, 9);
		pb.assertNeighbors(4, 10);
		pb.assertNeighbors(5);
	}

	@Test
	public void testValidBiGraph2() throws ImportException, FileNotFoundException {
		pb.parse("c comment 1\n" + "c comment 2\n" + "p ocr 5 5 7\n" + "2 8\n" + "2 7\n" + "3 9\n" + "3 10\n" + "4 10\n"
				+ "3 6\n" + "4 9\n");
		pb.assertBiGraph(5, 5, 7);
		pb.assertNeighbors(1);
		pb.assertNeighbors(2, 7, 8);
		pb.assertNeighbors(3, 6, 9, 10);
		pb.assertNeighbors(4, 9, 10);
		pb.assertNeighbors(5);
	}

	@Test(expected = ImportException.class)
	public void testInvalidBiGraph2() throws ImportException, FileNotFoundException {
		pb.parse("p ocr 5 5 4\n" + "2 8\n" + "3 6\n" + "3 9\n" + "4 20\n");
	}

}
