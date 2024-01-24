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
import java.util.Comparator;

public class AssignmentRowBuilder {

	int[] indices;
	int[] svalues;
	int[] next;
	int[] prev;

	private int crossingCount;

	private int tail;

	public AssignmentRowBuilder(int[] values) {
		super();
		final int n = values.length;
		indices = new int[n];
		svalues = new int[n + 1];
		next = new int[n + 1];
		prev = new int[n + 1];

		// Create an array of indices
		Integer[] inds = new Integer[n];
		for (int i = 0; i < n; i++) {
			inds[i] = i;
		}
		// Sort the indices based on the values in the original array
		Arrays.sort(inds, Comparator.comparingInt(index -> values[index]));

		for (int i = 0; i < n; i++) {
			indices[inds[i]] = i;
			svalues[i] = values[inds[i]];
		}
		svalues[n] = n * n * n;
	}

	public void setUp() {
		final int n = indices.length;
		tail = n - 1;
		crossingCount = 0;
		for (int i = 0; i < n; i++) {
			prev[i] = i - 1;
			next[i] = i + 1;
			crossingCount += svalues[i];
		}
		prev[0] = n;
		prev[n] = n - 1;
		next[n] = 0;
	}

	public void remove(int node) {
		int pos = indices[node];
		if (pos <= tail) {
			crossingCount -= svalues[pos];
			crossingCount += svalues[next[tail]];
			tail = next[tail];
		}
		next[prev[pos]] = next[pos];
		prev[next[pos]] = prev[pos];
		// prev[pos] = -1;
		// next[pos] = -1;
	}

	public void next() {
		crossingCount -= svalues[tail];
		tail = prev[tail];
	}

	public final int getCrossingCount() {
		return crossingCount;
	}

	@Override
	public String toString() {
		return "AssignmentRowBuilder [\n svalues=" + Arrays.toString(svalues) + ",\n indices="
				+ Arrays.toString(indices) + ",\n tail=" + tail + ",\n next=" + Arrays.toString(next) + ",\n prev="
				+ Arrays.toString(prev) + ",\n crossingCount=" + crossingCount + "\n]";
	}

	public static void main(String[] args) {
		int[] values = new int[] { 5, 8, 3, 7, 10 };
		AssignmentRowBuilder lb = new AssignmentRowBuilder(values);
		lb.setUp();
		System.out.println(lb.getCrossingCount());
		lb.next();
		System.out.println(lb.getCrossingCount());
		lb.remove(0);
		System.out.println(lb.getCrossingCount());
		System.out.println(lb);

		lb.next();
		System.out.println(lb.getCrossingCount());

		lb.next();
		System.out.println(lb.getCrossingCount());

		lb.remove(1);
		lb.remove(4);
		System.out.println(lb.getCrossingCount());
		System.out.println(lb);

		lb.next();
		System.out.println(lb.getCrossingCount());

		lb.remove(2);
		System.out.println(lb.getCrossingCount());

		/////////////////
		lb.setUp();
		lb.next();
		lb.remove(2);
		System.out.println(lb);

		lb.next();
		lb.remove(4);
		System.out.println(lb);

	}
}
