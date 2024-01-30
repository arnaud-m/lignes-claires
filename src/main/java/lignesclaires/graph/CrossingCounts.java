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
import lignesclaires.choco.AssignmentRowBuilder;

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

	private Point getKey(final int i, final int j) {
		return counts[i][j] <= counts[j][i] ? new Point(counts[i][j], counts[j][i])
				: new Point(counts[j][i], counts[i][j]);
	}

	public final TObjectIntHashMap<Point> getPatternDistribution() {
		TObjectIntHashMap<Point> patterns = new TObjectIntHashMap<>();
		for (int i = 0; i < counts.length; i++) {
			for (int j = i + 1; j < counts.length; j++) {
				patterns.adjustOrPutValue(getKey(i, j), 1, 1);
			}
		}
		return patterns;
	}

	public final String getDimacsPatterns() {
		final StringBuilder b = new StringBuilder();
		getPatternDistribution().forEachEntry((p, v) -> {
			b.append("c PATTERN ").append(p.x).append(' ').append(p.y).append(' ').append(v).append('\n');
			return true;
		});
		if (b.length() > 0) {
			b.deleteCharAt(b.length() - 1);
		}
		return b.toString();
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

	public AssignmentRowBuilder getHRowBuilder(int i) {
		return new AssignmentRowBuilder(counts[i]);
	}

	@Override
	public String toString() {
		try (final Formatter formatter = new Formatter(new StringBuilder())) {
			for (int i = 0; i < counts.length; i++) {
				for (int j = 0; j < counts.length - 1; j++) {
					formatter.format("% 2d ", counts[i][j]);
				}
				formatter.format("% 2d%n", counts[i][counts.length - 1]);
			}
			return formatter.toString();
		}
	}

}