/*
 * This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
 *
 * Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package lignesclaires;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import lignesclaires.choco.ChocoLogger;

public final class JULogUtil {

	private static final String PROPERTIES = "logging.properties";

	private JULogUtil() {
	}

	public static void readResourceConfigurationLoggers(final String resourcePath) {
		final InputStream stream = LignesClaires.class.getClassLoader().getResourceAsStream(resourcePath);
		try {
			LogManager.getLogManager().readConfiguration(stream);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void configureDefaultLoggers() {
		readResourceConfigurationLoggers(PROPERTIES);
	}

	public static void configureTestLoggers() {
		readResourceConfigurationLoggers(PROPERTIES);
		configureLoggers(Level.WARNING);
	}

	public static void configureSilentLoggers() {
		readResourceConfigurationLoggers(PROPERTIES);
		configureLoggers(Level.OFF);
	}

	public static void configureLoggers(final Level level) {
		setLevel(level, LignesClaires.LOGGER, ChocoLogger.LOGGER);
	}

	public static void setLevel(final Level level, final Logger... loggers) {
		for (Logger logger : loggers) {
			logger.setLevel(level);
		}
	}

	public static void flushLogs(final Logger logger) {
		logger.log(Level.FINEST, "Flush logger {0}", logger.getName());
		for (Handler handler : logger.getHandlers()) {
			handler.flush();
		}
	}

	public static void flushLogs() {
		final LogManager manager = LogManager.getLogManager();
		final Enumeration<String> names = manager.getLoggerNames();
		final String pkg = JULogUtil.class.getPackage().getName();
		while (names.hasMoreElements()) {
			final String name = names.nextElement();
			if (name.startsWith(pkg)) {
				flushLogs(manager.getLogger(name));
			}
		}
	}

}
