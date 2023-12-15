/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2023, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class OutputHandler extends StreamHandler {

	private static class OutputFormat extends Formatter {

		@Override
		public String format(LogRecord rec) {
			return formatMessage(rec);
		}
	}

	public OutputHandler() {
		setOutputStream(System.out);
		setLevel(Level.ALL); // Handlers should not filter, loggers should
		setFormatter(new OutputFormat());
	}

	@Override
	public synchronized void publish(final LogRecord logRecord) {
		super.publish(logRecord);
		flush();
	}

	@Override
	public synchronized void close() {
		flush();
		super.close();
	}
}
