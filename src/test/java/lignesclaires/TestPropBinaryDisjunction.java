/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires;

import java.util.List;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import lignesclaires.choco.PropBinaryDisjunction;

public class TestPropBinaryDisjunction {

	private final int n = 5;
	private final int ns = n * (n + 1);

	private Model m;
	private IntVar x;
	private IntVar y;
	private IntVar b;

	private IntVar[] vars;

	@Before
	public void buildModel() {
		m = new Model();
		x = m.intVar("x", 0, n);
		y = m.intVar("y", 0, n);
		b = m.intVar("b", new int[] { 0, n });
		vars = new IntVar[] { x, y, b };
		Constraint c = new Constraint("MyConstraint", new PropBinaryDisjunction(vars));
		m.post(c);
	}

	public void testEnumerate(int expectedSolutionCount, boolean randomSearch) {
		if (randomSearch) {
			m.getSolver().setSearch(Search.randomSearch(vars, expectedSolutionCount));
		}
		final List<Solution> sols = m.getSolver().findAllSolutions();
		Assert.assertEquals(expectedSolutionCount, sols.size());
	}

	@Test
	public void testBinDisj01() {
		testEnumerate(ns, false);
	}

	@Test
	public void testBinDisj0() {
		b.eq(0).post();
		testEnumerate(ns / 2, false);
	}

	@Test
	public void testBinDisj1() {
		b.ne(0).post();
		testEnumerate(ns / 2, false);
	}

	@Test
	public void testRandBinDisj01() {
		testEnumerate(ns, true);
	}

	@Test
	public void testRandBinDisj0() {
		b.eq(0).post();
		testEnumerate(ns / 2, true);
	}

	@Test
	public void testRandBinDisj1() {
		b.ne(0).post();
		testEnumerate(ns / 2, true);
	}
}
