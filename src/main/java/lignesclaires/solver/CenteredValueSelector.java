/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.solver;

import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.variables.IntVar;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class CenteredValueSelector implements IntValueSelector {

	private final TObjectIntMap<IntVar> variableCenters;

	public CenteredValueSelector(IntVar[] vars, int[] vals) {
		super();
		variableCenters = new TObjectIntHashMap<>(vars.length);
		for (int i = 0; i < vars.length; i++) {
			variableCenters.put(vars[i], vals[i]);
		}
	}

	private final int selectValueFromEnumerated(IntVar variable) {
		final int val = variableCenters.get(variable);
		if (variable.contains(val))
			return val;
		else {
			final int prev = variable.previousValue(val);
			final int next = variable.nextValue(val);
			return (val - prev < next - val) ? prev : next;
		}
	}

	private final int selectValueFromBounded(IntVar variable) {
		final int val = variableCenters.get(variable);
		final int lb = variable.getLB();
		if (lb >= val)
			return lb;
		else {
			final int ub = variable.getUB();
			if (ub <= val)
				return ub;
			else {
				return (val - lb < ub - val) ? lb : ub;
			}
		}
	}

	@Override
	public int selectValue(IntVar variable) {
		return variable.hasEnumeratedDomain() ? selectValueFromEnumerated(variable) : selectValueFromBounded(variable);

	}

}
