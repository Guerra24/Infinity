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

package net.luxvacuos.lightengine.client.rendering.opengl;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11C.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL11C.glDrawElements;
import static org.lwjgl.opengl.GL11C.glGetIntegerv;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE2;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE3;
import static org.lwjgl.opengl.GL13C.glActiveTexture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector2f;

import com.badlogic.ashley.core.Entity;

import net.luxvacuos.lightengine.client.ecs.ClientComponents;
import net.luxvacuos.lightengine.client.ecs.entities.CameraEntity;
import net.luxvacuos.lightengine.client.network.IRenderingData;
import net.luxvacuos.lightengine.client.rendering.opengl.objects.Material;
import net.luxvacuos.lightengine.client.rendering.opengl.objects.Material.MaterialType;
import net.luxvacuos.lightengine.client.rendering.opengl.objects.Mesh;
import net.luxvacuos.lightengine.client.rendering.opengl.objects.Model;
import net.luxvacuos.lightengine.client.rendering.opengl.shaders.EntityDeferredShader;
import net.luxvacuos.lightengine.client.util.Maths;
import net.luxvacuos.lightengine.universal.ecs.entities.BasicEntity;

public class EntityRenderer implements IObjectRenderer {

	public static final int ENTITY_RENDERER_ID = 10;
	/**
	 * Entity Shader
	 */
	private EntityDeferredShader shader;
	private Map<Material, List<EntityRendererObject>> entities = new HashMap<>();
	private EntityShadowRenderer shadowRenderer;
	private EntityForwardRenderer forwardRenderer;

	// TODO: Temporary res storage
	private int[] viewport = new int[4];
	private Vector2f resolution = new Vector2f();

	public EntityRenderer() {
		shader = new EntityDeferredShader();
		shadowRenderer = new EntityShadowRenderer();
		forwardRenderer = new EntityForwardRenderer();
	}

	@Override
	public void preProcess(List<BasicEntity> entities) {
		for (Entity entity : entities) {
			processEntity((BasicEntity) entity);
		}
	}

	@Override
	public void render(CameraEntity camera) {
		glGetIntegerv(GL_VIEWPORT, viewport);
		resolution.set(viewport[2], viewport[3]);
		shader.start();
		shader.loadCamera(camera, resolution);
		renderEntity(entities);
		shader.stop();
	}

	@Override
	public void renderReflections(IRenderingData rd, RendererData rnd, CameraEntity cubeCamera) {
		forwardRenderer.render(entities, rd, rnd, cubeCamera, false, MaterialType.OPAQUE);
	}

	@Override
	public void renderForward(IRenderingData rd, RendererData rnd) {
		forwardRenderer.render(entities, rd, rnd, null, true, MaterialType.TRANSPARENT);
	}

	@Override
	public void renderShadow(CameraEntity sun) {
		shadowRenderer.renderShadow(entities, sun);
	}

	@Override
	public void end() {
		entities.clear();
	}

	private void processEntity(BasicEntity entity) {
		Model model = ClientComponents.RENDERABLE.get(entity).getModel();
		for (Mesh mesh : model.getMeshes()) {
			Material mat = model.getMaterials().get(mesh.getAiMesh().mMaterialIndex());

			EntityRendererObject obj = new EntityRendererObject();
			obj.entity = entity;
			obj.mesh = mesh;
			List<EntityRendererObject> batch = entities.get(mat);
			if (batch != null)
				batch.add(obj);
			else {
				List<EntityRendererObject> newBatch = new ArrayList<>();
				newBatch.add(obj);
				entities.put(mat, newBatch);
			}
		}
	}

	private void renderEntity(Map<Material, List<EntityRendererObject>> entities) {
		for (Material mat : entities.keySet()) {
			if (mat.getType() != MaterialType.OPAQUE)
				continue;
			List<EntityRendererObject> batch = entities.get(mat);
			for (EntityRendererObject obj : batch) {
				prepareInstance(obj.entity);
				prepareTexturedModel(obj.mesh, mat);
				shader.loadMaterial(mat);
				glDrawElements(GL_TRIANGLES, obj.mesh.getMesh().getIndexCount(), GL_UNSIGNED_INT, 0);
				unbindTexturedModel(obj.mesh);
			}
		}
	}

	private void prepareTexturedModel(Mesh mesh, Material material) {
		mesh.getMesh().bind(0, 1, 2, 3);
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, material.getDiffuseTexture().getTexture());
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, material.getNormalTexture().getTexture());
		glActiveTexture(GL_TEXTURE2);
		glBindTexture(GL_TEXTURE_2D, material.getRoughnessTexture().getTexture());
		glActiveTexture(GL_TEXTURE3);
		glBindTexture(GL_TEXTURE_2D, material.getMetallicTexture().getTexture());
	}

	private void unbindTexturedModel(Mesh mesh) {
		mesh.getMesh().unbind(0, 1, 2, 3);
	}

	private void prepareInstance(BasicEntity entity) {
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(), entity.getRX(),
				entity.getRY(), entity.getRZ(), entity.getScale());
		shader.loadTransformationMatrix(transformationMatrix);
	}

	@Override
	public void dispose() {
		shader.dispose();
		shadowRenderer.dispose();
		forwardRenderer.dispose();
	}

	@Override
	public int getID() {
		return ENTITY_RENDERER_ID;
	}

}
