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

package net.luxvacuos.lightengine.server.core;

import static net.luxvacuos.lightengine.universal.core.subsystems.CoreSubsystem.REGISTRY;

import net.luxvacuos.igl.Logger;
import net.luxvacuos.lightengine.server.bootstrap.Bootstrap;
import net.luxvacuos.lightengine.server.core.states.MPWorldState;
import net.luxvacuos.lightengine.server.core.states.StateNames;
import net.luxvacuos.lightengine.server.core.subsystems.ServerCoreSubsystem;
import net.luxvacuos.lightengine.universal.core.AbstractVoxel;
import net.luxvacuos.lightengine.universal.core.EngineType;
import net.luxvacuos.lightengine.universal.core.GlobalVariables;
import net.luxvacuos.lightengine.universal.core.TaskManager;
import net.luxvacuos.lightengine.universal.core.states.StateMachine;
import net.luxvacuos.lightengine.universal.core.subsystems.CoreSubsystem;
import net.luxvacuos.lightengine.universal.util.registry.Key;

public class LightEngineServer extends AbstractVoxel {

	private double lastLoopTime;
	private float timeCount;
	private Sync sync;

	public LightEngineServer() {
		super.engineType = EngineType.SERVER;
		init();
	}

	@Override
	public void init() {
		Logger.init();
		Logger.log("Starting Server");

		super.addSubsystem(new ServerCoreSubsystem());

		super.initSubsystems();

		Logger.log("Light Engine Server Version: " + GlobalVariables.version);
		Logger.log("Running on: " + Bootstrap.getPlatform());
		
		StateMachine.registerState(new MPWorldState());
		sync = new Sync();
		lastLoopTime = System.currentTimeMillis() / 1000l;
		StateMachine.setCurrentState(StateNames.MP_WORLD);
		try {
			StateMachine.run();
			update();
			dispose();
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			handleError(t);
		}
	}

	@Override
	public void update() {
		int ups = (int) REGISTRY.getRegistryItem(new Key("/Light Engine/Settings/Core/ups"));
		float delta = 0;
		float accumulator = 0f;
		float interval = 1f / ups;
		while (StateMachine.isRunning()) {
			TaskManager.update();
			if (timeCount > 1f) {
				CoreSubsystem.ups = CoreSubsystem.upsCount;
				CoreSubsystem.upsCount = 0;
				timeCount--;
			}
			delta = getDelta();
			accumulator += delta;
			while (accumulator >= interval) {
				StateMachine.update(this, interval);
				CoreSubsystem.upsCount++;
				accumulator -= interval;
			}
			sync.sync(ups);
		}
	}

	@Override
	public void handleError(Throwable e) {
		e.printStackTrace();
		dispose();
	}

	@Override
	public void dispose() {
		super.dispose();
		Logger.log("Cleaning Resources");
		StateMachine.dispose();
	}

	public float getDelta() {
		double time = System.currentTimeMillis() / 1000l;
		float delta = (float) (time - this.lastLoopTime);
		this.lastLoopTime = time;
		this.timeCount += delta;
		return delta;
	}

}