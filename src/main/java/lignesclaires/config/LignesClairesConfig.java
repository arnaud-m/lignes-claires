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
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import lignesclaires.cmd.OCModelOptionHandler;
import lignesclaires.cmd.OCSearchOptionHandler;
import lignesclaires.cmd.Verbosity;
import lignesclaires.solver.OCModelFlag;
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

	@Option(name = "-m", aliases = { "--model" }, usage = "Set the building strategy of the model.")
	private int modelMask = ~0; // Using bitwise NOT operator to set all bits to 1.

	@Option(name = "-m2", aliases = {
			"--model2" }, handler = OCModelOptionHandler.class, usage = "Set the building strategy of the model.")
	private EnumSet<OCModelFlag> modelMask2 = EnumSet.allOf(OCModelFlag.class);

	@Option(name = "-s2", aliases = {
			"--search2" }, handler = OCSearchOptionHandler.class, usage = "Set the search strategy of the solver.")
	private EnumSet<OCSearch> search2 = EnumSet.allOf(OCSearch.class);

	@Option(name = "--restart", usage = "Activate geometrical restarts.")
	private boolean withRestarts;

	@Option(name = "--heuristics", usage = "Activate heuristics.")
	private boolean withHeuristics;

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
		// System.out.println(search2);
		return search;
	}

	public final void setSearch(OCSearch search) {
		this.search = search;
	}

	public final int getModelMask() {
		// System.out.println(modelMask2);
		return modelMask;
	}

	public final void setModelMask(int modelMask) {
		this.modelMask = modelMask;
	}

	public final boolean isWithRestarts() {
		return withRestarts;
	}

	public final boolean isWithHeuristics() {
		return withHeuristics;
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

	public boolean contains(OCModelFlag flag) {
		return modelMask2.contains(flag);
	}

	public boolean contains(OCSearch flag) {
		return search2.contains(flag);
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
