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

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE6;
import static org.lwjgl.opengl.GL13.GL_TEXTURE7;
import static org.lwjgl.opengl.GL13.GL_TEXTURE8;
import static org.lwjgl.opengl.GL13.GL_TEXTURE9;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import net.luxvacuos.lightengine.client.core.subsystems.GraphicalSubsystem;
import net.luxvacuos.lightengine.client.rendering.api.opengl.DeferredPass;
import net.luxvacuos.lightengine.client.rendering.api.opengl.FBO;
import net.luxvacuos.lightengine.client.rendering.api.opengl.IDeferredPipeline;
import net.luxvacuos.lightengine.client.rendering.api.opengl.ShadowFBO;
import net.luxvacuos.lightengine.client.rendering.api.opengl.objects.CubeMapTexture;
import net.luxvacuos.lightengine.client.rendering.api.opengl.objects.Texture;

public class LensFlareMod extends DeferredPass {

	private Texture lensDirt;
	private Texture lensStar;

	public LensFlareMod(String name, int width, int height) {
		super(name, width, height);
		lensDirt = GraphicalSubsystem.getMainWindow().getResourceLoader()
				.loadTextureMisc("textures/lens/lens_dirt.png");
		lensStar = GraphicalSubsystem.getMainWindow().getResourceLoader()
				.loadTextureMisc("textures/lens/lens_star.png");
	}

	@Override
	public void render(FBO[] auxs, IDeferredPipeline pipe, CubeMapTexture irradianceCapture,
			CubeMapTexture environmentMap, Texture brdfLUT, ShadowFBO shadow) {
		glActiveTexture(GL_TEXTURE6);
		glBindTexture(GL_TEXTURE_2D, auxs[0].getTexture());
		glActiveTexture(GL_TEXTURE7);
		glBindTexture(GL_TEXTURE_2D, lensDirt.getID());
		glActiveTexture(GL_TEXTURE8);
		glBindTexture(GL_TEXTURE_2D, lensStar.getID());
		glActiveTexture(GL_TEXTURE9);
		glBindTexture(GL_TEXTURE_2D, auxs[1].getTexture());
	}

	@Override
	public void dispose() {
		super.dispose();
		lensDirt.dispose();
		lensStar.dispose();
	}

}