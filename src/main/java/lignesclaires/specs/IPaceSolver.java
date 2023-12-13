/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.specs;

import lignesclaires.config.PaceConfig;
import lignesclaires.solver.CryptaChocoException;

public interface IPaceSolver {

	boolean solve(IBipartiteGraph bigraph, PaceConfig config) throws CryptaChocoException;

}
