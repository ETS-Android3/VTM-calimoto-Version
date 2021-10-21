package org.oscim.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/*
 * Copyright 2018 calimoto GmbH (Luca Osten)
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Global logger for custom debugging.
 */
public abstract class GlobalLogger
{
	/**
	 * The global {@link GlobalLogger} instance that can be set for manual debug logging.
	 */
	private static GlobalLogger LOGGER = null;
	
	/**
	 * {@link Map} of {@link Class} to its according {@link Logger} instance.
	 */
	private static Map<Class<?>, Logger> MAPPING_CLASS_TO_LOGGER = new HashMap<>();
	
	/**
	 * Initialize the global {@link GlobalLogger} instance.
	 * @param logger global {@link GlobalLogger} to use
	 */
	public static void initialize(GlobalLogger logger)
	{
		GlobalLogger.LOGGER = logger;
	}
	
	/**
	 * Log an info.
	 * {@link #initialize(GlobalLogger)}.
	 * @param classOrigin {@link Class} from where the info is being logged
	 * @param message the message to log
	 */
	static void debug(Class<?> classOrigin, String message)
	{
		Logger logger = MAPPING_CLASS_TO_LOGGER.get(classOrigin);
		if (logger == null)
		{
			logger = LoggerFactory.getLogger(classOrigin);
			MAPPING_CLASS_TO_LOGGER.put(classOrigin, logger);
		}
		logger.debug(message);
		
		GlobalLogger globalLogger = GlobalLogger.LOGGER;
		if (globalLogger != null)
		{
			globalLogger.debugImpl(message);
		}
	}
	
	/**
	 * Log a warning.
	 * {@link #initialize(GlobalLogger)}.
	 * @param classOrigin {@link Class} from where the info is being logged
	 * @param message the message to log
	 */
	static void warn(Class<?> classOrigin, String message)
	{
		Logger logger = MAPPING_CLASS_TO_LOGGER.get(classOrigin);
		if (logger == null)
		{
			logger = LoggerFactory.getLogger(classOrigin);
			MAPPING_CLASS_TO_LOGGER.put(classOrigin, logger);
		}
		logger.warn(message);
		
		GlobalLogger globalLogger = GlobalLogger.LOGGER;
		if (globalLogger != null)
		{
			globalLogger.debugImpl(message);
		}
	}
	
	/**
	 * Log an error.
	 * {@link #initialize(GlobalLogger)}.
	 * @param classOrigin {@link Class} from where the error is being logged
	 * @param message the message to log
	 * @param error the error that occurred to log
	 */
	static void error(Class<?> classOrigin, String message, Throwable error)
	{
		Logger logger = MAPPING_CLASS_TO_LOGGER.get(classOrigin);
		if (logger == null)
		{
			logger = LoggerFactory.getLogger(classOrigin);
			MAPPING_CLASS_TO_LOGGER.put(classOrigin, logger);
		}
		logger.error(message, error);
		
		GlobalLogger globalLogger = GlobalLogger.LOGGER;
		if (globalLogger != null)
		{
			globalLogger.errorImpl(message, error);
		}
	}
	
	/**
	 * Log an info.
	 * @param message the message to log
	 */
	protected abstract void debugImpl(String message);
	
	/**
	 * Log a warning.
	 * @param message the message to log
	 */
	protected abstract void warnImpl(String message);
	
	/**
	 * Log an error.
	 * @param message the message to log
	 * @param error the error that occurred to log
	 */
	protected abstract void errorImpl(String message, Throwable error);
	
}
