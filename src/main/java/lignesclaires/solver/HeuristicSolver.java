/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.solver;

import java.util.logging.Level;

import lignesclaires.LignesClaires;
import lignesclaires.ToStringUtil;
import lignesclaires.config.LignesClairesConfig;
import lignesclaires.graph.BGraph;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IOCSolver;

public class HeuristicSolver implements IOCSolver {

	@Override
	public OCSolution solve(IBipartiteGraph bigraph, OCSolution initialSolution, LignesClairesConfig config)
			throws OCSolverException {
		if (bigraph instanceof BGraph) {
			final BGraph gr = (BGraph) bigraph;

			final Integer[] permM = gr.permutateMedians();
			final int ccountM = solve(bigraph, permM, "Median");

			final Integer[] permB = gr.permutateBarycenters();
			final int ccountB = solve(bigraph, permB, "Barycenter");

			final int n = bigraph.getFixedCount() + 1;
			return ccountM <= ccountB ? new OCSolution(ccountM, permM, n) : new OCSolution(ccountB, permB, n);
		} else {
			return OCSolution.getUnknownInstance();
		}
	}

	private int solve(IBipartiteGraph bigraph, Integer[] permutation, String name) {
		int ccount = getCrossingCount(bigraph, permutation);
		logOnSolution(name, "heuristic", ccount, permutation);
		int delta = bigraph.getCrossingCounts().greedySwitching(permutation);
		if (delta > 0) {
			ccount -= delta;
			logOnSolution(name, "greedy switching", ccount, permutation);
		}
		return ccount;
	}

	private static void logOnSolution(String name, String phase, int crossingCount, Integer[] permutation) {
		if (LignesClaires.LOGGER.isLoggable(Level.INFO)) {
			LignesClaires.LOGGER.log(Level.INFO, "{0} {1}:\no {2,number,#}\nv {3}",
					new Object[] { name, phase, crossingCount, ToStringUtil.toString(permutation, " ") });
		}
	}

	private static int getCrossingCount(IBipartiteGraph bigraph, Integer[] permutation) {
		return bigraph.getCrossingCounts().getCrossingCounts(permutation);
	}
}
