/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.solver;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;

import lignesclaires.LignesClaires;
import lignesclaires.choco.ChocoLogger;
import lignesclaires.cmd.Verbosity;
import lignesclaires.config.LignesClairesConfig;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IOCSolver;

public class OCSolver implements IOCSolver {

	@Override
	public OCSolution solve(final IBipartiteGraph bigraph, final OCSolution initialSolution,
			final LignesClairesConfig config) throws OCSolverException {
		if (initialSolution.getStatus() == Status.OPTIMUM) {
			return initialSolution;
		}
		final OCModel mod = build(bigraph, initialSolution, config);
		if (config.isDryRun()) {
			return initialSolution;
		} else {
			final Solver solver = mod.getSolver();
			final Solution sol = mod.createSolution();
			while (solver.solve()) {
				sol.record();
				ChocoLogger.logOnSolution(mod, sol);
			}
			if (solver.getSolutionCount() > 0) {
				ChocoLogger.logOnBestSolution(mod, sol);
			}
			ChocoLogger.logOnSolver(mod);

			final Status status = Status.getStatus(mod);
			switch (status) {
			case OPTIMUM:
			case SATISFIABLE:
				return new OCSolution(status, solver.getBestSolutionValue().intValue(), mod.recordSolution(sol));
			case UNSATISFIABLE: {
				return initialSolution.getStatus() == Status.SATISFIABLE
						? new OCSolution(Status.OPTIMUM, initialSolution.getObjective(),
								initialSolution.getPermutation())
						: new OCSolution(Status.UNSATISFIABLE);
			}
			default:
				return initialSolution;
			}

		}
	}

	private OCModel build(final IBipartiteGraph bigraph, final OCSolution initialSolution,
			final LignesClairesConfig config) {
		final OCModel mod = new OCModel(bigraph, config.getModelMask());
		if (config.isReport()) {
			mod.setExportPath(LignesClaires.getFilenameWithoutExtension(config.getGraphFile()));
		}
		mod.buildModel();
		mod.postUpperBound(initialSolution.getObjective());

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
		if (config.getVerbosity() == Verbosity.TRACE) {
			solver.showDecisions();
		}
		return mod;
	}

}
