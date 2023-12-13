/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import lignesclaires.cmd.Verbosity;

/**
 * A bean object that stores the common configuration. This is designed for
 * args4j command line, but it is not restricted to it.
 */
public class LignesClairesConfig {

	@Option(name = "-h", aliases = { "--help" }, usage = "Output a usage message and exit.")
	private boolean displayHelp;

	@Option(name = "-v", aliases = { "--verbose" }, usage = "Increase the verbosity of the program.")
	private Verbosity verbosity = Verbosity.NORMAL;

	@Option(name = "--solution", usage = "Limit the number of solutions returned by the solver.")
	private int solutionLimit;

	@Option(name = "--time", usage = "Limit the time taken by the solver (in seconds).")
	private int timeLimit;

	/**
	 * Receives other command line parameters than options.
	 */
	@Argument
	private List<String> arguments = new ArrayList<>();

	public final boolean isDisplayHelp() {
		return displayHelp;
	}

	public final Verbosity getVerbosity() {
		return verbosity;
	}

	public final void setVerbosity(final Verbosity verbosity) {
		this.verbosity = verbosity;
	}

	public final int getSolutionLimit() {
		return solutionLimit;
	}

	public final int getTimeLimit() {
		return timeLimit;
	}

	public final void setSolutionLimit(final int solutionLimit) {
		this.solutionLimit = solutionLimit;
	}

	public final void setTimeLimit(final int timeLimit) {
		this.timeLimit = timeLimit;
	}

	public final List<String> getArguments() {
		return Collections.unmodifiableList(arguments);
	}

}
