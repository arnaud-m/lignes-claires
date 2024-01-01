/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.specs;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;

/**
 * Getter interface of a Choco model.
 */
public interface IChocoModel {

    /**
     * Gets the model.
     *
     * @return the model
     */
    Model getModel();

    /**
     * Gets the solver.
     *
     * @return the solver
     */
    default Solver getSolver() {
        return getModel().getSolver();
    }

}
