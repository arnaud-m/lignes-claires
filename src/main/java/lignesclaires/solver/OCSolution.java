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

import lignesclaires.ToStringUtil;

public class OCSolution {

	public static final OCSolution SINGLOTON_UNKNOWN = new OCSolution(Status.UNKNOWN);
	public static final OCSolution SINGLOTON_ERROR = new OCSolution(Status.ERROR);

	private final Status status;
	private final OptionalInt objective;
	private final Optional<int[]> permutation;

	public OCSolution(final Status status) {
		super();
		this.status = status;
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

	public static final OCSolution getUnknownInstance() {
		return SINGLOTON_UNKNOWN;
	}

	public static final OCSolution getErrorInstance() {
		return SINGLOTON_ERROR;
	}

	@Override
	public String toString() {
		return permutation.isEmpty() ? "" : ToStringUtil.toString(permutation.get(), "\n");
	}

}