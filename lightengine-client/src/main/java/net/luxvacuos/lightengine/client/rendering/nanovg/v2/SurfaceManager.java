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

package net.luxvacuos.lightengine.client.rendering.nanovg.v2;

import static org.lwjgl.nanovg.NanoVG.nvgBeginFrame;
import static org.lwjgl.nanovg.NanoVG.nvgCancelFrame;
import static org.lwjgl.nanovg.NanoVG.nvgEndFrame;

import org.joml.Vector4f;

import net.luxvacuos.lightengine.client.rendering.glfw.Window;

public class SurfaceManager {

	private RootSurface rootSurface;
	private Vector4f rootSize;
	private Window window;
	private long ctx;

	public SurfaceManager(Window window) {
		this.window = window;
		this.ctx = window.getNVGID();
		rootSurface = new RootSurface();
		rootSurface.init(ctx, window.getMouseHandler(), window.getKeyboardHandler());
		rootSurface.setHorizontalAlignment(Alignment.STRETCH).setVerticalAlignment(Alignment.STRETCH);
		rootSize = new Vector4f(0, 0, window.getWidth(), window.getHeight());
	}

	public void render(float delta) {
		window.resetViewport();

		nvgBeginFrame(ctx, window.getWidth(), window.getHeight(), 1.0f);
		rootSurface.preLayout(delta);
		nvgCancelFrame(ctx);

		rootSurface.updateLayout(rootSize);

		nvgBeginFrame(ctx, window.getWidth(), window.getHeight(), 1.0f);
		rootSurface.render(delta);
		nvgEndFrame(ctx);
	}

	public void update(float delta) {
		rootSize.set(0, 0, window.getWidth(), window.getHeight());
		rootSurface.handleInputI();
		rootSurface.update(delta);
	}

	public void dispose() {
		rootSurface.dispose();
	}

	public void addSurface(Surface srf) {
		rootSurface.addSurface(srf);
	}

	public void removeSurface(Surface srf) {
		rootSurface.removeSurface(srf);
	}
}
