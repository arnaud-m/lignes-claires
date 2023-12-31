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

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;

import lignesclaires.choco.ChocoLogger;
import lignesclaires.cmd.Verbosity;
import lignesclaires.config.LignesClairesConfig;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IOCSolver;

public class OCSolver implements IOCSolver {

	@Override
	public OCSolution solve(final IBipartiteGraph bigraph, final LignesClairesConfig config) throws OCSolverException {
		final OCModel mod = new OCModel(bigraph, config.getModelMask());
		mod.buildModel();
		mod.configureSearch(config.getSearch());
		if (config.isWithRestarts()) {
			mod.configureRestarts();
		}

		final Solver solver = mod.getSolver();
		if (config.getTimeLimit() > 0) {
			solver.limitTime(config.getTimeLimit() * 1000);
		}
		if (config.getSolutionLimit() > 0) {
			solver.limitSolution(config.getSolutionLimit());
		}

		ChocoLogger.logOnModel(mod);
		if (config.getVerbosity() == Verbosity.DEBUG) {
			solver.showDecisions();
		}
		final Solution sol = mod.createSolution();
		while (solver.solve()) {
			sol.record();
			ChocoLogger.logOnSolution(Level.FINE, mod, sol);
		}

		if (mod.getSolver().getSolutionCount() > 0) {
			ChocoLogger.logOnSolution(Level.INFO, mod, sol);
		}
		ChocoLogger.logOnSolver(mod);
		return new OCSolution(mod, sol);
	}

}
