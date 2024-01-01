/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires.specs;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import lignesclaires.parser.InvalidGraphFormatException;

public interface IBipartiteGraphParser<E extends IBipartiteGraph> {

	E parse(Scanner scanner) throws InvalidGraphFormatException;

	default E parse(File file) throws FileNotFoundException, InvalidGraphFormatException {
		return parse(new Scanner(file));
	}

	default E parse(String filepath) throws FileNotFoundException, InvalidGraphFormatException {
		return parse(new File(filepath));
	}

}
