/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.choco;

import java.util.Arrays;
import java.util.Formatter;

import org.chocosolver.solver.variables.IntVar;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import lignesclaires.graph.CrossingCounts;
import lignesclaires.specs.IBipartiteGraph;

public class AssignmentBuilder {

	private int[][] matrix;

	private Integer[] indices;

	private AssignmentRowBuilder[] builders;

	private TIntArrayList[] eventsLB;
	private TIntArrayList[] eventsUB;

	public AssignmentBuilder(IBipartiteGraph bigraph) {
		super();
		final int n = bigraph.getFreeCount();
		indices = new Integer[n];
		builders = new AssignmentRowBuilder[n];
		matrix = new int[n][n];
		eventsLB = new TIntArrayList[n];
		eventsUB = new TIntArrayList[n];
		final CrossingCounts counts = bigraph.getReducedCrossingCounts();
		for (int i = 0; i < n; i++) {
			indices[i] = i;
			builders[i] = counts.getHRowBuilder(i);
			eventsLB[i] = new TIntArrayList();
			eventsUB[i] = new TIntArrayList();
		}
	}

	public void setUp() {
		final int n = matrix.length;
		final int costUB = n * n * n;
		for (int i = 0; i < n; i++) {
			Arrays.fill(matrix[i], costUB);
			builders[i].setUp();
			eventsLB[i].clear();
			eventsUB[i].clear();
		}
	}

	public void setUp(IntVar[] positions) {
		setUp();
		for (int i = 0; i < positions.length; i++) {
			eventsLB[positions[i].getLB()].add(i);
			eventsUB[positions[i].getUB()].add(i);
		}
	}

	private void updateBuilders(int col) {
		final TIntIterator it = eventsUB[col].iterator();
		while (it.hasNext()) {
			int k = it.next();
			for (int j = 0; j < k; j++) {
				builders[j].remove(k);
			}
			for (int j = k + 1; j < builders.length; j++) {
				builders[j].remove(k);
			}
		}
	}

	private void nextBuilders() {
		for (AssignmentRowBuilder builder : builders) {
			builder.next();
		}
	}

	private void updateMatrix(int col, TIntList rows) {
		TIntIterator it = rows.iterator();
		while (it.hasNext()) {
			int row = it.next();
			matrix[row][col] = builders[row].getCrossingCount();
		}
	}

	public void buildCostMatrix(IntVar[] positions) {
		setUp(positions);
//		for (int i = 0; i < positions.length; i++) {
//			System.out.println(builders[i]);
//		}
		final TIntLinkedList rows = new TIntLinkedList();
		for (int i = 0; i < positions.length; i++) {
			updateBuilders(i);
			rows.addAll(eventsLB[i]);
			updateMatrix(i, rows);
			rows.removeAll(eventsUB[i]);
			nextBuilders();
		}
	}

	public final int[][] getCostMatrix() {
		return matrix;
	}

	@Override
	public String toString() {
		try (final Formatter formatter = new Formatter(new StringBuilder())) {
			formatter.format("AssignmentBuilder [%n");
			final int n = matrix.length;
			final int costUB = n * n * n;
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (matrix[i][j] < costUB) {
						formatter.format("% 2d ", matrix[i][j]);
					} else {
						formatter.format("-- ");
					}
				}
				formatter.format("%n");
			}
			formatter.format("]%n");
			return formatter.toString();
		}
	}
}
