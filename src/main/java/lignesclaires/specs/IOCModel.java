/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.specs;

import org.chocosolver.solver.variables.IntVar;

public interface IOCModel extends IChocoModel {

	IBipartiteGraph getGraph();

	IntVar[] getPositionVars();

	IntVar[] getPermutationVars();

	IntVar getCrossingCountVar();

	/**
	 * Post the constraints of the model.
	 */
	void buildModel();

}
