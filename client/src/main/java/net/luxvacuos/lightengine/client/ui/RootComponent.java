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

package net.luxvacuos.lightengine.client.ui;

import static org.lwjgl.nanovg.NanoVG.nvgBeginPath;
import static org.lwjgl.nanovg.NanoVG.nvgRect;
import static org.lwjgl.nanovg.NanoVG.nvgRestore;
import static org.lwjgl.nanovg.NanoVG.nvgSave;
import static org.lwjgl.nanovg.NanoVG.nvgStroke;
import static org.lwjgl.nanovg.NanoVG.nvgStrokeColor;
import static org.lwjgl.nanovg.NanoVG.nvgStrokeWidth;

import java.util.ArrayList;
import java.util.List;

import net.luxvacuos.lightengine.client.rendering.api.glfw.Window;
import net.luxvacuos.lightengine.client.rendering.api.nanovg.themes.Theme;

public class RootComponent {

	private List<Component> components = new ArrayList<>();

	protected Root root;
	protected ILayout layout;

	public RootComponent(float x, float y, float w, float h) {
		layout = new EmptyLayout();
		root = new Root(x, y - h, w, h);
	}

	public void render(Window window) {
		if (Theme.DEBUG) {
			long vg = window.getNVGID();
			nvgSave(vg);
			nvgBeginPath(vg);
			nvgRect(vg, root.rootX ,
					window.getHeight() - root.rootY - root.rootH, root.rootW, root.rootH);
			nvgStrokeWidth(vg, Theme.DEBUG_STROKE);
			nvgStrokeColor(vg, Theme.debugC);
			nvgStroke(vg);
			nvgRestore(vg);
		}
		for (Component component : components) {
			component.render(window);
		}
	}

	public void update(float delta, Window window) {
		for (Component component : components) {
			component.update(delta, window);
		}
	}

	public void alwaysUpdate(float delta, Window window, float x, float y, float w, float h) {
		root.rootX = x;
		root.rootY = y - h;
		root.rootW = w;
		root.rootH = h;
		layout.preBuild();
		for (Component component : components) {
			layout.build(component);
			component.alwaysUpdate(delta, window);
		}
	}

	public void dispose() {
		for (Component component : components) {
			component.dispose();
		}
		components.clear();
	}

	public void addComponent(Component component) {
		component.rootComponent = root;
		component.init();
		components.add(component);
	}

	public void addAllComponents(List<Component> components) {
		for (Component component : components) {
			component.rootComponent = root;
			component.init();
			this.components.add(component);
		}
	}

	public void removeComponent(Component component) {
		component.dispose();
		components.remove(component);
	}

	public void setLayout(ILayout layout) {
		this.layout = layout;
	}

	public ILayout getLayout() {
		return layout;
	}

	public float getFinalW() {
		return layout.getFinalW();
	}

	public float getFinalH() {
		return layout.getFinalH();
	}

}
