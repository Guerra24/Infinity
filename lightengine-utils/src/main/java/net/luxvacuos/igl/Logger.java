/*
 * This file is part of Infinity Game Library
 * 
 * Copyright (C) 2016-2017 Lux Vacuos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.luxvacuos.igl;

/**
 * Logger
 * 
 * @author Guerra24 <pablo230699@hotmail.com>
 */
public final class Logger {

	private static org.apache.log4j.Logger log;

	public static void init() {
		log = org.apache.log4j.Logger.getLogger("Light Engine");
	}

	/**
	 * Prints to the Log
	 * 
	 * @param thread
	 *            Thread
	 * @param messages
	 *            Message
	 */
	public static void log(Object... messages) {
		if(log == null) return;
		for (Object object : messages)
			log.info(object);

	}

	/**
	 * Prints a Warning Message
	 * 
	 * @param thread
	 *            Thread
	 * @param messages
	 *            Message
	 */
	public static void warn(Object... messages) {
		if(log == null) return;
		for (Object object : messages)
			log.error(object);
	}

	/**
	 * Prints a Fatal Error
	 * 
	 * @param thread
	 *            Thread
	 * @param messages
	 *            Message
	 */
	public static void error(Object... messages) {
		if(log == null) return;
		for (Object object : messages)
			log.fatal(object);
	}
}