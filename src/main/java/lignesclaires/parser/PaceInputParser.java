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

import lignesclaires.bigraph.BipartiteGraph;
import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IGraphParser;

public class PaceInputParser implements IGraphParser<IBipartiteGraph> {

	@Override
	public IBipartiteGraph parse(final Scanner scanner) throws InvalidGraphFormatException {
		try {
			skipComments(scanner);
			scanner.next();
			scanner.next();
			final int fixedCount = scanner.nextInt();
			final int freeCount = scanner.nextInt();
			final int edgeCount = scanner.nextInt();
			BipartiteGraph bigraph = new BipartiteGraph(fixedCount, freeCount, edgeCount);
			for (int i = 0; i < edgeCount; i++) {
				final int fixed = scanner.nextInt();
				if (fixed < 1 || fixed > fixedCount) {
					throw new InvalidGraphFormatException("Invalid fixed node:" + fixed);
				}
				final int free = scanner.nextInt();
				if (free < fixedCount + 1 || free > fixedCount + freeCount + 1) {
					throw new InvalidGraphFormatException("Invalid free node:" + free);
				}
				bigraph.addEdge(fixed, free);
			}
			bigraph.sort();
			return bigraph;
		} catch (ArrayIndexOutOfBoundsException | NoSuchElementException | IllegalStateException e) {
			throw new InvalidGraphFormatException();
		}
	}

	public static final void skipComments(final Scanner sc) {
		final String pattern = "c\\h*.*\\v";
		boolean skipCLine = true;
		do {
			try {
				sc.skip(pattern);
			} catch (NoSuchElementException e) {
				skipCLine = false;
			}
			// System.out.println("A " + sc.next());
		} while (skipCLine);
	}

}
