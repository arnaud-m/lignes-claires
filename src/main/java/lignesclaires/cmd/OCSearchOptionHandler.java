/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.cmd;

import java.util.EnumSet;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OneArgumentOptionHandler;
import org.kohsuke.args4j.spi.Setter;

import lignesclaires.solver.OCSearchFlag;

public class OCSearchOptionHandler extends OneArgumentOptionHandler<EnumSet<OCSearchFlag>> {

	public OCSearchOptionHandler(CmdLineParser parser, OptionDef option, Setter<EnumSet<OCSearchFlag>> setter) {
		super(parser, option, setter);
	}

	@Override
	protected EnumSet<OCSearchFlag> parse(String argument) throws NumberFormatException {
		return OCModelOptionHandler.of(OCSearchFlag.class, Integer.parseInt(argument));
	}

}
