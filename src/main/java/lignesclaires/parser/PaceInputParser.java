/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.parser;

import java.io.FileNotFoundException;
import java.io.Reader;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.ImportException;

import lignesclaires.graph.BGraph;
import lignesclaires.graph.JGraphtUtil;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IGraphParser;

public class PaceInputParser implements IGraphParser<IBipartiteGraph> {

	@Override
	public IBipartiteGraph parse(Reader reader) throws ImportException, FileNotFoundException {
		final PACEImporter<Integer, DefaultEdge> importer = new PACEImporter<>();
		importer.setVertexFactory(i -> i);
		final Graph<Integer, DefaultEdge> graph = JGraphtUtil.unweightedUndirected();

		importer.importGraph(graph, reader);
		return new BGraph(graph, importer.getFixedCount(), importer.getFreeCount());
	}

}
