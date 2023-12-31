package lignesclaires.bigraph;

import java.util.List;
import java.util.Optional;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public class BlockCutTree {

	private final ForestDFS forest;

	private final List<TIntArrayList> blocks;

	private Optional<TIntSet> cuts;

	public BlockCutTree(final ForestDFS forest, List<TIntArrayList> blocks) {
		super();
		this.forest = forest;
		this.blocks = blocks;
		this.cuts = Optional.empty();
	}

	public final TIntSet getCuts() {
		if (cuts.isEmpty()) {
			final TIntSet s = new TIntHashSet();
			blocks.forEach(b -> {
				final int cut = b.getQuick(0);
				if (!forest.getNode(cut).isRoot() || forest.getForest().getNeighborsCount(cut) > 1) {
					s.add(b.getQuick(0));
				}
			});
			cuts = Optional.of(s);
		}
		return cuts.get();
	}

	private void toDottyCuts(StringBuilder b) {
		b.append("{node [shape=plain]\n");
		getCuts().forEach(cut -> {
			b.append(cut).append(";");
			return true;
		});
		b.append("\n}\n");
	}

	private void toDottyBlocks(StringBuilder b) {
		TIntSet s = getCuts();
		b.append("{node [shape=box]\n");
		int idx = 1;
		for (TIntArrayList block : blocks) {
			TIntIterator iter = block.iterator();
			while (iter.hasNext()) {
				final int node = iter.next();
				if (s.contains(node)) {
					b.append(node).append(" -- b").append(idx).append(";\n");
				}
			}
			b.append("b").append(idx).append(" [label=\"").append(DepthFirstSearch.toString(block, " "))
					.append("\"];\n");
			idx++;
		}
		b.append("}\n");
	}

	public final String toDotty() {
		final StringBuilder b = new StringBuilder();
		b.append("graph G {\n");
		toDottyCuts(b);
		toDottyBlocks(b);

		b.append("}\n");
		return b.toString();
	}

	@Override
	public String toString() {
		return DepthFirstSearch.toString(blocks.stream(), "\n");
	}

}