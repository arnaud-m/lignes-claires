/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.parser;

import java.util.NoSuchElementException;
import java.util.Scanner;

import lignesclaires.bigraph.BipartiteGraph;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IParser;

public class BiGraphParser implements IParser<IBipartiteGraph> {

	@Override
	public IBipartiteGraph parse(Scanner scanner) throws InvalidGraphFormatException {
		try {
			skipComments(scanner);
			scanner.next();
			scanner.next();
			final int fixedCount = scanner.nextInt();
			final int freeCount = scanner.nextInt();
			final int edgeCount = scanner.nextInt();
			BipartiteGraph bigraph = new BipartiteGraph(fixedCount, freeCount, edgeCount);
			for (int i = 0; i < edgeCount; i++) {
				bigraph.addGrEdge(scanner.nextInt(), scanner.nextInt());
			}
			bigraph.sort();
			return bigraph;
		} catch (ArrayIndexOutOfBoundsException | NoSuchElementException | IllegalStateException e) {
			throw new InvalidGraphFormatException();
		}
	}

	private static void skipComments(Scanner sc) {
		final String pattern = "^c.*\\v+";
		boolean skipCLine = true;
		do {
			try {
				sc.skip(pattern);
			} catch (NoSuchElementException e) {
				skipCLine = false;
			}
		} while (skipCLine);
	}

}
