/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class ToStringUtil {

	private ToStringUtil() {
		super();
	}

	public static <E> String toString(Stream<E> stream, CharSequence delimiter) {
		return stream.map(Object::toString).collect(Collectors.joining(delimiter));
	}

	public static String toString(int[] values, CharSequence delimiter) {
		return IntStream.of(values).mapToObj(Integer::toString).collect(Collectors.joining(delimiter));
	}

	public static String toString(int[] values, String format, CharSequence delimiter) {
		return IntStream.of(values).mapToObj(v -> String.format(format, v)).collect(Collectors.joining(delimiter));
	}

	public static String toString(int[][] values, String format) {
		return toString(Stream.of(values).map(t -> ToStringUtil.toString(t, format, " ")), "\n");
	}

	public static String toString(Object[] o, CharSequence delimiter) {
		return toString(Stream.of(o), delimiter);
	}

}