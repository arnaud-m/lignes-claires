/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.solver;

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lignesclaires.specs.IBipartiteGraphDimension;

public class OCSolution {

	public static final Logger LOGGER = Logger.getLogger(OCSolution.class.getName());

	private final IBipartiteGraphDimension graph;

	private final int[] permutation;

	public OCSolution(IBipartiteGraphDimension graph, int[] permutation) {
		super();
		this.graph = graph;
		this.permutation = permutation;
	}

	@Override
	public String toString() {
		return "v " + toString(IntStream.of(permutation), " ");
	}

	public String toOutputString() {
		final int n = graph.getFixedCount() + 1;
		return toString(IntStream.of(permutation).map(x -> x + n), "\n");
	}

	public static final String toString(IntStream intstream, CharSequence delimiter) {
		return intstream.mapToObj(Integer::toString).collect(Collectors.joining(delimiter));
	}

}