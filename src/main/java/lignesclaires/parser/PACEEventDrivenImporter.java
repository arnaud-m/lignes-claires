/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jgrapht.alg.util.Triple;
import org.jgrapht.nio.BaseEventDrivenImporter;
import org.jgrapht.nio.EventDrivenImporter;
import org.jgrapht.nio.ImportEvent;
import org.jgrapht.nio.ImportException;
import org.jgrapht.nio.dimacs.DIMACSEventDrivenImporter;

/**
 * A generic importer using consumers for PACE format.
 *
 * @see DIMACSEventDrivenImporter
 */
public class PACEEventDrivenImporter extends BaseEventDrivenImporter<Integer, Triple<Integer, Integer, Double>>
		implements EventDrivenImporter<Integer, Triple<Integer, Integer, Double>> {
	private boolean zeroBasedNumbering;
	private boolean renumberVertices;

	private int fixedCount;
	private int freeCount;
	private int edgeCount;

	private Map<String, Integer> vertexMap;
	private int nextId;

	/**
	 * Construct a new importer
	 */
	public PACEEventDrivenImporter() {
		super();
		this.zeroBasedNumbering = true;
		this.renumberVertices = true;
		this.vertexMap = new HashMap<>();
	}

	public final int getFixedCount() {
		return fixedCount;
	}

	public final int getFreeCount() {
		return freeCount;
	}

	/**
	 * Set whether to use zero-based numbering for vertices.
	 * 
	 * The DIMACS format by default starts vertices numbering from one. If true then
	 * we will use zero-based numbering. Default to true.
	 * 
	 * @param zeroBasedNumbering whether to use zero-based numbering
	 * @return the importer
	 */
	public PACEEventDrivenImporter zeroBasedNumbering(boolean zeroBasedNumbering) {
		this.zeroBasedNumbering = zeroBasedNumbering;
		return this;
	}

	/**
	 * Set whether to renumber vertices or not.
	 * 
	 * If true then the vertices are assigned new numbers from $0$ to $n-1$ in the
	 * order that they are first encountered in the file. Otherwise, the original
	 * numbering (minus one in order to get a zero-based numbering) of the DIMACS
	 * file is kept. Defaults to true.
	 * 
	 * @param renumberVertices whether to renumber vertices or not
	 * @return the importer
	 */
	public PACEEventDrivenImporter renumberVertices(boolean renumberVertices) {
		this.renumberVertices = renumberVertices;
		return this;
	}

	@Override
	public void importInput(Reader input) {
		// convert to buffered
		BufferedReader in;
		if (input instanceof BufferedReader) {
			in = (BufferedReader) input;
		} else {
			in = new BufferedReader(input);
		}

		if (zeroBasedNumbering) {
			this.nextId = 0;
		} else {
			this.nextId = 1;
		}

		notifyImportEvent(ImportEvent.START);

		// Dimensions
		readDimensions(in);
		final int size = freeCount + fixedCount;
		notifyVertexCount(size);
		notifyEdgeCount(edgeCount);

		// add edges
		String[] cols = skipComments(in);
		while (cols != null) {
			if (cols.length < 2) {
				throw new ImportException("Failed to parse edge:" + Arrays.toString(cols));
			}
			Integer source;
			try {
				source = Integer.parseInt(cols[0]);
			} catch (NumberFormatException e) {
				throw new ImportException("Failed to parse edge source node:" + e.getMessage(), e);
			}
			Integer target;
			try {
				target = Integer.parseInt(cols[1]);
			} catch (NumberFormatException e) {
				throw new ImportException("Failed to parse edge target node:" + e.getMessage(), e);
			}

			Integer from = mapVertexToInteger(String.valueOf(source));
			Integer to = mapVertexToInteger(String.valueOf(target));

			Double weight = null;
			if (cols.length > 2) {
				try {
					weight = Double.parseDouble(cols[2]);
				} catch (NumberFormatException e) {
					// ignore
				}
			}

			// notify
			notifyEdge(Triple.of(from, to, weight));
			cols = skipComments(in);
		}

		notifyImportEvent(ImportEvent.END);
	}

	private String[] split(final String src) {
		if (src == null) {
			return null;
		}
		return src.split("\\s+");
	}

	private String[] skipComments(BufferedReader input) {
		String[] cols = null;
		try {
			cols = split(input.readLine());
			while ((cols != null) && ((cols.length == 0) || cols[0].equals("c") || cols[0].startsWith("%"))) {
				cols = split(input.readLine());
			}
		} catch (IOException e) {
			// ignore
		}
		return cols;
	}

	private void readDimensions(BufferedReader input) throws ImportException {
		final String[] cols = skipComments(input);
		if (cols != null && cols[0].equals("p") && cols.length >= 5) {
			try {
				fixedCount = Integer.parseInt(cols[2]);
				freeCount = Integer.parseInt(cols[3]);
				edgeCount = Integer.parseInt(cols[4]);
				if (fixedCount >= 0 && freeCount >= 0 && edgeCount >= 0) {
					return;
				}
			} catch (NumberFormatException e) {
				// Fails at exit
			}
		}
		throw new ImportException("Failed to read graph dimensions.");
	}

	/**
	 * Map a vertex identifier to an integer.
	 * 
	 * @param id the vertex identifier
	 * @return the integer
	 */
	protected Integer mapVertexToInteger(String id) {
		if (renumberVertices) {
			return vertexMap.computeIfAbsent(id, (keyId) -> {
				return nextId++;
			});
		} else {
			if (zeroBasedNumbering) {
				return Integer.valueOf(id) - 1;
			} else {
				return Integer.valueOf(id);
			}
		}
	}

}
