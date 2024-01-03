/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.bigraph;

import java.util.Formatter;

public final class CrossingCounts {

	private final int[][] counts;
	private int constant;

	public CrossingCounts(final int[][] crossingCounts, final int constant) {
		super();
		this.counts = crossingCounts;
		this.constant = constant;
	}

	public int getCrossingCount(final int i, final int j) {
		return counts[i][j];
	}

	public int getConstant() {
		return constant;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		try (final Formatter formatter = new Formatter(b)) {
			formatter.format("CrossingCounts [constant= %d, counts =%n", constant);
			for (int i = 0; i < counts.length; i++) {
				for (int j = 0; j < counts.length - 1; j++) {
					formatter.format("% 2d ", counts[i][j]);
				}
				formatter.format("% 2d%n", counts[i][counts.length - 1]);
			}
			formatter.format("]%n");
			return formatter.toString();
		}
	}

}