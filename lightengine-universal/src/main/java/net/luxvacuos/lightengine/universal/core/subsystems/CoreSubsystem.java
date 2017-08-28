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

package net.luxvacuos.lightengine.universal.core.subsystems;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

import net.luxvacuos.lightengine.universal.bootstrap.AbstractBootstrap;
import net.luxvacuos.lightengine.universal.core.AbstractGameSettings;
import net.luxvacuos.lightengine.universal.core.GlobalVariables;
import net.luxvacuos.lightengine.universal.util.registry.Key;
import net.luxvacuos.lightengine.universal.util.registry.LanguageRegistry;
import net.luxvacuos.lightengine.universal.util.registry.SystemRegistry;

public class CoreSubsystem implements ISubsystem {

	protected static AbstractGameSettings gameSettings;
	public static LanguageRegistry LANG;
	public static SystemRegistry REGISTRY;
	public static int ups;
	public static int upsCount;

	@Override
	public void init() {
		try {
			Manifest manifest = new Manifest(
					getClass().getClassLoader().getResourceAsStream("lightengine-universal-version.MF"));
			Attributes attr = manifest.getMainAttributes();
			String versionUniversal = attr.getValue("LV-Version");
			String branchUniversal = attr.getValue("LV-Branch");
			String buildUniversal = attr.getValue("LV-Build");
			if (versionUniversal != null)
				GlobalVariables.versionUniversal = versionUniversal;
			if (branchUniversal != null)
				GlobalVariables.branchUniversal = branchUniversal;
			if (buildUniversal != null)
				GlobalVariables.buildUniversal = Integer.getInteger(buildUniversal);
		} catch (Exception e) {
		}
		REGISTRY = new SystemRegistry();
		REGISTRY.register(new Key("/Light Engine/Settings/file"),
				AbstractBootstrap.getPrefix() + "/config/registry.json");
		REGISTRY.register(new Key("/Light Engine/System/os"),
				System.getProperty("os.name") + " " + System.getProperty("os.arch").toUpperCase());
		REGISTRY.register(new Key("/Light Engine/universalVersion"), GlobalVariables.versionUniversal + "-"
				+ GlobalVariables.branchUniversal + "-" + GlobalVariables.buildUniversal);
		LANG = new LanguageRegistry();
	}

	@Override
	public void restart() {
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void render(float delta) {
	}

	@Override
	public void dispose() {
		REGISTRY.save();
	}

	public static AbstractGameSettings getGameSettings() {
		return gameSettings;
	}

}
