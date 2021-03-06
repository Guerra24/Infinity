/*
 * This file is part of Light Engine
 * 
 * Copyright (C) 2016-2019 Lux Vacuos
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

package net.luxvacuos.lightengine.client.loader;

import net.luxvacuos.lightengine.client.core.ClientEngine;
import net.luxvacuos.lightengine.client.core.ClientTaskManager;
import net.luxvacuos.lightengine.universal.core.IEngineLoader;
import net.luxvacuos.lightengine.universal.core.TaskManager;
import net.luxvacuos.lightengine.universal.loader.EngineData;
import net.luxvacuos.lightengine.universal.loader.Loader;
import net.luxvacuos.lightengine.universal.loader.Platform;

public class ClientLoader extends Loader {

	public ClientLoader(IEngineLoader el, String... args) {
		super(el, args);
	}
	
	@Override
	protected void findUserDir(EngineData ed) {
		String prefix = "";
		if (ed.platform == Platform.WINDOWS_32 || ed.platform == Platform.WINDOWS_64)
			prefix = System.getenv("AppData");
		else if (ed.platform == Platform.LINUX_32 || ed.platform == Platform.LINUX_64)
			prefix = System.getProperty("user.home");
		else if (ed.platform == Platform.MACOS) {
			prefix = System.getProperty("user.home");
			prefix += "/Library/Application Support";
		} else if (ed.platform == Platform.UNKNOWN) {
			System.err.println("Loader - Unknown OS, assuming Unix-like system...");
			prefix = System.getProperty("user.home");
		}
		prefix += "/." + ed.project;
		ed.userDir = prefix;
	}

	@Override
	protected void startEngine(IEngineLoader el, EngineData ed) {
		TaskManager.tm = new ClientTaskManager();
		new ClientEngine(el, ed);
	}

}
