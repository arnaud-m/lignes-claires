package lignesclaires.bigraph;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ForestDFS {

	private final UndirectedGraph graph;
	private final NodeDFS[] data;

	private Optional<NodeDFS[]> preorder;
	private Optional<NodeDFS[]> postorder;

	private Optional<NodeDFS[]> roots;

	public ForestDFS(UndirectedGraph graph, NodeDFS[] dataDFS) {
		super();
		this.graph = graph;
		this.data = dataDFS;
		this.preorder = Optional.empty();
		this.postorder = Optional.empty();
	}

	public final UndirectedGraph getGraph() {
		return graph;
	}

	public final NodeDFS[] getData() {
		return data;
	}

	public final NodeDFS[] getPreorder() {
		if (preorder.isEmpty()) {
			final NodeDFS[] pre = new NodeDFS[data.length];
			for (NodeDFS n : data) {
				pre[n.getPreorder()] = n;
			}
			preorder = Optional.of(pre);
		}
		return preorder.get();
	}

	public final NodeDFS[] getPostorder() {
		if (postorder.isEmpty()) {
			final NodeDFS[] post = new NodeDFS[data.length];
			for (NodeDFS n : data) {
				post[n.getPostorder()] = n;
			}
			postorder = Optional.of(post);
		}
		return postorder.get();
	}

	public final NodeDFS[] getRoots() {
		if (roots.isEmpty()) {
			NodeDFS[] r = Stream.of(data).filter(NodeDFS::isRoot).toArray(n -> new NodeDFS[n]);
			roots = Optional.of(r);
		}
		return roots.get();
	}

	public final boolean isIn(int i, int j) {
		return i == data[j].getParent() || j == data[i].getParent();
	}

	private void toDottyIn(StringBuilder b) {
		String spanningForest = Stream.of(data).map(NodeDFS::toDotty).collect(Collectors.joining("\n"));
		b.append("{\nnode[shape=record];\nedge[style=bold];\n");
		b.append(spanningForest);
		b.append("\n}\n");
	}

	private void toDottyOut(StringBuilder b) {
		b.append("{\nedge[style=dashed];\n");
		graph.forEachEdge((i, j) -> {
			if (!isIn(i, j)) {
				b.append(i).append(" -- ").append(j).append(";\n");
			}
		});
		b.append("}\n");

	}

	public String toDotty() {
		StringBuilder b = new StringBuilder();
		b.append("graph G{\n");
		toDottyIn(b);
		toDottyOut(b);
		b.append("}\n");
		return b.toString();
	}

	public String toString() {
		return DepthFirstSearch.toString(data);
	}
}