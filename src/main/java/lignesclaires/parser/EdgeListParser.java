/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.parser;

import java.util.NoSuchElementException;
import java.util.Scanner;

import lignesclaires.graph.AbstractGraph;
import lignesclaires.graph.DGraph;
import lignesclaires.graph.UGraph;
import lignesclaires.specs.IGraph;
import lignesclaires.specs.IGraphParser;

public class EdgeListParser implements IGraphParser<IGraph> {

	private final boolean directed;

	public EdgeListParser(final boolean directed) {
		super();
		this.directed = directed;
	}

	@Override
	public IGraph parse(final Scanner scanner) throws InvalidGraphFormatException {
		try {
			PaceInputParser.skipComments(scanner);
			final int nodeCount = scanner.nextInt();
			final int edgeCount = scanner.nextInt();
			AbstractGraph graph = directed ? new DGraph(nodeCount, edgeCount) : new UGraph(nodeCount, edgeCount);
			for (int i = 0; i < edgeCount; i++) {
				final int origin = scanner.nextInt();
				final int destination = scanner.nextInt();
				graph.addEdge(origin, destination);
			}
			graph.sort();
			return graph;
		} catch (ArrayIndexOutOfBoundsException | NoSuchElementException | IllegalStateException e) {
			throw new InvalidGraphFormatException();
		}
	}
}
