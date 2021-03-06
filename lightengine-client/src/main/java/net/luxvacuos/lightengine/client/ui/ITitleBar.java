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

package net.luxvacuos.lightengine.client.ui;

import net.luxvacuos.lightengine.client.rendering.glfw.Window;
import net.luxvacuos.lightengine.client.rendering.nanovg.Event;
import net.luxvacuos.lightengine.universal.resources.IDisposable;

public interface ITitleBar extends IDisposable {

	public void init(Window window);

	public void render(Window window);

	public void update(float delta, Window window);

	public void alwaysUpdate(float delta, Window window);

	public RootComponent getLeft();

	public RootComponent getRight();

	public RootComponent getCenter();

	public void setOnDrag(Event event);

	public boolean isEnabled();

	public boolean isDragging();

	public void setEnabled(boolean enabled);

}
