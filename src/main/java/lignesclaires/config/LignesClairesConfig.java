/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import lignesclaires.cmd.Verbosity;
import lignesclaires.solver.OCSearch;

/**
 * A bean object that stores the common configuration. This is designed for
 * args4j command line, but it is not restricted to it.
 */
public class LignesClairesConfig {

	@Option(name = "-h", aliases = { "--help" }, usage = "Output a usage message and exit.")
	private boolean displayHelp;

	@Option(name = "-v", aliases = { "--verbose" }, usage = "Increase the verbosity of the program.")
	private Verbosity verbosity = Verbosity.NORMAL;

	@Option(name = "-r", aliases = { "--report" }, usage = "Report on analysis and processing of the input graph.")
	private boolean report;

	@Option(name = "-d", aliases = { "--dry-run" }, usage = "Report on analysis and processing of the input graph.")
	private boolean dryRun;

	// TODO Replace by a search mask?
	@Option(name = "-s", aliases = { "--search" }, usage = "Set the search strategy of the solver.")
	private OCSearch search = OCSearch.DEFAULT;

	@Option(name = "-m", aliases = { "--model" }, usage = "Set the search strategy of the solver.")
	private int modelMask = ~0; // Using bitwise NOT operator to set all bits to 1.

	@Option(name = "--restart", usage = "Activate geometrical restarts.")
	private boolean withRestarts;

	@Option(name = "--solution", usage = "Limit the number of solutions returned by the solver.")
	private int solutionLimit;

	@Option(name = "--time", usage = "Limit the time taken by the solver (in seconds).")
	private long timeLimit;

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

	public final boolean isReport() {
		return report;
	}

	public final void setReport(boolean report) {
		this.report = report;
	}

	public final boolean isDryRun() {
		return dryRun;
	}

	public final void setDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}

	public final OCSearch getSearch() {
		return search;
	}

	public final void setSearch(OCSearch search) {
		this.search = search;
	}

	public final int getModelMask() {
		return modelMask;
	}

	public final void setModelMask(int modelMask) {
		this.modelMask = modelMask;
	}

	public final boolean isWithRestarts() {
		return withRestarts;
	}

	public final void setWithRestarts(boolean withRestarts) {
		this.withRestarts = withRestarts;
	}

	public final int getSolutionLimit() {
		return solutionLimit;
	}

	public final long getTimeLimit() {
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

	public final String getGraphFile() {
		return arguments.get(0);
	}

	public final Optional<String> getSolutionFile() {
		return arguments.size() < 2 ? Optional.empty() : Optional.of(arguments.get(1));
	}
}
