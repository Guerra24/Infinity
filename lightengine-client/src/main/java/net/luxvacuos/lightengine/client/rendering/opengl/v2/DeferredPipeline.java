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

package net.luxvacuos.lightengine.client.rendering.opengl.v2;

import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_LINEAR;
import static org.lwjgl.opengl.GL11C.GL_NEAREST;
import static org.lwjgl.opengl.GL11C.GL_RGB;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL11C.glDisable;
import static org.lwjgl.opengl.GL11C.glDrawArrays;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL15C.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL30C.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30C.GL_COLOR_ATTACHMENT1;
import static org.lwjgl.opengl.GL30C.GL_COLOR_ATTACHMENT2;
import static org.lwjgl.opengl.GL30C.GL_COLOR_ATTACHMENT3;
import static org.lwjgl.opengl.GL30C.GL_COLOR_ATTACHMENT4;
import static org.lwjgl.opengl.GL30C.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30C.GL_DEPTH_COMPONENT32F;
import static org.lwjgl.opengl.GL30C.GL_DRAW_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.GL_READ_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.GL_RG;
import static org.lwjgl.opengl.GL30C.GL_RGB16F;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL30C.GL_RGBA16F;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;
import static org.lwjgl.opengl.GL30C.glBlitFramebuffer;

import java.util.ArrayList;
import java.util.List;

import net.luxvacuos.lightengine.client.network.IRenderingData;
import net.luxvacuos.lightengine.client.rendering.opengl.RendererData;
import net.luxvacuos.lightengine.client.rendering.opengl.RenderingSettings;
import net.luxvacuos.lightengine.client.rendering.opengl.objects.Framebuffer;
import net.luxvacuos.lightengine.client.rendering.opengl.objects.FramebufferBuilder;
import net.luxvacuos.lightengine.client.rendering.opengl.objects.Texture;
import net.luxvacuos.lightengine.client.rendering.opengl.objects.TextureBuilder;
import net.luxvacuos.lightengine.client.rendering.opengl.objects.VAO;
import net.luxvacuos.lightengine.client.rendering.opengl.shaders.FinalShader;

public abstract class DeferredPipeline {

	protected int width, height;
	private Texture[] auxTex;

	protected List<DeferredPass<?>> passes;

	private Framebuffer main;
	private Texture diffuseTex, positionTex, normalTex, pbrTex, maskTex, depthTex;

	private VAO quad;

	private Framebuffer previousFrame;
	private Texture previousFrameTex;

	private FinalShader finalShader;

	public DeferredPipeline(int width, int height) {
		this.width = width;
		this.height = height;
		passes = new ArrayList<>();
		auxTex = new Texture[3];
		init();
	}

	public void init() {
		float[] positions = { -1, 1, -1, -1, 1, 1, 1, -1 };
		quad = VAO.create();
		quad.bind();
		quad.createAttribute(0, positions, 2, GL_STATIC_DRAW);
		quad.unbind();
		quad.setVertexCount(4);
		generatePipeline();
		setupPasses();
		for (DeferredPass<?> pass : passes)
			pass.init(width, height);
		finalShader = new FinalShader("deferred/Final");
	}

	public abstract void setupPasses();

	public void bind() {
		main.bind();
	}

	public void unbind() {
		main.unbind();
	}

	public void process(RenderingSettings rs, RendererData rnd, IRenderingData rd) {
		glDisable(GL_DEPTH_TEST);
		quad.bind(0);
		for (DeferredPass<?> pass : passes)
			pass.process(rs, rnd, rd, this, auxTex, quad);
		quad.unbind(0);
		glEnable(GL_DEPTH_TEST);
	}

	public void render(Framebuffer fb) {
		previousFrame.bind();
		finalShader.start();
		quad.bind(0);
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, auxTex[0].getTexture());
		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
		quad.unbind(0);
		finalShader.stop();
		previousFrame.unbind();
		// Blit depth
		glBindFramebuffer(GL_READ_FRAMEBUFFER, main.getFramebuffer());
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fb.getFramebuffer());
		glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_DEPTH_BUFFER_BIT, GL_NEAREST);
		// Blit color
		glBindFramebuffer(GL_READ_FRAMEBUFFER, previousFrame.getFramebuffer());
		glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_COLOR_BUFFER_BIT, GL_NEAREST);
	}

	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
		disposePipeline();
		generatePipeline();
		for (DeferredPass<?> pass : passes)
			pass.resize(width, height);
	}

	public void dispose() {
		disposePipeline();
		for (DeferredPass<?> pass : passes)
			pass.dispose();
		quad.dispose();
		finalShader.dispose();
	}

	public void reloadShaders() {
		for (DeferredPass<?> deferredPass : passes)
			deferredPass.reloadShader();
		finalShader.reload();
	}

	private void generatePipeline() {
		var tb = new TextureBuilder();

		tb.genTexture(GL_TEXTURE_2D).bindTexture();
		tb.sizeTexture(width, height).texImage2D(0, GL_RGBA16F, 0, GL_RGBA, GL_FLOAT, 0);
		tb.texParameteri(GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		tb.texParameteri(GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		tb.texParameteri(GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		tb.texParameteri(GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		diffuseTex = tb.endTexture();

		tb.genTexture(GL_TEXTURE_2D).bindTexture();
		tb.sizeTexture(width, height).texImage2D(0, GL_RGB32F, 0, GL_RGB, GL_FLOAT, 0);
		tb.texParameteri(GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		tb.texParameteri(GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		tb.texParameteri(GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		tb.texParameteri(GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		positionTex = tb.endTexture();

		tb.genTexture(GL_TEXTURE_2D).bindTexture();
		tb.sizeTexture(width, height).texImage2D(0, GL_RGB16F, 0, GL_RGB, GL_FLOAT, 0);
		tb.texParameteri(GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		tb.texParameteri(GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		tb.texParameteri(GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		tb.texParameteri(GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		normalTex = tb.endTexture();

		tb.genTexture(GL_TEXTURE_2D).bindTexture();
		tb.sizeTexture(width, height).texImage2D(0, GL_RG, 0, GL_RG, GL_UNSIGNED_BYTE, 0);
		tb.texParameteri(GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		tb.texParameteri(GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		tb.texParameteri(GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		tb.texParameteri(GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		pbrTex = tb.endTexture();

		tb.genTexture(GL_TEXTURE_2D).bindTexture();
		tb.sizeTexture(width, height).texImage2D(0, GL_RGBA, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
		tb.texParameteri(GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		tb.texParameteri(GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		tb.texParameteri(GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		tb.texParameteri(GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		maskTex = tb.endTexture();

		tb.genTexture(GL_TEXTURE_2D).bindTexture();
		tb.sizeTexture(width, height).texImage2D(0, GL_DEPTH_COMPONENT32F, 0, GL_DEPTH_COMPONENT, GL_FLOAT, 0);
		tb.texParameteri(GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		tb.texParameteri(GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		tb.texParameteri(GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		tb.texParameteri(GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		depthTex = tb.endTexture();

		var fb = new FramebufferBuilder();

		fb.genFramebuffer().bindFramebuffer().sizeFramebuffer(width, height);
		fb.framebufferTexture(GL_COLOR_ATTACHMENT0, diffuseTex, 0);
		fb.framebufferTexture(GL_COLOR_ATTACHMENT1, positionTex, 0);
		fb.framebufferTexture(GL_COLOR_ATTACHMENT2, normalTex, 0);
		fb.framebufferTexture(GL_COLOR_ATTACHMENT3, pbrTex, 0);
		fb.framebufferTexture(GL_COLOR_ATTACHMENT4, maskTex, 0);
		fb.framebufferTexture(GL_DEPTH_ATTACHMENT, depthTex, 0);
		int bufs[] = { GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2, GL_COLOR_ATTACHMENT3,
				GL_COLOR_ATTACHMENT4 };
		fb.drawBuffers(bufs);
		main = fb.endFramebuffer();

		tb.genTexture(GL_TEXTURE_2D).bindTexture();
		tb.sizeTexture(width, height).texImage2D(0, GL_RGBA16F, 0, GL_RGBA, GL_FLOAT, 0);
		tb.texParameteri(GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		tb.texParameteri(GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		tb.texParameteri(GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		tb.texParameteri(GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		previousFrameTex = tb.endTexture();

		fb.genFramebuffer().bindFramebuffer().sizeFramebuffer(width, height);
		fb.framebufferTexture(GL_COLOR_ATTACHMENT0, previousFrameTex, 0);
		previousFrame = fb.endFramebuffer();
	}

	private void disposePipeline() {
		main.dispose();
		diffuseTex.dispose();
		positionTex.dispose();
		normalTex.dispose();
		pbrTex.dispose();
		maskTex.dispose();
		depthTex.dispose();
		previousFrame.dispose();
		previousFrameTex.dispose();
	}

	public Texture getDiffuseTex() {
		return diffuseTex;
	}

	public Texture getPositionTex() {
		return positionTex;
	}

	public Texture getNormalTex() {
		return normalTex;
	}

	public Texture getPbrTex() {
		return pbrTex;
	}

	public Texture getMaskTex() {
		return maskTex;
	}

	public Texture getDepthTex() {
		return depthTex;
	}

	public Texture getPreviousFrameTex() {
		return previousFrameTex;
	}

}
