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
import java.util.function.Consumer;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import lignesclaires.ToStringUtil;
import lignesclaires.cmd.OCModelOptionHandler;
import lignesclaires.cmd.OCSearchOptionHandler;
import lignesclaires.cmd.Verbosity;
import lignesclaires.solver.OCModelFlag;
import lignesclaires.solver.OCSearchFlag;

/**
 * A bean object that stores the common configuration. This is designed for
 * args4j command line, but it is not restricted to it.
 */
public class LignesClairesConfig {

	@Option(name = "-h", aliases = { "--help" }, usage = "Output a usage message and exit.")
	private boolean displayHelp;

	@Option(name = "-v", aliases = { "--verbose" }, usage = "Increase the verbosity of the program.")
	private Verbosity verbosity = Verbosity.NORMAL;

	@Option(name = "-e", aliases = { "--export" }, usage = "Export analysis and processing of the input graph.")
	private boolean report;

	@Option(name = "-d", aliases = { "--dry-run" }, usage = "Report on analysis and processing of the input graph.")
	private boolean dryRun;

	@Option(name = "-m", aliases = {
			"--model" }, handler = OCModelOptionHandler.class, usage = "Set the building strategy of the model.")
	private EnumSet<OCModelFlag> modelMask = EnumSet.allOf(OCModelFlag.class);

	@Option(name = "-s", aliases = {
			"--search" }, handler = OCSearchOptionHandler.class, usage = "Set the search strategy of the solver.")
	private EnumSet<OCSearchFlag> searchMask = EnumSet.allOf(OCSearchFlag.class);

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

	public final void report(Consumer<String> reporter) {
		if (isReport()) {
			reporter.accept(getGraphFileWithoutExt());
		}
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

	public final int getSolutionLimit() {
		return solutionLimit;
	}

	public final long getTimeLimit() {
		return timeLimit;
	}

	public boolean contains(OCModelFlag flag) {
		return modelMask.contains(flag);
	}

	public boolean contains(OCSearchFlag flag) {
		return searchMask.contains(flag);
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

	public final String getGraphFileWithoutExt() {
		return ToStringUtil.getFilenameWithoutExtension(arguments.get(0));
	}

	public final Optional<String> getSolutionFile() {
		return arguments.size() < 2 ? Optional.empty() : Optional.of(arguments.get(1));
	}

	public void setModelMask(int mask) {
		modelMask = OCModelOptionHandler.of(OCModelFlag.class, mask);
	}

	public void setSearchMask(int mask) {
		searchMask = OCModelOptionHandler.of(OCSearchFlag.class, mask);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("c MODEL_FLAGS ").append(modelMask);
		b.append("\nc SEARCH_FLAGS ").append(searchMask);
		return b.toString();
	}

}
