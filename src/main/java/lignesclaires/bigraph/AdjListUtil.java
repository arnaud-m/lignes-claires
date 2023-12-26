/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.bigraph;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public final class AdjListUtil {

	private AdjListUtil() {
		super();
	}

	public static int getCrossingCount(final TIntArrayList left, final TIntArrayList right) {
		int count = 0;
		final int nl = left.size();
		final int nr = right.size();
		int l = 0;
		int r = 0;
		while (l < nl && r < nr) {
			final int lf = left.getQuick(l);
			final int rf = right.getQuick(r);
			if (lf <= rf) {
				l++;
			} else {
				r++;
				count += nl - l;
			}
		}
		return count;
	}

	public static <E> E[] permutate(final E[] vars, final ToDoubleFunction<TIntArrayList> func,
			final TIntArrayList[] lists, final int offset) {
		final int n = vars.length;
		final Integer[] indices = new Integer[n];
		final double[] values = new double[n];
		for (int i = 0; i < n; i++) {
			indices[i] = Integer.valueOf(i);
			values[i] = func.applyAsDouble(lists[offset + i]);
		}
		Arrays.sort(indices, (Integer arg0, Integer arg1) -> Double.compare(values[arg0], values[arg1]));
		return Stream.of(indices).map(i -> vars[i]).toArray(m -> (E[]) Array.newInstance(vars[0].getClass(), m));
	}

	public static final double getMedian(final TIntArrayList adjList) {
		if (adjList.isEmpty()) {
			return 0;
		}
		final int n = adjList.size();
		if (n % 2 == 0) {
			// For an even number of elements, average the middle two elements
			final int middleRight = n / 2;
			final int middleLeft = middleRight - 1;
			return (adjList.getQuick(middleLeft) + adjList.getQuick(middleRight)) / 2.0;
		} else {
			// For an odd number of elements, return the middle element
			return adjList.getQuick(n / 2);
		}
	}

	public static final double getBarycenter(final TIntList adjList) {
		return adjList.isEmpty() ? 0 : 1.0 * adjList.sum() / adjList.size();
	}

	public static final boolean isEqual(TIntList adjList1, TIntList adjList2) {
		if (adjList1.size() == adjList2.size()) {
			final TIntIterator it1 = adjList1.iterator();
			final TIntIterator it2 = adjList2.iterator();
			while (it1.hasNext()) {
				if (it1.next() != it2.next())
					return false;
			}
			return true;
		}
		return false;
	}

}
