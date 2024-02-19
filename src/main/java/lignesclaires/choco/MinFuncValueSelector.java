/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.choco;

import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;

import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.iterators.DisposableValueIterator;

public class MinFuncValueSelector implements IntValueSelector {

	double[] score;

	public MinFuncValueSelector(int n, IntToDoubleFunction fun) {
		super();
		this.score = IntStream.range(0, n).mapToDouble(fun).toArray();
	}

	@Override
	public int selectValue(IntVar ivar) {
		DisposableValueIterator vit = ivar.getValueIterator(true);
		int best = ivar.getLB();
		while (vit.hasNext()) {
			int v = vit.next();
			// operate on value v here
			if (score[v] < score[best]) {
				best = v;
			}
		}
		vit.dispose();
		return best;
	}

}
