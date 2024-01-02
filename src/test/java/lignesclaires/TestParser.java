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
import static org.junit.Assert.assertTrue;

import java.util.Scanner;

import org.junit.Assert;
import org.junit.Test;

import gnu.trove.list.TIntList;
import lignesclaires.bigraph.TListUtil;
import lignesclaires.parser.InvalidGraphFormatException;
import lignesclaires.parser.PaceInputParser;
import lignesclaires.specs.IBipartiteGraph;

public class TestParser {

	private final PaceInputParser parser = new PaceInputParser();

	private IBipartiteGraph bigraph;

	public void assertNeighbors(int i, int... neighbors) {
		assertEquals(bigraph.getNeighbors(1), (TIntList) TListUtil.wrap());

	}

	public void assertDimensions(int fixed, int free, int edges) {
		Assert.assertEquals(fixed, bigraph.getFreeCount());
		Assert.assertEquals(free, bigraph.getFixedCount());
		Assert.assertEquals(edges, bigraph.getEdgeCount());
	}

	@Test
	public void TestSkipComments() throws InvalidGraphFormatException {
		final Scanner sc = new Scanner("c comment\nc comment 1\nc comment 2\nOK\n");
		PaceInputParser.skipComments(sc);
		assertTrue(sc.hasNext());
		assertEquals("OK", sc.next());

	}

	@Test
	public void TestEmptyBiGraph() throws InvalidGraphFormatException {
		final Scanner sc = new Scanner("c comment 1\n" + "p ocr 5 5 0\n");
		bigraph = parser.parse(sc);
		assertDimensions(5, 5, 0);
	}

	@Test
	public void TestValidBiGraph1() throws InvalidGraphFormatException {
		final Scanner sc = new Scanner("p ocr 5 5 4\n" + "2 8\n" + "3 6\n" + "3 9\n" + "4 10\n");
		bigraph = parser.parse(sc);
		assertDimensions(5, 5, 4);
		assertNeighbors(1);
		assertNeighbors(2, 8);
		assertNeighbors(3, 6, 9);
		assertNeighbors(4, 10);
		assertNeighbors(5);
	}

	@Test
	public void TestValidBiGraph2() throws InvalidGraphFormatException {
		final Scanner sc = new Scanner("c comment 1\n" + "c comment 2\n" + "p ocr 5 5 7\n" + "2 8\n" + "2 7\n" + "3 9\n"
				+ "3 10\n" + "4 10\n" + "3 6\n" + "4 9\n");
		bigraph = parser.parse(sc);
		assertDimensions(5, 5, 7);
		assertNeighbors(1);
		assertNeighbors(2, 7, 8);
		assertNeighbors(3, 6, 9, 10);
		assertNeighbors(4, 9, 10);
		assertNeighbors(5);
	}

	@Test(expected = InvalidGraphFormatException.class)
	public void TestInvalidBiGraph1() throws InvalidGraphFormatException {
		final Scanner sc = new Scanner("p ocr 5 5 5\n" + "2 8\n" + "3 6\n" + "3 9\n" + "4 10\n");
		parser.parse(sc);
	}

	@Test(expected = InvalidGraphFormatException.class)
	public void TestInvalidBiGraph2() throws InvalidGraphFormatException {
		final Scanner sc = new Scanner("p ocr 5 5 4\n" + "2 8\n" + "3 6\n" + "3 9\n" + "4 20\n");
		parser.parse(sc);
	}
}
