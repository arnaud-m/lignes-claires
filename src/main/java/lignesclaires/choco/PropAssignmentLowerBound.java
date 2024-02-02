/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.choco;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import lignesclaires.specs.IBipartiteGraph;

public class PropAssignmentLowerBound extends Propagator<IntVar> {

	private final IntVar[] positions;
	private final IntVar cost;
	private final int constant;

	private final AssignmentBuilder builder;

	public PropAssignmentLowerBound(IBipartiteGraph bigraph, IntVar[] positions, IntVar cost) {
		super(ArrayUtils.concat(positions, cost), PropagatorPriority.VERY_SLOW, false);
		this.positions = positions;
		this.cost = cost;
		this.constant = bigraph.getReducedCrossingCounts().getConstant();
		builder = new AssignmentBuilder(bigraph);
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return IntEventType.boundAndInst();
	}

	static boolean isOn = true;

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		builder.buildAssignmentGraph(positions);
		final int weight = (int) Math.ceil(builder.solveAssignment().getWeight());

		final int lb = constant + weight;
		if (lb > cost.getUB()) {
			System.err.println(cost + " " + (constant + weight));
		} else if (lb > cost.getLB()) {
			System.out.println(cost + " " + (constant + weight));
		}
		if (isOn) {
			cost.updateLowerBound(constant + weight, this);
		}
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

}
