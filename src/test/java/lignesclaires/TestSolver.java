/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.ImportException;
import org.junit.BeforeClass;
import org.junit.Test;

import lignesclaires.config.LignesClairesConfig;
import lignesclaires.graph.JGraphtUtil;
import lignesclaires.parser.PACEImporter;
import lignesclaires.parser.PaceInputParser;
import lignesclaires.solver.OCModel;
import lignesclaires.solver.OCSearch;
import lignesclaires.solver.OCSolution;
import lignesclaires.solver.OCSolver;
import lignesclaires.solver.OCSolverException;
import lignesclaires.solver.Status;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IGraphParser;

public class TestSolver {

	private final LignesClairesConfig config = new LignesClairesConfig();

	private final OCSolver solver = new OCSolver();

	@BeforeClass
	public static void setTestLoggers() {
		JULogUtil.configureTestLoggers();
	}

	private final IBipartiteGraph getResourceGraph(final String resourcePath)
			throws ImportException, FileNotFoundException {
		final IGraphParser<IBipartiteGraph> parser = new PaceInputParser();
		final InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath);
		// Temporary
		final InputStream in2 = getClass().getClassLoader().getResourceAsStream(resourcePath);
		PACEImporter<Integer, DefaultEdge> importer = new PACEImporter<>();
		importer.setVertexFactory(i -> i);
		Graph<Integer, DefaultEdge> graph = JGraphtUtil.unweightedUndirected();
		importer.importGraph(graph, in2);
		//
		return parser.parse(in);
	}

	public void testAll(String resourcePath, int optimum)
			throws OCSolverException, ImportException, FileNotFoundException, ImportException, FileNotFoundException {
		final IBipartiteGraph graph = getResourceGraph(resourcePath);
		for (OCSearch search : OCSearch.values()) {
			config.setSearch(search);
			for (int modelMask = 0; modelMask < 2 * OCModel.LB - 1; modelMask++) {
				config.setModelMask(modelMask);
				final OCSolution sol = solver.solve(graph, config);
				assertEquals(Status.OPTIMUM, sol.getStatus());
				assertTrue(sol.getObjective().isPresent());
				assertEquals(optimum, sol.getObjective().getAsInt());
			}
		}
	}

	public void testTiny(String resourcePath, int optimum)
			throws OCSolverException, ImportException, FileNotFoundException {
		testAll("tiny_test_set/" + resourcePath, optimum);
	}

	@Test
	public void testComplete_4_5() throws OCSolverException, ImportException, FileNotFoundException {
		testTiny("complete_4_5.gr", 60);
	}

	@Test
	public void testCycle_8_shuffled() throws OCSolverException, ImportException, FileNotFoundException {
		testTiny("cycle_8_shuffled.gr", 4);
	}

	@Test
	public void testCycle_8_sorted() throws OCSolverException, ImportException, FileNotFoundException {
		testTiny("cycle_8_sorted.gr", 3);
	}

	@Test
	public void testGrid_9_shuffled() throws OCSolverException, ImportException, FileNotFoundException {
		testTiny("grid_9_shuffled.gr", 17);
	}

	@Test
	public void testLadder_4_4_shuffled() throws OCSolverException, ImportException, FileNotFoundException {
		testTiny("ladder_4_4_shuffled.gr", 11);
	}

	@Test
	public void testLadder_4_4_sorted() throws OCSolverException, ImportException, FileNotFoundException {
		testTiny("ladder_4_4_sorted.gr", 3);
	}

	@Test
	public void testMatching_4_4() throws OCSolverException, ImportException, FileNotFoundException {
		testTiny("matching_4_4.gr", 0);
	}

	@Test
	public void testPath_9_shuffled() throws OCSolverException, ImportException, FileNotFoundException {
		testTiny("path_9_shuffled.gr", 6);
	}

	@Test
	public void testPath_9_sorted() throws OCSolverException, ImportException, FileNotFoundException {
		testTiny("path_9_sorted.gr", 0);
	}

	@Test
	public void testPlane_5_6() throws OCSolverException, ImportException, FileNotFoundException {
		testTiny("plane_5_6.gr", 0);
	}

	@Test
	public void testStar_6() throws OCSolverException, ImportException, FileNotFoundException {
		testTiny("star_6.gr", 0);
	}

	@Test
	public void testTree_6_10() throws OCSolverException, ImportException, FileNotFoundException {
		testTiny("tree_6_10.gr", 13);
	}

	@Test
	public void testWebsite_20() throws OCSolverException, ImportException, FileNotFoundException {
		testTiny("website_20.gr", 17);
	}

}
