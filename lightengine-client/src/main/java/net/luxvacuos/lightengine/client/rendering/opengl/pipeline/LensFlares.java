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

package net.luxvacuos.lightengine.client.rendering.opengl.pipeline;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE1;

import net.luxvacuos.lightengine.client.rendering.opengl.RendererData;
import net.luxvacuos.lightengine.client.rendering.opengl.objects.Texture;
import net.luxvacuos.lightengine.client.rendering.opengl.pipeline.shaders.LensFlaresShader;
import net.luxvacuos.lightengine.client.rendering.opengl.v2.DeferredPass;
import net.luxvacuos.lightengine.client.rendering.opengl.v2.DeferredPipeline;
import net.luxvacuos.lightengine.client.resources.ResourcesManager;

public class LensFlares extends DeferredPass<LensFlaresShader> {

	private Texture lensColor;

	public LensFlares() {
		super("LensFlares");
	}

	@Override
	public void init(int width, int height) {
		super.init(width, height);
		lensColor = ResourcesManager.loadTextureMisc("textures/lens/lens_color.png", null).get();
	}

	@Override
	protected LensFlaresShader setupShader() {
		return new LensFlaresShader(name);
	}

	@Override
	protected void setupTextures(RendererData rnd, DeferredPipeline dp, Texture[] auxTex) {
		super.activateTexture(GL_TEXTURE0, GL_TEXTURE_2D, auxTex[1].getTexture());
		super.activateTexture(GL_TEXTURE1, GL_TEXTURE_2D, lensColor.getTexture());
		auxTex[1] = auxTex[0];
	}

	@Override
	public void dispose() {
		super.dispose();
		lensColor.dispose();
	}

}