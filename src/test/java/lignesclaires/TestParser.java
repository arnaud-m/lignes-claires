/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires;

import java.util.Scanner;

import org.junit.Assert;
import org.junit.Test;

import lignesclaires.parser.BiGraphParser;
import lignesclaires.parser.InvalidGraphFormatException;
import lignesclaires.specs.IBipartiteGraph;

public class TestParser {

	private final BiGraphParser parser = new BiGraphParser();

	@Test
	public void TestValidBiGraph() throws InvalidGraphFormatException {
		final Scanner sc = new Scanner("p ocr 5 5 4\n" + "2 8\n" + "3 6\n" + "3 9\n" + "4 10\n");
		final IBipartiteGraph bigraph = parser.parse(sc);
		Assert.assertEquals(5, bigraph.getFreeCount());
		Assert.assertEquals(5, bigraph.getFixedCount());
		Assert.assertEquals(4, bigraph.getEdgeCount());
		Assert.assertArrayEquals(new int[] { 1 }, bigraph.getFreeNeighbors(2).toArray());
		Assert.assertArrayEquals(new int[] { 2 }, bigraph.getFixedNeighbors(1).toArray());
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
