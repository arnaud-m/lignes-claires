/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.parser;

public class InvalidGraphFormatException extends Exception {

	private static final long serialVersionUID = 1072678592196266096L;

	public InvalidGraphFormatException() {
		super("Invalid graph format exception");
	}

	public InvalidGraphFormatException(final String message) {
		super(message);
	}
}