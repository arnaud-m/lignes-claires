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
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntIterator;
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

	public static TIntArrayList intersect(final TIntList l1, final TIntList l2) {
		TIntArrayList intersection = new TIntArrayList();
		if (!l1.isEmpty() && !l2.isEmpty()) {
			final TIntIterator it1 = l1.iterator();
			final TIntIterator it2 = l2.iterator();
			int v2 = it2.next();
			do {
				final int v1 = it1.next();
				while (v1 > v2 && it2.hasNext()) {
					v2 = it2.next();
				}
				if (v1 == v2) {
					intersection.add(v1);
				}

			} while (it1.hasNext());
		}
		return intersection;
	}

	public static int intersectSingloton(final TIntList l1, final TIntList l2) {
		TIntArrayList l3 = intersect(l1, l2);
		if (l3.size() != 1) {
			throw new IllegalArgumentException("Intersection cardinality " + l3.size());
		}
		return l3.getQuick(0);
	}

	public static int lazyIntersectSingloton(final TIntList l1, final TIntList l2) {
		if (!l1.isEmpty() && !l2.isEmpty()) {
			final TIntIterator it1 = l1.iterator();
			final TIntIterator it2 = l2.iterator();
			int v2 = it2.next();
			do {
				final int v1 = it1.next();
				while (v1 > v2 && it2.hasNext()) {
					v2 = it2.next();
				}
				if (v1 == v2) {
					return v1;
				}

			} while (it1.hasNext());
		}
		throw new IllegalArgumentException("Empty intersection");
	}

	public static void difference(final TIntList l1, final TIntList l2) {
		if (!l1.isEmpty() && !l2.isEmpty()) {
			final TIntIterator it1 = l1.iterator();
			final TIntIterator it2 = l2.iterator();
			int v2 = it2.next();
			do {
				final int v1 = it1.next();
				while (v1 > v2 && it2.hasNext()) {
					v2 = it2.next();
				}
				if (v1 == v2) {
					it1.remove();
				}
			} while (it1.hasNext());
		}
	}

	public static boolean isEqual(final TIntList l1, final TIntList l2) {
		if (l1.size() == l2.size()) {
			final TIntIterator it1 = l1.iterator();
			final TIntIterator it2 = l2.iterator();
			while (it1.hasNext()) {
				if (it1.next() != it2.next()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

}
