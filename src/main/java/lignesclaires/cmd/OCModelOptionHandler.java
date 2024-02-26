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

import lignesclaires.solver.OCModelFlag;

public class OCModelOptionHandler extends OneArgumentOptionHandler<EnumSet<OCModelFlag>> {

	public OCModelOptionHandler(CmdLineParser parser, OptionDef option, Setter<EnumSet<OCModelFlag>> setter) {
		super(parser, option, setter);
	}

	@Override
	protected EnumSet<OCModelFlag> parse(String argument) throws NumberFormatException {
		return of(OCModelFlag.class, Integer.parseInt(argument));
	}

	public static <T extends Enum<T>> EnumSet<T> of(Class<T> clazz, final int mask) {
		EnumSet<T> set = EnumSet.noneOf(clazz);
		int flag = 1;
		for (T v : clazz.getEnumConstants()) {
			if ((flag & mask) == flag) {
				set.add(v);
			}
			flag *= 2;
		}
		return set;
	}

	public static <T> int order(Class<T> clazz) {
		final int n = clazz.getEnumConstants().length;
		return (1 << n) - 1;
	}
}
