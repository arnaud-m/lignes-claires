/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.bigraph.rules;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Optional;

import org.chocosolver.solver.constraints.extension.Tuples;

import lignesclaires.specs.IBipartiteGraph;
import lignesclaires.specs.IReductionRule;

public class ReductionRules implements IReductionRule {

	private final Optional<ReductionRuleLO2> ruleLO2;
	private final IReductionRule[] rules;

	public ReductionRules(Optional<ReductionRuleLO2> ruleLO2, IReductionRule... rules) {
		super();
		this.ruleLO2 = ruleLO2;
		this.rules = rules;
	}

	@Override
	public Optional<Point> apply(final int i, final int j) {
		ruleLO2.ifPresent(x -> x.accept(i, j));
		for (IReductionRule rule : rules) {
			Optional<Point> p = rule.apply(i, j);
			if (p.isPresent()) {
				return p;
			}
		}
		return Optional.empty();
	}

	public Optional<Tuples> getTuplesLO2() {
		return ruleLO2.map(x -> x.getTuples());
	}

	public static final class Builder {

		private final IBipartiteGraph bigraph;

		private Optional<ReductionRuleLO2> ruleLO2 = Optional.empty();
		private final ArrayList<IReductionRule> rules = new ArrayList<>();

		public Builder(IBipartiteGraph bigraph) {
			super();
			this.bigraph = bigraph;
		}

		public void withReductionRule1() {
			rules.add(new ReductionRule1(bigraph));
		}

		public void withReductionRule2() {
			rules.add(new ReductionRule2(bigraph));
		}

		public void withReductionRule3() {
			rules.add(new ReductionRule3(bigraph));
		}

		public void withReductionRuleLO2() {
			ruleLO2 = Optional.of(new ReductionRuleLO2(bigraph));

		}

		public ReductionRules build() {
			return new ReductionRules(ruleLO2, rules.toArray(new IReductionRule[rules.size()]));
		}
	}
}
