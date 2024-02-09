/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.Graphs;
import org.jgrapht.util.CollectionUtil;

/**
 * Collection of methods which provide numerical graph information.
 *
 * @see org.jgrapht.GraphMetrics
 * 
 */
public abstract class GraphTriangles {

	public interface ITriangleConsumer<V> {

		void accept(V i, V j, V k);
	}

	/**
	 * An $O(|V|^3)$ (assuming vertexSubset provides constant time indexing) naive
	 * implementation for counting non-trivial triangles in an undirected graph
	 * induced by the subset of vertices.
	 *
	 * @param graph        the input graph
	 * @param vertexSubset the vertex subset
	 * @param <V>          the graph vertex type
	 * @param <E>          the graph edge type
	 * @return the number of triangles in the graph induced by vertexSubset
	 */
	static <V, E> void naiveForEachTriangle(Graph<V, E> graph, List<V> vertexSubset, ITriangleConsumer<V> consumer) {

		for (int i = 0; i < vertexSubset.size(); i++) {
			for (int j = i + 1; j < vertexSubset.size(); j++) {
				for (int k = j + 1; k < vertexSubset.size(); k++) {
					V u = vertexSubset.get(i);
					V v = vertexSubset.get(j);
					V w = vertexSubset.get(k);

					if (graph.containsEdge(u, v) && graph.containsEdge(v, w) && graph.containsEdge(w, u)) {
						consumer.accept(u, v, w);
					}
				}
			}
		}
	}

	/**
	 * An $O(|E|^{3/2})$ algorithm for counting the number of non-trivial triangles
	 * in an undirected graph. A non-trivial triangle is formed by three distinct
	 * vertices all connected to each other.
	 *
	 * <p>
	 * For more details of this algorithm see Ullman, Jeffrey: "Mining of Massive
	 * Datasets", Cambridge University Press, Chapter 10
	 *
	 * @param graph the input graph
	 * @param <V>   the graph vertex type
	 * @param <E>   the graph edge type
	 * @return the number of triangles in the graph
	 * @throws NullPointerException     if {@code graph} is {@code null}
	 * @throws IllegalArgumentException if {@code graph} is not undirected
	 */
	public static <V, E> void forEachTriangle(Graph<V, E> graph, ITriangleConsumer<V> consumer) {
		GraphTests.requireUndirected(graph);

		final int sqrtV = (int) Math.sqrt(graph.vertexSet().size());

		List<V> vertexList = new ArrayList<>(graph.vertexSet());

		/*
		 * The book suggest the following comparator: "Compare vertices based on their
		 * degree. If equal compare them of their actual value, since they are all
		 * integers".
		 */

		// Fix vertex order for unique comparison of vertices
		Map<V, Integer> vertexOrder = CollectionUtil.newHashMapWithExpectedSize(graph.vertexSet().size());
		int k = 0;
		for (V v : graph.vertexSet()) {
			vertexOrder.put(v, k++);
		}

		Comparator<V> comparator = Comparator.comparingInt(graph::degreeOf).thenComparingInt(System::identityHashCode)
				.thenComparingInt(vertexOrder::get);

		vertexList.sort(comparator);

		// vertex v is a heavy-hitter iff degree(v) >= sqrtV
		List<V> heavyHitterVertices = vertexList.stream().filter(x -> graph.degreeOf(x) >= sqrtV)
				.collect(Collectors.toCollection(ArrayList::new));

		// count the number of triangles formed from only heavy-hitter vertices
		naiveForEachTriangle(graph, heavyHitterVertices, consumer);

		for (E edge : graph.edgeSet()) {
			V v1 = graph.getEdgeSource(edge);
			V v2 = graph.getEdgeTarget(edge);

			if (v1 == v2) {
				continue;
			}

			if (graph.degreeOf(v1) < sqrtV || graph.degreeOf(v2) < sqrtV) {
				// ensure that v1 <= v2 (swap them otherwise)
				if (comparator.compare(v1, v2) > 0) {
					V tmp = v1;
					v1 = v2;
					v2 = tmp;
				}

				for (E e : graph.edgesOf(v1)) {
					V u = Graphs.getOppositeVertex(graph, e, v1);

					// check if the triangle is non-trivial: u, v1, v2 are distinct vertices
					if (u == v1 || u == v2) {
						continue;
					}

					/*
					 * Check if v2 <= u and if (u, v2) is a valid edge. If both of them are true,
					 * then we have a new triangle (v1, v2, u) and all three vertices in the
					 * triangle are ordered (v1 <= v2 <= u) so we count it only once.
					 */
					if (comparator.compare(v2, u) <= 0 && graph.containsEdge(u, v2)) {
						consumer.accept(v1, v2, u);
					}
				}
			}
		}
	}
}
