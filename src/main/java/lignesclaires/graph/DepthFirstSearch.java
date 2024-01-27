/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.graph;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;

/**
 * https://en.wikipedia.org/wiki/Bridge_(graph_theory)
 * 
 * Tarjan's bridge-finding algorithm
 * 
 */
public class DepthFirstSearch {

	public static <E> String toString(Stream<E> stream, CharSequence delimiter) {
		return stream.map(Object::toString).collect(Collectors.joining(delimiter));
	}

	public static final String toString(int[] values, CharSequence delimiter) {
		return toString(IntStream.of(values), delimiter);
	}

	public static final String toString(IntStream stream, CharSequence delimiter) {
		return stream.mapToObj(Integer::toString).collect(Collectors.joining(delimiter));
	}

	public static String toString(TIntCollection collection, CharSequence delimiter) {
		final StringBuilder b = new StringBuilder();
		for (TIntIterator it = collection.iterator(); it.hasNext();) {
			b.append(it.next()).append(delimiter);
		}
		b.delete(b.length() - delimiter.length(), b.length());
		return b.toString();
	}

	public static String toString(Object[] o, CharSequence delimiter) {
		return toString(Stream.of(o), delimiter);
	}

}