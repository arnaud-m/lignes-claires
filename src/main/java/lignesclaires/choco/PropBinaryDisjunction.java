/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.choco;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

public final class PropBinaryDisjunction extends Propagator<IntVar> {

	private final IntVar x;
	private final IntVar y;
	private final IntVar b;

	public PropBinaryDisjunction(IntVar[] vars) {
		super(vars, PropagatorPriority.BINARY, true);
		this.x = vars[0];
		this.y = vars[1];
		this.b = vars[2];
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return (vIdx < 2) ? IntEventType.boundAndInst() : IntEventType.instantiation();
	}

	private final void updateBounds(IntVar left, IntVar right) throws ContradictionException {
		left.updateUpperBound(right.getUB() - 1, this);
		right.updateLowerBound(left.getLB() + 1, this);
	}

	private final void updateBounds() throws ContradictionException {
		if (b.isInstantiatedTo(0)) {
			updateBounds(x, y);
		} else {
			updateBounds(y, x);
		}
	}

	private final void updateBinary() throws ContradictionException {
		if (x.getUB() <= y.getLB()) {
			b.instantiateTo(0, this);
		} else if (y.getUB() <= x.getLB()) {
			b.removeValue(0, this);
		}
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		if (b.isInstantiated()) {
			updateBounds();
		} else {
			updateBinary();
		}
	}

	@Override
	public void propagate(int varIdx, int mask) throws ContradictionException {
		propagate(mask);
	}

	private static final ESat isEntailed(IntVar left, IntVar right) {
		if (left.getUB() < right.getLB())
			return ESat.TRUE;
		else if (left.getLB() > right.getUB())
			return ESat.FALSE;
		else
			return ESat.UNDEFINED;
	}

	@Override
	public ESat isEntailed() {
		if (b.isInstantiated()) {
			return b.isInstantiatedTo(0) ? isEntailed(x, y)
					: isEntailed(y, x);
		} else {
			if (x.isInstantiated() && y.isInstantiated()
					&& x.getValue() == y.getValue()) {
				return ESat.FALSE;
			} else
				return ESat.UNDEFINED;
		}
	}

	@Override
	public void explain(int p, ExplanationForSignedClause explanation) {
		// IntIterableRangeSet set0, set1;
		// if (explanation.readVar(p) == vars[0]) { // case a. (see javadoc)
		// set1 = explanation.complement(vars[1]);
		// set0 = explanation.domain(vars[1]);
		// set0.times(-1);
		// set0.plus(cste);
		// vars[0].intersectLit(set0, explanation);
		// vars[1].unionLit(set1, explanation);
		// } else { // case b. (see javadoc)
		// assert explanation.readVar(p) == vars[1];
		// set0 = explanation.complement(vars[0]);
		// set1 = explanation.domain(vars[0]);
		// set1.times(-1);
		// set1.plus(cste);
		// vars[0].unionLit(set0, explanation);
		// vars[1].intersectLit(set1, explanation);
		// }
	}

	@Override
	public String toString() {
		return "(" + x + " < " + y + ") == " + b;
	}

	public static void main(String[] args) {
		Model m = new Model();
		final int n = 3;
		IntVar x = m.intVar("x", 0, n);
		IntVar y = m.intVar("y", 0, n);
		IntVar b = m.intVar("b", new int[] { 0, n });

		Constraint c = new Constraint("MyConstraint", new PropBinaryDisjunction(new IntVar[] { x, y, b }));
		m.post(c);

		System.out.println(m.getSolver().findAllSolutions());
	}
}
