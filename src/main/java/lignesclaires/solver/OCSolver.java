/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.solver;

import org.chocosolver.solver.Solution;

import lignesclaires.choco.ChocoLogger;
import lignesclaires.cmd.Verbosity;
import lignesclaires.config.LignesClairesConfig;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IOCSolver;

public class OCSolver implements IOCSolver {

	@Override
	public boolean solve(IBipartiteGraph bigraph, LignesClairesConfig config) throws OCSolverException {
		OCModel mod = new OCModel(bigraph, config.getModelMask());
		mod.buildModel();
		mod.configureSearch(config.getSearch());
		if (config.getTimeLimit() > 0) {
			mod.getSolver().limitTime(config.getTimeLimit() * 1000);
		}
		if (config.getSolutionLimit() > 0) {
			mod.getSolver().limitSolution(config.getSolutionLimit());
		}
		ChocoLogger.logOnModel(mod);
		if (config.getVerbosity() == Verbosity.DEBUG) {
			mod.getSolver().showDecisions();
		}
		final Solution s = mod.createSolution();
		while (mod.getSolver().solve()) {
			s.record();
			ChocoLogger.logOnSolutionFound(mod, s);
		}
		if (mod.getSolver().getSolutionCount() > 0) {
			ChocoLogger.logOnSolution(mod, s);
		}
		ChocoLogger.logOnSolver(mod);
		return !mod.getSolver().hasEndedUnexpectedly();
	}

}
