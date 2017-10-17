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

package net.luxvacuos.lightengine.client.rendering.api.opengl.pipeline;

import static net.luxvacuos.lightengine.universal.core.subsystems.CoreSubsystem.REGISTRY;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE6;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import net.luxvacuos.lightengine.client.rendering.api.opengl.FBO;
import net.luxvacuos.lightengine.client.rendering.api.opengl.PostProcessPass;
import net.luxvacuos.lightengine.universal.util.registry.KeyCache;

public class MotionBlur extends PostProcessPass {

	public MotionBlur(String name, int width, int height) {
		super(name, width, height);
	}

	@Override
	public void render(FBO[] auxs) {
		glActiveTexture(GL_TEXTURE6);
		glBindTexture(GL_TEXTURE_2D, auxs[0].getTexture());
	}
	
	@Override
	public boolean isEnabled() {
		return (boolean) REGISTRY.getRegistryItem(KeyCache.getKey("/Light Engine/Settings/Graphics/motionBlur"));
	}

}
