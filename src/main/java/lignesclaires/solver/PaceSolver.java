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

import lignesclaires.config.PaceConfig;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IPaceSolver;

public class PaceSolver implements IPaceSolver {

	@Override
	public boolean solve(IBipartiteGraph bigraph, PaceConfig config) throws CryptaChocoException {
		OneSideModel mod = new OneSideModel(bigraph);
		mod.buildModel();
		System.out.println(mod);
		boolean solved = mod.getSolver().solve();
		Solution s = new Solution(mod.getModel());
		s.record();
		System.out.println(s);
		mod.getSolver().printStatistics();
		return solved;
	}

}
