/*
 * This file is part of Light Engine
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

package net.luxvacuos.lightengine.server.bootstrap;

import java.io.File;
import java.io.IOException;

import net.luxvacuos.lightengine.server.core.LightEngineServer;
import net.luxvacuos.lightengine.universal.bootstrap.AbstractBootstrap;

public class Bootstrap extends AbstractBootstrap {

	public Bootstrap(String[] args) {
		super(args);
	}

	@Override
	public void init() {
		Thread.currentThread().setName("Light Engine-Server");
		try {
			prefix = new File(".").getCanonicalPath().toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			File file = new File(new File(".").getCanonicalPath() + "/logs");
			if (!file.exists())
				file.mkdirs();
		} catch (IOException e) {
			e.printStackTrace();
		}
		new LightEngineServer();
	}

	@Override
	public void parseArgs(String[] args) {
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			default:
				if (args[i].startsWith("-")) {
					throw new IllegalArgumentException("Unknown argument: " + args[i].substring(1));
				} else {
					throw new IllegalArgumentException("Unknown token: " + args[i]);
				}
			}
		}
	}

}