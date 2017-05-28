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

package net.luxvacuos.lightengine.universal.core.states;

import net.luxvacuos.lightengine.universal.core.AbstractVoxel;

public interface IState {
	
	public void init();
	
	public void start();
	
	public boolean isRunning();
	
	public void update(AbstractVoxel lightengine, float delta);
	
	public void render(AbstractVoxel lightengine, float alpha);
	
	public void end();
	
	public String getName();

}