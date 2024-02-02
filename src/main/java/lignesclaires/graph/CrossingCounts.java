/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.graph;

import java.util.Arrays;

import org.chocosolver.solver.constraints.extension.Tuples;

import lignesclaires.choco.AssignmentRowBuilder;

class IntFrequency {

	private int[] frequencies;

	public IntFrequency() {
		super();
		frequencies = new int[1];
	}

	public void add(int i) {
		if (i >= frequencies.length) {
			frequencies = Arrays.copyOf(frequencies, i + 1);
		}
		frequencies[i]++;
	}

	@Override
	public String toString() {
		return DepthFirstSearch.toString(frequencies, " ");
	}
}

class CrossingCountPatterns {

	private int[][] matrix;
	private int m;

	public CrossingCountPatterns() {
		super();
		matrix = new int[0][0];
		m = 0;
	}

	private void addColumns(int j) {
		if (j >= m) {
			m = j + 1;
			for (int k = 0; k < matrix.length; k++) {
				matrix[k] = Arrays.copyOf(matrix[k], m);
			}
		}
	}

	private void addRows(int i) {
		final int n = matrix.length;
		if (i >= n) {
			matrix = Arrays.copyOf(matrix, i + 1);
			for (int k = n; k <= i; k++) {
				matrix[k] = new int[m];
			}
		}
	}

	public void addPattern(int i, int j) {
		addColumns(j);
		addRows(i);
		matrix[i][j]++;
	}

	@Override
	public String toString() {
		return DepthFirstSearch.toString(matrix, "% 3d");
	}

}

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

	public final IntFrequency getDistribution() {
		IntFrequency frequency = new IntFrequency();
		for (int i = 0; i < counts.length; i++) {
			for (int j = 0; j < counts.length; j++) {
				frequency.add(counts[i][j]);
			}
		}
		return frequency;
	}

	public final CrossingCountPatterns getPatterns() {
		CrossingCountPatterns patterns = new CrossingCountPatterns();
		for (int i = 0; i < counts.length; i++) {
			for (int j = i + 1; j < counts.length; j++) {
				if (counts[i][j] <= counts[j][i]) {
					patterns.addPattern(counts[i][j], counts[j][i]);
				} else {
					patterns.addPattern(counts[j][i], counts[i][j]);
				}
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

	public AssignmentRowBuilder getHRowBuilder(int i) {
		return new AssignmentRowBuilder(counts[i]);
	}

	@Override
	public String toString() {
		return DepthFirstSearch.toString(counts, "% 2d");
	}

}