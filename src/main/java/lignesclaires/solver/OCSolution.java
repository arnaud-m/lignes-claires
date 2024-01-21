/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.solver;

import java.util.Optional;
import java.util.OptionalInt;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;

import lignesclaires.graph.DepthFirstSearch;

public class OCSolution {

	private final Status status;
	private final OptionalInt objective;
	private final Optional<int[]> permutation;

	public OCSolution() {
		super();
		this.status = Status.ERROR;
		this.objective = OptionalInt.empty();
		this.permutation = Optional.empty();
	}

	public OCSolution(final OCModel model, final Solution solution) {
		super();
		this.status = Status.getStatus(model);
		final Solver solver = model.getSolver();
		if (solver.getSolutionCount() > 0) {
			this.objective = solver.hasObjective() ? OptionalInt.of(solver.getBestSolutionValue().intValue())
					: OptionalInt.empty();
			this.permutation = Optional.of(model.recordSolution(solution));
		} else {
			this.objective = OptionalInt.empty();
			this.permutation = Optional.empty();
		}
	}

	public final Status getStatus() {
		return status;
	}

	public final OptionalInt getObjective() {
		return objective;
	}

	public final Optional<int[]> getPermutation() {
		return permutation;
	}

	@Override
	public String toString() {
		return permutation.isEmpty() ? "" : DepthFirstSearch.toString(permutation.get(), "\n");
	}

}