/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.bigraph.rules;

import lignesclaires.bigraph.CrossingCounts;
import lignesclaires.specs.IBipartiteGraph;

class BiGraphWithCounts {

	protected final IBipartiteGraph graph;
	protected final CrossingCounts counts;

	protected BiGraphWithCounts(IBipartiteGraph graph) {
		super();
		this.graph = graph;
		this.counts = graph.getCrossingCounts();
	}

}