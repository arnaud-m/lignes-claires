/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires;

import static lignesclaires.graph.TListUtil.getBarycenter;
import static lignesclaires.graph.TListUtil.getCrossingCount;
import static lignesclaires.graph.TListUtil.getMedian;
import static lignesclaires.graph.TListUtil.sequence;
import static lignesclaires.graph.TListUtil.wrap;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import gnu.trove.list.array.TIntArrayList;

public class TestTListUtil {

	@Test
	public void testIncreasingSequences() {
		assertEquals(wrap(0, 1, 2, 3), sequence(0, 4, 1));
		assertEquals(wrap(1, 3, 5, 7, 9), sequence(1, 10, 2));
		assertEquals(wrap(1, 3, 5, 7), sequence(1, 9, 2));
	}

	@Test
	public void testDecreasingSequences() {
		assertEquals(wrap(4, 3, 2, 1), sequence(4, 0, -1));
		assertEquals(wrap(10, 8, 6, 4, 2), sequence(10, 0, -2));
		assertEquals(wrap(9, 7, 5, 3, 1), sequence(9, 0, -2));
	}

	@Test
	public void testEmpySequences() {
		final TIntArrayList l = new TIntArrayList();
		assertEquals(l, sequence(4, 4, -1));
		assertEquals(l, sequence(10, 10, 0));
		assertEquals(l, sequence(9, 9, 1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidSequence1() {
		sequence(4, 5, -1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidSequence2() {
		sequence(0, -1, 1);
	}

	@Test
	public void testCrossingCount00() {
		final TIntArrayList l1 = wrap();
		final TIntArrayList l2 = wrap(1);
		assertEquals(0, getCrossingCount(l1, l1));
		assertEquals(0, getCrossingCount(l1, l2));
		assertEquals(0, getCrossingCount(l2, l2));
	}

	@Test
	public void testCrossingCount10() {
		final TIntArrayList l1 = wrap(10);
		final TIntArrayList l2 = wrap(5);
		assertEquals(1, getCrossingCount(l1, l2));
		assertEquals(0, getCrossingCount(l2, l1));

	}

	@Test
	public void testCrossingCount11() {
		final TIntArrayList l1 = wrap(1, 2);
		final TIntArrayList l2 = wrap(1, 2);
		assertEquals(1, getCrossingCount(l1, l2));
		assertEquals(1, getCrossingCount(l1, l2));
	}

	@Test
	public void testCrossingCount21a() {
		TIntArrayList a = wrap(1, 2);
		TIntArrayList b = wrap(1, 3);
		assertEquals(2, getCrossingCount(b, a));
		assertEquals(1, getCrossingCount(a, b));

		a = wrap(1, 3);
		b = wrap(2, 3);
		assertEquals(2, getCrossingCount(b, a));
		assertEquals(1, getCrossingCount(a, b));
	}

	@Test
	public void testCrossingCount21b() {
		TIntArrayList a = wrap(1, 2, 4);
		TIntArrayList b = wrap(3);
		assertEquals(2, getCrossingCount(b, a));
		assertEquals(1, getCrossingCount(a, b));

		a = wrap(2);
		b = wrap(1, 3, 4);
		assertEquals(2, getCrossingCount(b, a));
		assertEquals(1, getCrossingCount(a, b));
	}

	@Test
	public void testMedians() {
		final int n = 50;
		TIntArrayList l = new TIntArrayList(n);
		assertEquals(0, getMedian(l), 0);
		for (int i = 0; i < n; i++) {
			l.add(i);
			assertEquals(0.5 * i, getMedian(l), 0);

		}
	}

	@Test
	public void testBarycenters() {
		final int n = 50;
		TIntArrayList l = new TIntArrayList(n);
		assertEquals(0, getMedian(l), 0);
		double sum = 0;
		for (int i = 0; i < n; i++) {
			l.add(i);
			sum += i;
			assertEquals(sum / (i + 1), getBarycenter(l), 0);

		}
	}

}
