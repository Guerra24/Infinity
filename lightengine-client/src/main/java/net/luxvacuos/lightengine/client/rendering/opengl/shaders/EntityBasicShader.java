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

package net.luxvacuos.lightengine.client.rendering.opengl.shaders;

import org.joml.Matrix4f;

import net.luxvacuos.lightengine.client.ecs.entities.CameraEntity;
import net.luxvacuos.lightengine.client.rendering.opengl.shaders.data.Attribute;
import net.luxvacuos.lightengine.client.rendering.opengl.shaders.data.UniformMatrix;
import net.luxvacuos.lightengine.client.rendering.opengl.shaders.data.UniformSampler;
import net.luxvacuos.lightengine.client.util.Maths;
import net.luxvacuos.lightengine.universal.core.subsystems.ResManager;
import net.luxvacuos.lightengine.universal.resources.ResourceType;

public class EntityBasicShader extends ShaderProgram {

	private UniformMatrix transformationMatrix = new UniformMatrix("transformationMatrix");
	private UniformMatrix projectionMatrix = new UniformMatrix("projectionMatrix");
	private UniformMatrix viewMatrix = new UniformMatrix("viewMatrix");
	private UniformSampler texture0 = new UniformSampler("texture0");

	public EntityBasicShader() {
		super(ResManager.getResourceOfType("ENGINE_RND_EntityBasic_VS", ResourceType.SHADER).get(),
				ResManager.getResourceOfType("ENGINE_RND_EntityBasic_FS", ResourceType.SHADER).get(),
				new Attribute(0, "position"), new Attribute(1, "textureCoords"));
		super.storeUniforms(transformationMatrix, projectionMatrix, viewMatrix, texture0);
		super.validate();
		this.loadInitialData();
	}

	@Override
	protected void loadInitialData() {
		super.start();
		texture0.loadTexUnit(0);
		super.stop();
	}

	public void loadTransformationMatrix(Matrix4f matrix) {
		transformationMatrix.loadMatrix(matrix);
	}

	public void loadviewMatrix(CameraEntity camera) {
		viewMatrix.loadMatrix(Maths.createViewMatrix(camera));
	}

	public void loadProjectionMatrix(Matrix4f projection) {
		projectionMatrix.loadMatrix(projection);
	}

}
