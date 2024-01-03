/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.solver;

import org.chocosolver.solver.Solver;

import lignesclaires.specs.IChocoModel;

public enum Status {

	OPTIMUM, SATISFIABLE, UNSATISFIABLE, UNKNOWN, ERROR;

	public static Status getStatus(final IChocoModel model) {
		return getStatus(model.getSolver());
	}

	public static Status getStatus(final Solver solver) {
		switch (solver.getSearchState()) {
		case TERMINATED:
			if (solver.getSolutionCount() == 0) {
				return UNSATISFIABLE;
			} else {
				return solver.hasObjective() ? OPTIMUM : SATISFIABLE;
			}
		case STOPPED:
			return solver.getSolutionCount() == 0 ? UNKNOWN : SATISFIABLE;
		default:
			return ERROR;
		}
	}
}
