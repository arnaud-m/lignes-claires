/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import lignesclaires.bigraph.BipartiteGraph;
import lignesclaires.config.LignesClairesConfig;
import lignesclaires.solver.OCModel;
import lignesclaires.solver.OCSearch;
import lignesclaires.solver.OCSolver;
import lignesclaires.solver.OCSolverException;
import lignesclaires.specs.IBipartiteGraph;

public class TestSolver {

	private final LignesClairesConfig config = new LignesClairesConfig();

	private final OCSolver solver = new OCSolver();

	@BeforeClass
	public static void setTestLoggers() {
		JULogUtil.configureTestLoggers();
	}

	public void testAll(IBipartiteGraph bigraph) throws OCSolverException {
		for (OCSearch search : OCSearch.values()) {
			config.setSearch(search);
			for (int modelMask = 0; modelMask < OCModel.LB; modelMask++) {
				config.setModelMask(modelMask);
				Assert.assertTrue(solver.solve(bigraph, config));
			}
		}
	}

	@Test
	public void testSolver554() throws OCSolverException {
		BipartiteGraph.Builder builder = new BipartiteGraph.Builder(5, 5, 4);
		builder.addGrEdge(2, 8);
		builder.addGrEdge(3, 6);
		builder.addGrEdge(3, 9);
		builder.addGrEdge(4, 10);
		testAll(builder.build());
	}

	@Test
	public void testSolver557() throws OCSolverException {
		BipartiteGraph.Builder builder = new BipartiteGraph.Builder(5, 5, 7);
		builder.addGrEdge(1, 7);
		builder.addGrEdge(1, 8);
		builder.addGrEdge(2, 8);
		builder.addGrEdge(3, 6);
		builder.addGrEdge(4, 6);
		builder.addGrEdge(4, 8);
		builder.addGrEdge(5, 7);
		testAll(builder.build());
	}

	@Test
	public void testSolver10109() throws OCSolverException {
		BipartiteGraph.Builder builder = new BipartiteGraph.Builder(10, 10, 9);
		builder.addGrEdge(1, 16);
		builder.addGrEdge(3, 13);
		builder.addGrEdge(4, 13);
		builder.addGrEdge(5, 14);
		builder.addGrEdge(9, 11);
		builder.addGrEdge(10, 15);
		builder.addGrEdge(10, 17);
		builder.addGrEdge(10, 19);
		builder.addGrEdge(10, 20);
		testAll(builder.build());
	}

	@Test
	public void testSolver101012() throws OCSolverException {
		BipartiteGraph.Builder builder = new BipartiteGraph.Builder(10, 10, 12);
		builder.addGrEdge(1, 18);
		builder.addGrEdge(2, 11);
		builder.addGrEdge(2, 14);
		builder.addGrEdge(2, 20);
		builder.addGrEdge(4, 15);
		builder.addGrEdge(5, 11);
		builder.addGrEdge(5, 13);
		builder.addGrEdge(6, 17);
		builder.addGrEdge(7, 11);
		builder.addGrEdge(7, 16);
		builder.addGrEdge(8, 15);
		builder.addGrEdge(9, 18);
		testAll(builder.build());
	}

}
