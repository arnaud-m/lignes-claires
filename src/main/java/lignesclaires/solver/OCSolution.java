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
import java.util.stream.Stream;

import lignesclaires.ToStringUtil;

public class OCSolution {

	public static final OCSolution SINGLOTON_UNKNOWN = new OCSolution(Status.UNKNOWN);
	public static final OCSolution SINGLOTON_ERROR = new OCSolution(Status.ERROR);

	private final Status status;
	private final OptionalInt objective;
	private final Optional<int[]> permutation;

	public OCSolution(Status status, OptionalInt objective, Optional<int[]> permutation) {
		super();
		this.status = status;
		this.objective = objective;
		this.permutation = permutation;
	}

	public OCSolution(final Status status) {
		this(status, OptionalInt.empty(), Optional.empty());
	}

	public OCSolution(final Status status, final int objective, final int[] permutation) {
		this(status, OptionalInt.of(objective), Optional.of(permutation));

	}

	public OCSolution(final int objective, final Integer[] permutation, final int offset) {
		this(Status.SATISFIABLE, OptionalInt.of(objective),
				Optional.of(Stream.of(permutation).mapToInt(i -> i + offset).toArray()));
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

	public String toPaceOutputString() {
		return permutation.isEmpty() ? "" : ToStringUtil.toString(permutation.get(), "\n");
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append("s ").append(getStatus());
		objective.ifPresent(obj -> b.append("\no ").append(obj));
		return b.toString();
	}

}