/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.choco;

import java.util.Formatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;

import lignesclaires.solver.OCModel;
import lignesclaires.solver.Status;
import lignesclaires.specs.IChocoModel;

public final class ChocoLogger {

	public static final Logger LOGGER = Logger.getLogger(ChocoLogger.class.getName());

	private ChocoLogger() {
		super();
	}

	public static void logOnModel(final IChocoModel m) {
		logOnModel(m.getModel());
	}

	public static void logOnModel(final Model model) {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.log(Level.INFO, "Model diagnostics:\n{0}", toDimacs(model));
			LOGGER.log(Level.FINER, "Display model:{0}", model);
		}
	}

	public static void logOnSolution(final OCModel model, final Solution solution) {
		LOGGER.log(Level.FINE, "Display solution #{0}:\no {1}\n{2}",
				new Object[] { model.getSolver().getSolutionCount(),
						model.getSolver().getObjectiveManager().getBestSolutionValue(),
						model.printSolution(solution) });
	}

	public static void logOnBestSolution(final OCModel model, final Solution solution) {
		LOGGER.log(Level.CONFIG, "Display Best Solution #{0}:\n{1}",
				new Object[] { model.getSolver().getSolutionCount(), model.printSolution(solution) });
	}

	public static void logOnSolver(final IChocoModel m) {
		logOnSolver(m.getSolver());
	}

	public static void logOnSolver(final Solver solver) {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.log(Level.INFO, "Solver diagnostics:\n{0}", toDimacs(solver));
		}
	}

	public static String toDimacs(final Model model) {
		final StringBuilder b = new StringBuilder();
		Formatter fmt = new Formatter(b, Locale.US);
		fmt.format("c MODEL_NAME %s", model.getName());
		fmt.format("%nd VARIABLES %d", model.getNbVars());
		fmt.format("%nd CONSTRAINTS %d", model.getNbCstrs());
		fmt.format("%nd BOOL_VARS %d", model.getNbBoolVar());
		fmt.format("%nd INT_VARS %d", model.getNbIntVar(false));
		fmt.close();
		return b.toString();
	}

	public static String toDimacs(final Solver s) {
		final StringBuilder b = new StringBuilder(256);
		Formatter fmt = new Formatter(b, Locale.US);
		fmt.format("s %s", Status.getStatus(s));
		if (s.hasObjective()) {
			fmt.format("%no %d", s.getBoundsManager().getBestSolutionValue().intValue());
		}
		fmt.format(
				"%nd NBSOLS %d%nd TIME %.3f%nd TIME_BEST %.3f%nd NODES %d%nd BACKTRACKS %d%nd BACKJUMPS %d%nd FAILURES %d%nd RESTARTS %d%nd STATUS %s",
				s.getSolutionCount(), s.getTimeCount(), s.getTimeToBestSolution(), s.getNodeCount(),
				s.getBackTrackCount(), s.getBackjumpCount(), s.getFailCount(), s.getRestartCount(), s.getSearchState());
		fmt.close();
		return b.toString();
	}
}
