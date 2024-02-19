/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.solver;

public enum OCModelFlag {

	RR1, RR2, RR3, RRLO2, DISJ, LB, TRANS;

	@Deprecated
	public final boolean isPresent(final int mask) {
		final int flag = 1 << ordinal();
		return (mask & flag) != 0;
	}

	@Deprecated
	public static final int getAllFlags() {
		return (1 << values().length) - 1;
	}

}
