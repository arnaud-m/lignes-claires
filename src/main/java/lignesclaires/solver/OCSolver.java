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
		Solution s = new Solution(mod.getModel());
		s.record();
		System.out.println(s);
		ChocoLogger.logOnSolver(mod);
		return solved;
	}

}
