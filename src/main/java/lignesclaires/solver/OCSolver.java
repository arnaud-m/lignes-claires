/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.solver;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lignesclaires.choco.ChocoLogger;
import lignesclaires.cmd.Verbosity;
import lignesclaires.config.LignesClairesConfig;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IOCSolver;

public class OCSolver implements IOCSolver {

	@Override
	public boolean solve(IBipartiteGraph bigraph, LignesClairesConfig config) throws OCSolverException {
		OCModel mod = new OCModel(bigraph);
		mod.buildModel();
		ChocoLogger.logOnModel(mod);
		boolean solved = mod.getSolver().solve();
		ChocoLogger.logOnSolution(mod, config.getVerbosity());
		if (config.getVerbosity() == Verbosity.QUIET) {
			System.out.println();
		}
		ChocoLogger.logOnSolver(mod);
		return solved;
	}

	public static final String toString(int[] values, CharSequence delimiter) {
		return IntStream.of(values).mapToObj(Integer::toString).collect(Collectors.joining(delimiter));
	}

}
