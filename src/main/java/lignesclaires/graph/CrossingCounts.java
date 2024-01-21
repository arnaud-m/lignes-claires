/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.graph;

import java.awt.Point;
import java.util.Formatter;

import org.chocosolver.solver.constraints.extension.Tuples;

import gnu.trove.map.hash.TObjectIntHashMap;

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

	public TObjectIntHashMap<Point> getPatternDistribution() {
		TObjectIntHashMap<Point> patterns = new TObjectIntHashMap<>();
		for (int i = 0; i < counts.length; i++) {
			for (int j = i + 1; j < counts.length; j++) {
				final Point p = counts[i][j] <= counts[j][i] ? new Point(counts[i][j], counts[j][i])
						: new Point(counts[j][i], counts[i][j]);
				patterns.adjustOrPutValue(p, 1, 1);

			}
		}
		return patterns;
	}

	public Tuples getTuplesLO2() {
		Tuples tuples = new Tuples();
		for (int i = 0; i < counts.length; i++) {
			for (int j = i + 1; j < counts.length; j++) {
				if (counts[i][j] <= counts[j][i]) {
					tuples.add(i, j);
				} else {
					tuples.add(j, i);
				}
			}
		}
		return tuples;
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