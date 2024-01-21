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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Scanner;

import org.junit.Test;

import gnu.trove.list.TIntList;
import lignesclaires.graph.TListUtil;
import lignesclaires.parser.EdgeListParser;
import lignesclaires.parser.InvalidGraphFormatException;
import lignesclaires.parser.PaceInputParser;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IGraph;
import lignesclaires.specs.IGraphParser;

public class TestParser {

	private static class GraphParserTest<E extends IGraph> {

		private final IGraphParser<E> parser;
		private Scanner sc;
		protected E graph;

		public GraphParserTest(IGraphParser<E> parser) {
			super();
			this.parser = parser;
		}

		public void assertNeighbors(int i, int... neighbors) {
			assertEquals(neighbors.length, graph.getOutDegree(i));
			assertEquals(graph.getNeighbors(i), (TIntList) TListUtil.wrap(neighbors));
		}

		public void parse(String graphString) throws InvalidGraphFormatException {
			sc = new Scanner(graphString);
			graph = parser.parse(sc);
		}

		public void assertGraph(int nodes, int edges) {
			assertEquals(nodes, graph.getNodeCount());
			assertEquals(edges, graph.getEdgeCount());
			assertNotNull(graph.toString());
			assertNotNull(graph.toDotty());

		}
	}

	private static class BiGraphParserTest extends GraphParserTest<IBipartiteGraph> {

		public BiGraphParserTest() {
			super(new PaceInputParser());
		}

		private void assertBiGraph(int fixed, int free, int edges) {
			assertEquals(fixed, graph.getFreeCount());
			assertEquals(free, graph.getFixedCount());
			assertFalse(graph.isDirected());
			assertGraph(fixed + free + 1, edges);
		}

	}

	GraphParserTest<IGraph> pu = new GraphParserTest<IGraph>(new EdgeListParser(false));
	GraphParserTest<IGraph> pd = new GraphParserTest<IGraph>(new EdgeListParser(true));

	BiGraphParserTest pb = new BiGraphParserTest();

	@Test
	public void testSkipComments() throws InvalidGraphFormatException {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < 5; i++) {
			final Scanner sc = new Scanner(b.toString() + "OK\n");
			PaceInputParser.skipComments(sc);
			assertTrue(sc.hasNext());
			assertEquals("OK", sc.next());
			b.append("c comment ").append(i).append('\n');
		}
	}

	@Test
	public void TestEmptyGraph() throws InvalidGraphFormatException {
		pu.parse("c comment 1\n" + "5 0\n");
		pu.assertGraph(5, 0);

		pu.parse("c comment 1\n" + "5 0\n");
		pu.assertGraph(5, 0);
	}

	@Test
	public void testEmptyBiGraph() throws InvalidGraphFormatException {
		pb.parse("c comment 1\n" + "p ocr 5 5 0\n");
		pb.assertBiGraph(5, 5, 0);
	}

	private static void testValidGraph1(GraphParserTest<IGraph> p) throws InvalidGraphFormatException {
		p.parse("11 4\n" + "2 8\n" + "3 6\n" + "3 9\n" + "4 10\n");
		p.assertGraph(11, 4);
		p.assertNeighbors(1);
		p.assertNeighbors(2, 8);
		p.assertNeighbors(3, 6, 9);
		p.assertNeighbors(4, 10);
		p.assertNeighbors(5);

	}

	@Test
	public void testValidGraph1() throws InvalidGraphFormatException {
		testValidGraph1(pu);
		testValidGraph1(pd);
	}

	@Test
	public void testValidBiGraph1() throws InvalidGraphFormatException {
		pb.parse("p ocr 5 5 4\n" + "2 8\n" + "3 6\n" + "3 9\n" + "4 10\n");
		pb.assertBiGraph(5, 5, 4);
		pb.assertNeighbors(1);
		pb.assertNeighbors(2, 8);
		pb.assertNeighbors(3, 6, 9);
		pb.assertNeighbors(4, 10);
		pb.assertNeighbors(5);
	}

	@Test
	public void testValidBiGraph2() throws InvalidGraphFormatException {
		pb.parse("c comment 1\n" + "c comment 2\n" + "p ocr 5 5 7\n" + "2 8\n" + "2 7\n" + "3 9\n" + "3 10\n" + "4 10\n"
				+ "3 6\n" + "4 9\n");
		pb.assertBiGraph(5, 5, 7);
		pb.assertNeighbors(1);
		pb.assertNeighbors(2, 7, 8);
		pb.assertNeighbors(3, 6, 9, 10);
		pb.assertNeighbors(4, 9, 10);
		pb.assertNeighbors(5);
	}

	private void testValidGraph2(GraphParserTest<IGraph> p) throws InvalidGraphFormatException {
		p.parse("c comment 1\n" + "c comment 2\n" + "11 7\n" + "2 8\n" + "2 7\n" + "3 9\n" + "3 10\n" + "4 10\n"
				+ "3 6\n" + "4 9\n");
		p.assertGraph(11, 7);
		p.assertNeighbors(1);
		p.assertNeighbors(2, 7, 8);
		p.assertNeighbors(3, 6, 9, 10);
		p.assertNeighbors(4, 9, 10);
		p.assertNeighbors(5);
	}

	@Test
	public void testValidGraph2() throws InvalidGraphFormatException {
		testValidGraph2(pu);
		testValidGraph2(pd);
	}

	@Test(expected = InvalidGraphFormatException.class)
	public void testInvalidBiGraph1() throws InvalidGraphFormatException {
		pb.parse("p ocr 5 5 5\n" + "2 8\n" + "3 6\n" + "3 9\n" + "4 10\n");
	}

	@Test(expected = InvalidGraphFormatException.class)
	public void testInvalidGraph1() throws InvalidGraphFormatException {
		pu.parse("10 4\n" + "2 8\n" + "3 6\n" + "3 9\n" + "4 10\n");
	}

	@Test(expected = InvalidGraphFormatException.class)
	public void testInvalidBiGraph2() throws InvalidGraphFormatException {
		pb.parse("p ocr 5 5 4\n" + "2 8\n" + "3 6\n" + "3 9\n" + "4 20\n");
	}

	@Test(expected = InvalidGraphFormatException.class)
	public void testInvalidGraph2() throws InvalidGraphFormatException {
		pd.parse("10 4\n" + "2 8\n" + "3 6\n" + "3 9\n" + "-1 9\n");
	}

}
