/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.graph;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Stream;

import gnu.trove.impl.Constants;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public final class TListUtil {

	private TListUtil() {
		super();
	}

	public static TIntArrayList wrap(final int... values) {
		return TIntArrayList.wrap(values);
	}

	public static TIntArrayList sequence(final int begin, final int end, final int by) {
		if (begin < end) {
			if (by > 0) {
				TIntArrayList l = new TIntArrayList(((end - begin) / by) + 1);
				for (int i = begin; i < end; i += by) {
					l.add(i);
				}
				return l;
			} else {
				throw new IllegalArgumentException("");
			}
		} else if (begin > end) {
			if (by < 0) {
				TIntArrayList l = new TIntArrayList(((end - begin) / by) + 1);
				for (int i = begin; i > end; i -= -by) {
					l.add(i);
				}
				return l;
			} else {
				throw new IllegalArgumentException("");
			}
		} else {
			return new TIntArrayList();
		}
	}

	public static TIntArrayList[] createArrayOfTLists(final int n) {
		return createArrayOfTLists(n, Constants.DEFAULT_CAPACITY);

	}

	public static TIntArrayList[] createArrayOfTLists(final int n, final int capacity) {
		TIntArrayList[] lists = new TIntArrayList[n];
		for (int i = 0; i < lists.length; i++) {
			lists[i] = new TIntArrayList(capacity);
		}
		return lists;
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

	public static Integer[] permutate(final int n, final IntToDoubleFunction func) {
		final Integer[] indices = new Integer[n];
		final double[] values = new double[n];
		for (int i = 0; i < n; i++) {
			indices[i] = Integer.valueOf(i);
			values[i] = func.applyAsDouble(i);
		}
		Arrays.sort(indices, (Integer arg0, Integer arg1) -> Double.compare(values[arg0], values[arg1]));
		return indices;
	}

	public static <E> E[] permutate(final E[] objects, final IntToDoubleFunction func) {
		final int n = objects.length;
		final Integer[] indices = permutate(n, func);
		return Stream.of(indices).map(i -> objects[i]).toArray(m -> (E[]) Array.newInstance(objects[0].getClass(), m));
	}

	public static double getMedian(final TIntArrayList list) {
		if (list.isEmpty()) {
			return 0;
		}
		final int n = list.size();
		if (n % 2 == 0) {
			// For an even number of elements, average the middle two elements
			final int middleRight = n / 2;
			final int middleLeft = middleRight - 1;
			return (list.getQuick(middleLeft) + list.getQuick(middleRight)) / 2.0;
		} else {
			// For an odd number of elements, return the middle element
			return list.getQuick(n / 2);
		}
	}

	public static double getBarycenter(final TIntList list) {
		return list.isEmpty() ? 0 : 1.0 * list.sum() / list.size();
	}

}
