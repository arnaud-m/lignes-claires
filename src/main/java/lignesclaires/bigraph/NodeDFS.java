package lignesclaires.bigraph;

public final class NodeDFS {

	private final int node;
	private final int parent;
	private final int preorder;
	private int lowest;
	private int highest;
	private int descendants;

	public NodeDFS(int node, int parent, int preorder) {
		super();
		this.node = node;
		this.parent = parent;
		this.preorder = preorder;
		this.lowest = preorder;
		this.highest = 0;
		this.descendants = 1;
	}

	public final int getNode() {
		return node;
	}

	public final int getParent() {
		return parent;
	}

	public final int getPreorder() {
		return preorder;
	}

	public final int getLowest() {
		return lowest;
	}

	public final int getHighest() {
		return highest;
	}

	public final int getDescendants() {
		return descendants;
	}

	protected void awakeOnTransitiveEdge(NodeDFS node) {
		if (this.parent != node.node) {
			System.out.println("C " + this.node + " -> " + node.node);
			if (node.preorder < lowest)
				lowest = node.preorder;
			else if (node.preorder > highest)
				highest = node.preorder;
		}
	}

	protected void awakeOnSpanningEdge(NodeDFS node) {
		if (this.node != node.node) {
			System.out.println("T " + this.node + " -> " + node.node);
			if (node.lowest < lowest)
				lowest = node.lowest;

			if (node.highest > highest)
				highest = node.highest;

			descendants += node.descendants;
		}
	}

	public boolean isBridge() {
		return lowest == preorder && highest < preorder + descendants;
	}

	@Override
	public String toString() {
		return String.format("%3d %3d %3d %3d %3d", node, preorder, lowest, highest, descendants);
	}

	public String toDotty() {
		final StringBuilder b = new StringBuilder();
		b.append(String.format("%d [label=\"{{%d|%d}|{%d|%d}}\"];", node, node, preorder, lowest, highest));
		b.append('\n');
		final String attrs = isBridge() ? " [color=firebrick]" : "";
		b.append(String.format("%d -- %d%s;", parent, node, attrs));
		return b.toString();
	}

}