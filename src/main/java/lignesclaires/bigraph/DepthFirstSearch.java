package lignesclaires.bigraph;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;

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
	private TIntArrayList roots;
	private NodeDFS[] data;
	private NodeDFS[] preorder;

	// Temporary
	private int root;
	private int preNum;
	private Deque<StackElt> stack = new ArrayDeque<>();

	private void setUp(UndirectedGraph graph) {
		this.graph = graph;
		roots = new TIntArrayList();
		final int n = graph.getNodeCount();
		data = new NodeDFS[n];
		preorder = new NodeDFS[n];
		root = 0;
		preNum = 0;
		stack.clear();
	}

	private boolean findRoot() {
		while (root < data.length) {
			if (data[root] == null) {
				addNode(root, root);
				roots.add(root);
				return true;
			}
			root++;
		}
		return false;
	}

	private void addNode(int node, int parent) {
		data[node] = new NodeDFS(node, parent, preNum);
		preorder[preNum] = data[node];
		stack.push(new StackElt(node, graph.getNeighborIterator(node)));
		preNum++;
	}

	public void execute(UndirectedGraph graph) {
		setUp(graph);
		while (findRoot()) {
			while (!stack.isEmpty()) {
				final StackElt elt = stack.peek();
				final int node = elt.node;
				if (elt.iter.hasNext()) {
					final int child = elt.iter.next();
					if (data[child] == null) {
						addNode(child, node);
					} else {
						data[node].awakeOnTransitiveEdge(data[child]);
					}
				} else {
					stack.pop();
					final int parent = data[node].getParent();
					data[parent].awakeOnSpanningEdge(data[node]);
				}

			}
		}
	}

	public boolean isSpanningEdge(int i, int j) {
		return i == data[j].getParent() || j == data[i].getParent();
	}

	public String toDotty() {
		String spanningForest = Stream.of(data).map(NodeDFS::toDotty).collect(Collectors.joining("\n"));
		StringBuilder graphb = new StringBuilder();
		graphb.append("graph G{\n");
		graphb.append("{\nnode[shape=record];\nedge[style=bold];\n");
		graphb.append(spanningForest);
		graphb.append("\n}\n");
		graphb.append("{\nedge[style=dashed];\n");
		graph.forEachEdge((i, j) -> {
			if (!isSpanningEdge(i, j)) {
				graphb.append(i).append(" -- ").append(j).append(";\n");
			}
		});
		graphb.append("}\n");

		graphb.append("}\n");
		return graphb.toString();
	}

	public static class StackElt {
		public final int node;
		public final TIntIterator iter;

		public StackElt(int index, TIntIterator iter) {
			super();
			this.node = index;
			this.iter = iter;
		}
	}

	private static String toString(Object[] o) {
		return Stream.of(o).map(Object::toString).collect(Collectors.joining("\n"));
	}

	@Override
	public String toString() {
		return "DepthFirstSearch [roots=" + roots + "\n, data=\n" + toString(data) + ",\nordering=\n"
				+ toString(preorder) + "\n]";
	}

}