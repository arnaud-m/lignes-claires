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
	public void propagate(final int varIdx, final int mask) throws ContradictionException {
		propagate(mask);
	}

	private static ESat isEntailed(final IntVar left, final IntVar right) {
		if (left.getUB() < right.getLB()) {
			return ESat.TRUE;
		} else if (left.getLB() > right.getUB()) {
			return ESat.FALSE;
		} else {
			return ESat.UNDEFINED;
		}
	}

	@Override
	public ESat isEntailed() {
		if (b.isInstantiated()) {
			return b.isInstantiatedTo(0) ? isEntailed(x, y) : isEntailed(y, x);
		} else {
			if (x.isInstantiated() && y.isInstantiated() && x.getValue() == y.getValue()) {
				return ESat.FALSE;
			} else
				return ESat.UNDEFINED;
		}
	}

	@Override
	public void explain(final int p, final ExplanationForSignedClause explanation) {
	}

	@Override
	public String toString() {
		return "(" + x + " < " + y + ") == " + b;
	}

}
