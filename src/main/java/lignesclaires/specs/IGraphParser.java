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
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.jgrapht.nio.ImportException;

public interface IGraphParser<E extends IGraph> {

	E parse(Reader reader) throws ImportException, FileNotFoundException;

	default E parse(InputStream instream) throws ImportException, FileNotFoundException {
		return parse(new InputStreamReader(instream));
	}

	default E parse(File file) throws ImportException, FileNotFoundException {
		return parse(new FileReader(file));
	}

	default E parse(String filepath) throws ImportException, FileNotFoundException {
		return parse(new File(filepath));
	}

}
