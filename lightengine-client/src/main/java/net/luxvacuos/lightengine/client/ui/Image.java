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

import static org.lwjgl.nanovg.NanoVG.nvgDeleteImage;

import net.luxvacuos.lightengine.client.rendering.glfw.Window;
import net.luxvacuos.lightengine.client.rendering.nanovg.themes.Theme;
import net.luxvacuos.lightengine.universal.core.TaskManager;

public class Image extends Component {

	private int image = -1;
	private boolean deleteOnClose = true;

	public Image(float x, float y, float w, float h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public Image(float x, float y, float w, float h, int image) {
		this(x, y, w, h);
		this.image = image;
	}

	public Image(float x, float y, float w, float h, int image, boolean deleteOnClose) {
		this(x, y, w, h, image);
		this.deleteOnClose = deleteOnClose;
	}

	@Override
	public void render(Window window) {
		if (image != -1)
			Theme.renderImage(window.getNVGID(), rootComponent.rootX + alignedX,
					window.getHeight() - rootComponent.rootY - alignedY - h, w, h, image, 1);
	}

	@Override
	public void dispose(Window window) {
		super.dispose(window);
		if (deleteOnClose && image != -1)
			TaskManager.tm.addTaskRenderThread(() -> nvgDeleteImage(window.getNVGID(), image));
	}

	public void setImage(int image) {
		this.image = image;
	}

}
