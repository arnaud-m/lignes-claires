/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
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
import lignesclaires.solver.Status;
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
				Assert.assertNotEquals(Status.ERROR, solver.solve(bigraph, config).getStatus());
			}
		}
	}

	@Test
	public void testSolver554() throws OCSolverException {
		BipartiteGraph bigraph = new BipartiteGraph(5, 5, 4);
		bigraph.addEdge(2, 8);
		bigraph.addEdge(3, 6);
		bigraph.addEdge(3, 9);
		bigraph.addEdge(4, 10);
		testAll(bigraph);
	}

	@Test
	public void testSolver557() throws OCSolverException {
		BipartiteGraph builder = new BipartiteGraph(5, 5, 7);
		builder.addEdge(1, 7);
		builder.addEdge(1, 8);
		builder.addEdge(2, 8);
		builder.addEdge(3, 6);
		builder.addEdge(4, 6);
		builder.addEdge(4, 8);
		builder.addEdge(5, 7);
		testAll(builder);
	}

	@Test
	public void testSolver10109() throws OCSolverException {
		BipartiteGraph bigraph = new BipartiteGraph(10, 10, 9);
		bigraph.addEdge(1, 16);
		bigraph.addEdge(3, 13);
		bigraph.addEdge(4, 13);
		bigraph.addEdge(5, 14);
		bigraph.addEdge(9, 11);
		bigraph.addEdge(10, 15);
		bigraph.addEdge(10, 17);
		bigraph.addEdge(10, 19);
		bigraph.addEdge(10, 20);
		testAll(bigraph);
	}

	@Test
	public void testSolver101012() throws OCSolverException {
		BipartiteGraph bigraph = new BipartiteGraph(10, 10, 12);
		bigraph.addEdge(1, 18);
		bigraph.addEdge(2, 11);
		bigraph.addEdge(2, 14);
		bigraph.addEdge(2, 20);
		bigraph.addEdge(4, 15);
		bigraph.addEdge(5, 11);
		bigraph.addEdge(5, 13);
		bigraph.addEdge(6, 17);
		bigraph.addEdge(7, 11);
		bigraph.addEdge(7, 16);
		bigraph.addEdge(8, 15);
		bigraph.addEdge(9, 18);
		testAll(bigraph);
	}

}
