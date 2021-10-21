package org.oscim.debug;

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

import java.util.regex.Pattern;

/**
 * Logger used for easier access to the {@link GlobalLogger}.
 */
public class Logger
{
	/**
	 * {@link Throwable} to use when no error was passed to the error log method.
	 */
	public static final Throwable ERROR_NOT_PASSED = new Throwable("no error passed");
	
	/**
	 * Create the message from the given format and objects.
	 * @param format format of the message where "{}" is replaced by the objects
	 * @param objects object array to fill the format "{}" placeholders with
	 * @return formatted message
	 */
	private static String createMessage(String format, Object... objects)
	{
		for (Object o : objects)
		{
			format = format.replaceFirst(Pattern.quote("{}"), String.valueOf(o));
		}
		return format;
	}
	
	/**
	 * {@link Class} reference required when using {@link GlobalLogger}
	 */
	private final Class<?> clazz;
	
	/**
	 * Create a new {@link Logger} instance.
	 * @param clazz {@link Class} that created this logger
	 */
	public Logger(Class<?> clazz)
	{
		this.clazz = clazz;
	}
	
	/**
	 * Log an info.
	 * @param message the message to log
	 */
	public void debug(String message)
	{
		GlobalLogger.debug(this.clazz, message);
	}
	
	/**
	 * Log an info.
	 * @param error the error that occurred to log
	 */
	public void debug(Throwable error)
	{
		GlobalLogger.debug(this.clazz, error.getMessage());
	}
	
	/**
	 * Log an info.
	 * @param format format of the message where "{}" is replaced by the objects
	 * @param objects object array to fill the format "{}" placeholders with
	 */
	public void debug(String format, Object... objects)
	{
		GlobalLogger.debug(this.clazz, createMessage(format, objects));
	}
	
	/**
	 * Log a warning.
	 * @param message the message to log
	 */
	public void warn(String message)
	{
		GlobalLogger.warn(this.clazz, message);
	}
	
	/**
	 * Log an error.
	 * @param message the message to log
	 * @param error the error that occurred to log
	 */
	public void error(String message, Throwable error)
	{
		GlobalLogger.error(this.clazz, message, error);
	}
	
	/**
	 * Log an error.
	 * @param error the error that occurred to log
	 */
	public void error(Throwable error)
	{
		GlobalLogger.error(this.clazz, error.getMessage(), error);
	}
	
	/**
	 * Log an error.
	 * @param format format of the message where "{}" is replaced by the objects
	 * @param objects object array to fill the format "{}" placeholders with
	 */
	public void error(String format, Object... objects)
	{
		Throwable error = ERROR_NOT_PASSED;
		for (Object o : objects)
		{
			if (o instanceof Throwable)
			{
				error = (Throwable) o;
				break;
			}
		}
		GlobalLogger.error(this.clazz, createMessage(format, objects), error);
	}
	
}
