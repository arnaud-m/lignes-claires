/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.graph;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.BlockCutpointGraph;
import org.jgrapht.graph.DefaultEdge;

import gnu.trove.list.array.TIntArrayList;
import lignesclaires.LignesClaires;
import lignesclaires.specs.IBipartiteGraph;

public class BGraph extends AbstractGraph implements IBipartiteGraph {

	private final static Logger LOGGER = LignesClaires.LOGGER;
	private final int fixedCount;
	private final int freeCount;
	private final int freeOffset;

	Optional<TIntArrayList[]> freeAdjLists;

	Optional<CrossingCounts> crossingCounts;

	Optional<CrossingCounts> reducedCrossingCounts;

	Optional<BlockCutpointGraph<Integer, DefaultEdge>> blockCutGraph;

	public BGraph(Graph<Integer, DefaultEdge> graph, int fixedCount, int freeCount) {
		super(graph);
		this.fixedCount = fixedCount;
		this.freeCount = freeCount;
		this.freeOffset = fixedCount + 1;
		freeAdjLists = Optional.empty();
		crossingCounts = Optional.empty();
		reducedCrossingCounts = Optional.empty();
		blockCutGraph = Optional.empty();
	}

	@Override
	public final int getFixedCount() {
		return fixedCount;
	}

	@Override
	public final int getFreeCount() {
		return freeCount;
	}

	@Override
	public final int getFreeNode(final int free) {
		return freeOffset + free;
	}

	private TIntArrayList getNeighbors(int node) {
		final TIntArrayList neighbors = new TIntArrayList(graph.outDegreeOf(node));
		for (Integer v : Graphs.neighborListOf(graph, node)) {
			neighbors.add(v);
		}
		neighbors.sort();
		return neighbors;
	}

	private void buildFreeAdjacencyLists() {
		final TIntArrayList[] adjLists = new TIntArrayList[freeCount];
		for (int i = 0; i < freeCount; i++) {
			adjLists[i] = getNeighbors(freeOffset + i);
		}
		freeAdjLists = Optional.of(adjLists);
	}

	protected TIntArrayList[] getFreeAdjacencyLists() {
		if (freeAdjLists.isEmpty()) {
			buildFreeAdjacencyLists();
		}
		return freeAdjLists.get();
	}

	@Override
	public final int getFreeDegree(final int free) {
		return graph.degreeOf(freeOffset + free);
	}

	public <E> E[] permutateMedians(final E[] objects) {
		final TIntArrayList[] adjLists = getFreeAdjacencyLists();
		return TListUtil.permutate(objects, i -> TListUtil.getMedian(adjLists[i]));
	}

	public <E> E[] permutateBarycenters(final E[] objects) {
		final TIntArrayList[] adjLists = getFreeAdjacencyLists();
		return TListUtil.permutate(objects, i -> TListUtil.getBarycenter(adjLists[i]));
	}

	protected void logOnCrossingCounts() {
		if (LOGGER.isLoggable(Level.INFO)) {
			if (LOGGER.isLoggable(Level.CONFIG)) {
				if (LOGGER.isLoggable(Level.FINE)) {
					LOGGER.log(Level.CONFIG, "Crossing Counts\nc CONSTANT {0}\n{1}\n{2}",
							new Object[] { reducedCrossingCounts.get().getConstant(),
									crossingCounts.get().getDimacsPatterns(), crossingCounts.get() });
				} else {
					LOGGER.log(Level.CONFIG, "Crossing Counts\nc CONSTANT {0}\n{1}\n", new Object[] {
							reducedCrossingCounts.get().getConstant(), crossingCounts.get().getDimacsPatterns() });
				}
			} else {
				LOGGER.log(Level.INFO, "Crossing Counts\nc CONSTANT {0}\n",
						new Object[] { reducedCrossingCounts.get().getConstant() });
			}
		}
	}

	protected void buildCrossingCounts() {
		final int n = getFreeCount();
		final int[][] counts = new int[n][n];
		final int[][] redCounts = new int[n][n];
		int constant = 0;
		final TIntArrayList[] adjLists = getFreeAdjacencyLists();
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				counts[i][j] = TListUtil.getCrossingCount(adjLists[i], adjLists[j]);
				counts[j][i] = TListUtil.getCrossingCount(adjLists[j], adjLists[i]);
				final int min = Math.min(counts[i][j], counts[j][i]);
				constant += min;
				redCounts[i][j] = counts[i][j] - min;
				redCounts[j][i] = counts[j][i] - min;
			}
		}
		this.crossingCounts = Optional.of(new CrossingCounts(counts, 0));
		this.reducedCrossingCounts = Optional.of(new CrossingCounts(redCounts, constant));
	}

	@Override
	public final CrossingCounts getReducedCrossingCounts() {
		if (reducedCrossingCounts.isEmpty()) {
			buildCrossingCounts();
			logOnCrossingCounts();
		}
		return reducedCrossingCounts.get();
	}

	@Override
	public final CrossingCounts getCrossingCounts() {
		if (crossingCounts.isEmpty()) {
			buildCrossingCounts();
			logOnCrossingCounts();
		}
		return crossingCounts.get();
	}

	@Override
	public final BlockCutpointGraph<Integer, DefaultEdge> getBlockCutGraph() {
		if (blockCutGraph.isEmpty()) {
			blockCutGraph = Optional.of(new BlockCutpointGraph<>(graph));
			LignesClaires.LOGGER.log(Level.INFO, "Block-Cut Graph\nc BLOCKS {0}\nc CUTPOINTS {1}",
					new Object[] { blockCutGraph.get().getBlocks().size(), blockCutGraph.get().getCutpoints().size() });
		}
		return blockCutGraph.get();
	}

}