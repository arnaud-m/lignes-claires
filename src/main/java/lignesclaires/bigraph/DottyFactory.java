/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.bigraph;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lignesclaires.specs.IDotty;

public class DottyFactory {

	private final StringBuilder b;
	private final String graphType;
	private final String edgeType;

	public DottyFactory(boolean directed) {
		super();
		this.b = new StringBuilder();
		if (directed) {
			graphType = "digraph";
			edgeType = "->";
		} else {
			graphType = "graph";
			edgeType = "--";
		}
	}

	public final void beginGraph() {
		b.append(graphType).append(" G{\n");
	}

	public final void endGraph() {
		b.append("\n}\n");
	}

	public final void beginBlock(String nodeAttributes) {
		beginBlock(Optional.of(nodeAttributes), Optional.empty());
	}

	public final void beginBlock(String nodeAttributes, String edgeAttributes) {
		beginBlock(Optional.of(nodeAttributes), Optional.of(edgeAttributes));
	}

	public final void beginBlock(Optional<String> nodeAttributes, Optional<String> edgeAttributes) {
		b.append("{\n");
		addAttributes("node", nodeAttributes);
		addAttributes("edge", edgeAttributes);
	}

	public final void endBlock() {
		b.append("\n}\n");
	}

	public final void endInst() {
		b.append(';');
	}

	public final void endLine() {
		endInst();
		b.append('\n');
	}

	public final void brackets(String attrs) {
		b.append(" [").append(attrs).append("]");
	}

	public final void addNode(final int id) {
		b.append(id);
		endInst();
	}

	public final void addAttributes(final int id, String attributes) {
		b.append(id);
		brackets(attributes);
		endLine();
	}

	public final void addAttributes(final String id, String attributes) {
		b.append(id);
		brackets(attributes);
		endLine();
	}

	public final void addAttributes(final String id, Optional<String> attributes) {
		attributes.ifPresent(attrs -> addAttributes(id, attrs));
	}

	public final void addEdge(final int origin, int destination) {
		addEdge(String.valueOf(origin), String.valueOf(destination), Optional.empty());
	}

	public final void addEdge(String origin, String destination) {
		addEdge(origin, destination, Optional.empty());
	}

	public final void addEdge(int origin, int destination, Optional<String> edgeAttributes) {
		b.append(origin).append(edgeType).append(destination);
		edgeAttributes.ifPresent(this::brackets);
		endLine();
	}

	public final void addEdge(String origin, String destination, Optional<String> edgeAttributes) {
		b.append(origin).append(edgeType).append(destination);
		edgeAttributes.ifPresent(this::brackets);
		endLine();
	}

	public void toDotty(IDotty[] data) {
		b.append(Stream.of(data).map(IDotty::toDotty).collect(Collectors.joining("\n")));
	}

	@Override
	public String toString() {
		return b.toString();
	}

}
