package lignesclaires.bigraph;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gnu.trove.iterator.TIntIterator;

/**
 * https://en.wikipedia.org/wiki/Bridge_(graph_theory)
 * 
 * Tarjan's bridge-finding algorithm
 * 
 */
class DepthFirstSearch {

	// Input
	private UndirectedGraph graph;

	// Output
	private NodeDFS[] data;

	// Temporary
	private int root;
	private int preNum;
	private int postNum;
	private Deque<StackIterator> stack = new ArrayDeque<>();

	private void setUp(UndirectedGraph graph) {
		this.graph = graph;
		final int n = graph.getNodeCount();
		data = new NodeDFS[n];
		root = 0;
		preNum = 0;
		postNum = 0;
		stack.clear();
	}

	private boolean findRoot() {
		while (root < data.length) {
			if (data[root] == null) {
				pushNode(root, root);
				return true;
			}
			root++;
		}
		return false;
	}

	private void pushNode(int node, int parent) {
		data[node] = new NodeDFS(node, parent, preNum++);
		stack.push(new StackIterator(node, graph.getNeighborIterator(node)));
	}

	private void popNode(int node) {
		stack.pop();
		data[node].setPostorder(postNum++);
	}

	public ForestDFS execute(UndirectedGraph graph) {
		setUp(graph);
		while (findRoot()) {
			while (!stack.isEmpty()) {
				final StackIterator elt = stack.peek();
				final int node = elt.node;
				if (elt.hasNext()) {
					final int child = elt.next();
					if (data[child] == null) {
						pushNode(child, node);
					} else {
						data[node].awakeOnOutEdge(data[child]);
					}
				} else {
					popNode(node);
					data[data[node].getParent()].awakeOnInEdge(data[node]);
				}

			}
		}
		return new ForestDFS(graph, data);
	}

	private static final class StackIterator implements TIntIterator {
		public final int node;
		public final TIntIterator iter;

		public StackIterator(int index, TIntIterator iter) {
			super();
			this.node = index;
			this.iter = iter;
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public void remove() {
			iter.remove();
		}

		@Override
		public int next() {
			return iter.next();
		}

	}

	public static String toString(Object[] o) {
		return toString(o, "\n");
	}

	public static String toString(Object[] o, CharSequence delimiter) {
		return Stream.of(o).map(Object::toString).collect(Collectors.joining(delimiter));
	}

	@Override
	public String toString() {
		return "DepthFirstSearch [roots=" + "\n, data=\n" + toString(data) + ",\nordering=\n" + "\n]";
	}

}