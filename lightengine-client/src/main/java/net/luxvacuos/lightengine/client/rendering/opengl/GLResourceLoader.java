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

import static org.lwjgl.nanovg.NanoVG.nvgCreateFontMem;
import static org.lwjgl.nanovg.NanoVG.nvgCreateImageMem;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RED;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameterf;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_LOD_BIAS;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL21.GL_SRGB_ALPHA;
import static org.lwjgl.opengl.GL30.GL_RG;
import static org.lwjgl.opengl.GL30.GL_RGB16F;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_info_from_memory;
import static org.lwjgl.stb.STBImage.stbi_is_hdr_from_memory;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memRealloc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.system.MemoryStack;

import net.luxvacuos.igl.Logger;
import net.luxvacuos.lightengine.client.core.ClientTaskManager;
import net.luxvacuos.lightengine.client.core.exception.DecodeTextureException;
import net.luxvacuos.lightengine.client.core.exception.LoadOBJModelException;
import net.luxvacuos.lightengine.client.core.exception.LoadTextureException;
import net.luxvacuos.lightengine.client.core.subsystems.GraphicalSubsystem;
import net.luxvacuos.lightengine.client.rendering.IResourceLoader;
import net.luxvacuos.lightengine.client.rendering.glfw.AbstractWindow;
import net.luxvacuos.lightengine.client.rendering.opengl.objects.CubeMapTexture;
import net.luxvacuos.lightengine.client.rendering.opengl.objects.RawModel;
import net.luxvacuos.lightengine.client.rendering.opengl.objects.RawTexture;
import net.luxvacuos.lightengine.client.rendering.opengl.objects.Texture;
import net.luxvacuos.lightengine.client.rendering.opengl.objects.VertexNM;
import net.luxvacuos.lightengine.client.ui.Font;
import net.luxvacuos.lightengine.universal.core.TaskManager;

/**
 * This objects handles all loading methods from any type of data, models,
 * textures, fonts, etc.
 * 
 * @author Guerra24 <pablo230699@hotmail.com>
 * @category Assets
 */
public class GLResourceLoader implements IResourceLoader {
	/**
	 * VAOs List
	 */
	private List<Integer> vaos = new ArrayList<Integer>();
	/**
	 * VBOs List
	 */
	private List<Integer> vbos = new ArrayList<Integer>();
	private AbstractWindow window;

	public GLResourceLoader(AbstractWindow abstractWindow) {
		this.window = abstractWindow;
	}

	/**
	 * Load a multiple arrays of positions, texture coords, normals and indices
	 * 
	 * @param positions     Array of Positions
	 * @param textureCoords Array of Tex Coords
	 * @param normals       Array of Normals
	 * @param indices       Array of Indices
	 * @return A RawModel
	 */
	@Override
	public RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals, float[] tangents,
			int[] indices) {
		int vaoID = createVAO();
		bindIndicesBuffer(indices);
		storeDataInAttributeList(0, 3, positions);
		storeDataInAttributeList(1, 2, textureCoords);
		storeDataInAttributeList(2, 3, normals);
		storeDataInAttributeList(3, 3, tangents);
		unbindVAO();
		return new RawModel(vaoID, indices.length);
	}

	@Override
	public int loadToVAO(float[] positions, float[] textureCoords) {
		int vaoID = createVAO();
		storeDataInAttributeList(0, 2, positions);
		storeDataInAttributeList(1, 2, textureCoords);
		unbindVAO();
		return vaoID;
	}

	/**
	 * Load an array of positions and a dimension
	 * 
	 * @param positions  Array of Positions
	 * @param dimensions Dimension
	 * @return RawModel
	 */
	@Override
	public RawModel loadToVAO(float[] positions, int dimensions) {
		int vaoID = createVAO();
		this.storeDataInAttributeList(0, dimensions, positions);
		unbindVAO();
		return new RawModel(vaoID, positions.length / dimensions);
	}

	@Override
	public int createEmptyVBO(int floatCount) {
		int vbo = glGenBuffers();
		vbos.add(vbo);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, floatCount * 4, GL_STREAM_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		return vbo;
	}

	@Override
	public void updateVBO(int vbo, float[] data) {
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, data.length * 4, GL_STREAM_DRAW);
		glBufferSubData(GL_ARRAY_BUFFER, 0, data);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	@Override
	public void addInstacedAttribute(int vao, int vbo, int attribute, int dataSize, int instancedDataLenght,
			int offset) {
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBindVertexArray(vao);
		glVertexAttribPointer(attribute, dataSize, GL_FLOAT, false, instancedDataLenght * 4, offset * 4);
		glVertexAttribDivisor(attribute, 1);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}

	@Override
	public Texture loadTextureMisc(String fileName) {
		return loadTextureMisc(fileName, GL_LINEAR, true);
	}

	@Override
	public Texture loadTextureMisc(String fileName, int filter, boolean textureMipMapAF) {
		int texture_id = 0;
		try {
			Logger.log("Loading Texture: " + fileName);
			texture_id = loadTexture("assets/" + fileName, filter, GL_REPEAT, GL_RGBA, textureMipMapAF);
		} catch (Exception e) {
			throw new LoadTextureException(fileName, e);
		}
		return new Texture(texture_id);
	}

	@Override
	public Texture loadTexture(String fileName) {
		return loadTexture(fileName, GL_LINEAR, true);
	}

	@Override
	public Texture loadTexture(String fileName, int filter, boolean textureMipMapAF) {
		int texture = 0;
		try {
			Logger.log("Loading Texture: " + fileName);
			texture = loadTexture("assets/" + fileName, filter, GL_REPEAT, GL_SRGB_ALPHA, textureMipMapAF);
		} catch (Exception e) {
			throw new LoadTextureException(fileName, e);
		}
		return new Texture(texture);
	}

	private int loadTexture(String file, int filter, int textureWarp, int format, boolean textureMipMapAF) {
		RawTexture data = decodeTextureFile(file);
		if (isThread(((ClientTaskManager) TaskManager.tm).getRenderBackgroundThreadID())
				|| isThread(GraphicalSubsystem.getRenderThreadID())) {
			int textureID = createTexture(data, filter, textureWarp, format, textureMipMapAF);
			data.dispose();
			return textureID;
		} else {
			Thread main = Thread.currentThread();
			int[] textureID = new int[1];
			textureID[0] = -1;
			TaskManager.tm.addTaskRenderBackgroundThread(() -> {
				textureID[0] = createTexture(data, filter, textureWarp, format, textureMipMapAF);
				main.interrupt();
			});
			try {
				Thread.sleep(Long.MAX_VALUE);
			} catch (InterruptedException e) {
			}
			data.dispose();
			return textureID[0];
		}
	}

	@Override
	public int createTexture(RawTexture data, int filter, int textureWarp, int format, boolean textureMipMapAF) {
		int textureID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, textureID);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, textureWarp);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, textureWarp);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
		if (data.getComp() == 3) {
			if ((data.getWidth() & 3) != 0)
				glPixelStorei(GL_UNPACK_ALIGNMENT, 2 - (data.getWidth() & 1));
			glTexImage2D(GL_TEXTURE_2D, 0, format, data.getWidth(), data.getHeight(), 0, GL_RGB, GL_UNSIGNED_BYTE,
					data.getBuffer());
		} else if (data.getComp() == 2)
			glTexImage2D(GL_TEXTURE_2D, 0, format, data.getWidth(), data.getHeight(), 0, GL_RG, GL_UNSIGNED_BYTE,
					data.getBuffer());
		else if (data.getComp() == 1)
			glTexImage2D(GL_TEXTURE_2D, 0, format, data.getWidth(), data.getHeight(), 0, GL_RED, GL_UNSIGNED_BYTE,
					data.getBuffer());
		else
			glTexImage2D(GL_TEXTURE_2D, 0, format, data.getWidth(), data.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE,
					data.getBuffer());

		if (textureMipMapAF) {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, 0);
			glGenerateMipmap(GL_TEXTURE_2D);

			if (window.getCapabilities().GL_EXT_texture_filter_anisotropic) {
				float amount = Math.min(16f, EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
				glTexParameterf(GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, amount);
			} else
				Logger.warn("Anisotropic Filtering not supported");
		}
		glBindTexture(GL_TEXTURE_2D, 0);
		return textureID;
	}

	@Override
	public Font loadNVGFont(String filename, String name) {
		return loadNVGFont(filename, name, 150);
	}

	@Override
	public Font loadNVGFont(String filename, String name, int size) {
		Logger.log("Loading NVGFont: " + filename + ".ttf");
		int font[] = new int[1];
		ByteBuffer buffer[] = new ByteBuffer[1];
		try {
			buffer[0] = ioResourceToByteBuffer("assets/fonts/" + filename + ".ttf", size * 1024);

			if (isThread(GraphicalSubsystem.getRenderThreadID()))
				font[0] = nvgCreateFontMem(window.getNVGID(), name, buffer[0], 0);
			else {
				Thread main = Thread.currentThread();
				TaskManager.tm.addTaskRenderThread(() -> {
					font[0] = nvgCreateFontMem(window.getNVGID(), name, buffer[0], 0);
					main.interrupt();
				});
				try {
					Thread.sleep(Long.MAX_VALUE);
				} catch (InterruptedException e) {
				}
			}

			font[0] = nvgCreateFontMem(window.getNVGID(), name, buffer[0], 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Font(name, buffer[0], font[0]);
	}

	@Override
	public int loadNVGTexture(String file) {
		Logger.log("Loading NVGTexture: " + file + ".png");
		ByteBuffer buffer[] = new ByteBuffer[1];
		int tex[] = new int[1];
		try {
			buffer[0] = ioResourceToByteBuffer("assets/textures/menu/" + file + ".png", 1024 * 1024);

			if (isThread(GraphicalSubsystem.getRenderThreadID()))
				tex[0] = nvgCreateImageMem(window.getNVGID(), 0, buffer[0]);
			else {
				Thread main = Thread.currentThread();
				TaskManager.tm.addTaskRenderThread(() -> {
					tex[0] = nvgCreateImageMem(window.getNVGID(), 0, buffer[0]);
					main.interrupt();
				});
				try {
					Thread.sleep(Long.MAX_VALUE);
				} catch (InterruptedException e) {
				}
			}
			memFree(buffer[0]);
		} catch (Exception e) {
			throw new LoadTextureException(file, e);
		}
		return tex[0];
	}

	@Override
	public int loadCubeMap(String[] textureFiles) {
		int texID = glGenTextures();
		glBindTexture(GL_TEXTURE_CUBE_MAP, texID);

		for (int i = 0; i < textureFiles.length; i++) {
			RawTexture data = decodeTextureFile("assets/textures/skybox/" + textureFiles[i] + ".png");
			glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA, data.getWidth(), data.getHeight(), 0, GL_RGBA,
					GL_UNSIGNED_BYTE, data.getBuffer());
		}

		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
		return texID;
	}

	@Override
	public CubeMapTexture createEmptyCubeMap(int size, boolean hdr, boolean mipmap) {
		int texID = glGenTextures();
		glBindTexture(GL_TEXTURE_CUBE_MAP, texID);
		if (hdr)
			for (int i = 0; i < 6; i++) {
				glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB16F, size, size, 0, GL_RGB, GL_FLOAT,
						(ByteBuffer) null);
			}
		else
			for (int i = 0; i < 6; i++) {
				glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB, size, size, 0, GL_RGB, GL_UNSIGNED_BYTE,
						(ByteBuffer) null);
			}
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
		if (mipmap) {
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
			glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_LOD_BIAS, 0);
			glGenerateMipmap(GL_TEXTURE_CUBE_MAP);
		}
		glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
		return new CubeMapTexture(texID, size);
	}

	private RawTexture decodeTextureFile(String file) {
		ByteBuffer imageBuffer;
		try {
			imageBuffer = ioResourceToByteBuffer(file, 1024 * 1024);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		int width = 0;
		int height = 0;
		int component = 0;
		ByteBuffer image;
		try (MemoryStack stack = stackPush()) {
			IntBuffer w = stack.callocInt(1);
			IntBuffer h = stack.callocInt(1);
			IntBuffer comp = stack.callocInt(1);

			if (!stbi_info_from_memory(imageBuffer, w, h, comp))
				throw new DecodeTextureException("Failed to read image information: " + stbi_failure_reason());

			Logger.log("Image width: " + w.get(0), "Image height: " + h.get(0), "Image components: " + comp.get(0),
					"Image HDR: " + stbi_is_hdr_from_memory(imageBuffer));

			image = stbi_load_from_memory(imageBuffer, w, h, comp, 0);
			memFree(imageBuffer);

			if (image == null)
				throw new DecodeTextureException("Failed to load image: " + stbi_failure_reason());
			width = w.get(0);
			height = h.get(0);
			component = comp.get(0);
		}
		return new RawTexture(image, width, height, component);
	}

	/**
	 * Load an ObjModel
	 * 
	 * @param fileName OBJ File name
	 * @return RawModel that contains all the data loaded to the GPU
	 */
	@Override
	public RawModel loadObjModel(String fileName) {
		InputStream file = getClass().getClassLoader().getResourceAsStream("assets/models/" + fileName + ".obj");
		Logger.log("Loading Model: " + fileName + ".obj");
		BufferedReader reader = new BufferedReader(new InputStreamReader(file));
		String line;
		List<VertexNM> vertices = new ArrayList<VertexNM>();
		List<Vector2f> textures = new ArrayList<Vector2f>();
		List<Vector3f> normals = new ArrayList<Vector3f>();
		List<Integer> indices = new ArrayList<Integer>();
		try {
			while (true) {
				line = reader.readLine();
				if (line.startsWith("v ")) {
					String[] currentLine = line.split(" ");
					Vector3f vertex = new Vector3f((float) Float.valueOf(currentLine[1]),
							(float) Float.valueOf(currentLine[2]), (float) Float.valueOf(currentLine[3]));
					VertexNM newVertex = new VertexNM(vertices.size(), vertex);
					vertices.add(newVertex);

				} else if (line.startsWith("vt ")) {
					String[] currentLine = line.split(" ");
					Vector2f texture = new Vector2f((float) Float.valueOf(currentLine[1]),
							(float) Float.valueOf(currentLine[2]));
					textures.add(texture);
				} else if (line.startsWith("vn ")) {
					String[] currentLine = line.split(" ");
					Vector3f normal = new Vector3f((float) Float.valueOf(currentLine[1]),
							(float) Float.valueOf(currentLine[2]), (float) Float.valueOf(currentLine[3]));
					normals.add(normal);
				} else if (line.startsWith("f ")) {
					break;
				}
			}
			while (line != null && line.startsWith("f ")) {
				String[] currentLine = line.split(" ");
				String[] vertex1 = currentLine[1].split("/");
				String[] vertex2 = currentLine[2].split("/");
				String[] vertex3 = currentLine[3].split("/");
				VertexNM v0 = processVertex(vertex1, vertices, indices);
				VertexNM v1 = processVertex(vertex2, vertices, indices);
				VertexNM v2 = processVertex(vertex3, vertices, indices);
				calculateTangents(v0, v1, v2, textures);// NEW
				line = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			throw new LoadOBJModelException(e);
		}

		removeUnusedVertices(vertices);
		float[] verticesArray = new float[vertices.size() * 3];
		float[] texturesArray = new float[vertices.size() * 2];
		float[] normalsArray = new float[vertices.size() * 3];
		float[] tangentsArray = new float[vertices.size() * 3];
		float furthest = convertDataToArrays(vertices, textures, normals, verticesArray, texturesArray, normalsArray,
				tangentsArray);
		int[] indicesArray = convertIndicesListToArray(indices);

		return loadToVAO(verticesArray, texturesArray, normalsArray, tangentsArray, indicesArray);
	}

	private void calculateTangents(VertexNM v0, VertexNM v1, VertexNM v2, List<Vector2f> textures) {
		Vector3f delatPos1 = v1.getPosition().sub(v0.getPosition(), new Vector3f());
		Vector3f delatPos2 = v2.getPosition().sub(v0.getPosition(), new Vector3f());
		Vector2f uv0 = textures.get(v0.getTextureIndex());
		Vector2f uv1 = textures.get(v1.getTextureIndex());
		Vector2f uv2 = textures.get(v2.getTextureIndex());
		Vector2f deltaUv1 = uv1.sub(uv0, new Vector2f());
		Vector2f deltaUv2 = uv2.sub(uv0, new Vector2f());

		float r = 1.0f / (deltaUv1.x * deltaUv2.y - deltaUv1.y * deltaUv2.x);
		delatPos1.mul(deltaUv2.y);
		delatPos2.mul(deltaUv1.y);
		Vector3f tangent = delatPos1.sub(delatPos2, new Vector3f());
		tangent.mul(r);
		v0.addTangent(tangent);
		v1.addTangent(tangent);
		v2.addTangent(tangent);
	}

	private VertexNM processVertex(String[] vertex, List<VertexNM> vertices, List<Integer> indices) {
		int index = Integer.parseInt(vertex[0]) - 1;
		VertexNM currentVertex = vertices.get(index);
		int textureIndex = Integer.parseInt(vertex[1]) - 1;
		int normalIndex = Integer.parseInt(vertex[2]) - 1;
		if (!currentVertex.isSet()) {
			currentVertex.setTextureIndex(textureIndex);
			currentVertex.setNormalIndex(normalIndex);
			indices.add(index);
			return currentVertex;
		} else {
			return dealWithAlreadyProcessedVertex(currentVertex, textureIndex, normalIndex, indices, vertices);
		}
	}

	private int[] convertIndicesListToArray(List<Integer> indices) {
		int[] indicesArray = new int[indices.size()];
		for (int i = 0; i < indicesArray.length; i++) {
			indicesArray[i] = indices.get(i);
		}
		return indicesArray;
	}

	private float convertDataToArrays(List<VertexNM> vertices, List<Vector2f> textures, List<Vector3f> normals,
			float[] verticesArray, float[] texturesArray, float[] normalsArray, float[] tangentsArray) {
		float furthestPoint = 0;
		for (int i = 0; i < vertices.size(); i++) {
			VertexNM currentVertex = vertices.get(i);
			if (currentVertex.getLength() > furthestPoint) {
				furthestPoint = currentVertex.getLength();
			}
			Vector3f position = currentVertex.getPosition();
			Vector2f textureCoord = textures.get(currentVertex.getTextureIndex());
			Vector3f normalVector = normals.get(currentVertex.getNormalIndex());
			Vector3f tangent = currentVertex.getAverageTangent();
			verticesArray[i * 3] = position.x;
			verticesArray[i * 3 + 1] = position.y;
			verticesArray[i * 3 + 2] = position.z;
			texturesArray[i * 2] = textureCoord.x;
			texturesArray[i * 2 + 1] = 1 - textureCoord.y;
			normalsArray[i * 3] = normalVector.x;
			normalsArray[i * 3 + 1] = normalVector.y;
			normalsArray[i * 3 + 2] = normalVector.z;
			tangentsArray[i * 3] = tangent.x;
			tangentsArray[i * 3 + 1] = tangent.y;
			tangentsArray[i * 3 + 2] = tangent.z;

		}
		return furthestPoint;
	}

	private VertexNM dealWithAlreadyProcessedVertex(VertexNM previousVertex, int newTextureIndex, int newNormalIndex,
			List<Integer> indices, List<VertexNM> vertices) {
		if (previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) {
			indices.add(previousVertex.getIndex());
			return previousVertex;
		} else {
			VertexNM anotherVertex = previousVertex.getDuplicateVertex();
			if (anotherVertex != null) {
				return dealWithAlreadyProcessedVertex(anotherVertex, newTextureIndex, newNormalIndex, indices,
						vertices);
			} else {
				VertexNM duplicateVertex = previousVertex.duplicate(vertices.size());// NEW
				duplicateVertex.setTextureIndex(newTextureIndex);
				duplicateVertex.setNormalIndex(newNormalIndex);
				previousVertex.setDuplicateVertex(duplicateVertex);
				vertices.add(duplicateVertex);
				indices.add(duplicateVertex.getIndex());
				return duplicateVertex;
			}
		}
	}

	private void removeUnusedVertices(List<VertexNM> vertices) {
		for (VertexNM vertex : vertices) {
			vertex.averageTangents();
			if (!vertex.isSet()) {
				vertex.setTextureIndex(0);
				vertex.setNormalIndex(0);
			}
		}
	}

	/**
	 * Reads the specified resource and returns the raw data as a ByteBuffer.
	 *
	 * @param resource   the resource to read
	 * @param bufferSize the initial buffer size
	 *
	 * @return the resource data
	 *
	 * @throws IOException if an IO error occurs
	 */
	public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
		ByteBuffer buffer;

		File file = new File(resource);
		if (file.isFile()) {
			FileInputStream fis = new FileInputStream(file);
			FileChannel fc = fis.getChannel();

			buffer = memAlloc((int) fc.size() + 1);

			while (fc.read(buffer) != -1)
				;

			fis.close();
			fc.close();
		} else {
			int size = 0;
			buffer = memAlloc(bufferSize);
			try (InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
				if (source == null)
					throw new FileNotFoundException(resource);
				try (ReadableByteChannel rbc = Channels.newChannel(source)) {
					while (true) {
						int bytes = rbc.read(buffer);
						if (bytes == -1)
							break;
						size += bytes;
						if (!buffer.hasRemaining())
							buffer = memRealloc(buffer, size * 2);
					}
				}
			}
			buffer = memRealloc(buffer, size + 1);
		}
		buffer.put((byte) 0);
		buffer.flip();
		return buffer;
	}

	public static boolean isThread(long id) {
		return Thread.currentThread().getId() == id;
	}

	/**
	 * Create VAO
	 * 
	 * @return VaoID
	 */
	private int createVAO() {
		int vaoID = glGenVertexArrays();
		vaos.add(vaoID);
		glBindVertexArray(vaoID);
		return vaoID;
	}

	/**
	 * Store The Data in Attribute List
	 * 
	 * @param attributeNumber Number
	 * @param coordinateSize  Coord Size
	 * @param data            Data
	 */
	private void storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data) {
		int vboID = glGenBuffers();
		vbos.add(vboID);
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
		glVertexAttribPointer(attributeNumber, coordinateSize, GL_FLOAT, false, 0, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	/**
	 * Unbids the VAO
	 * 
	 */
	private void unbindVAO() {
		glBindVertexArray(0);
	}

	/**
	 * Bind Indices Buffer
	 * 
	 * @param indices Array of Indices
	 */
	private void bindIndicesBuffer(int[] indices) {
		int vboID = glGenBuffers();
		vbos.add(vboID);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
	}

	/**
	 * Clear All VAOs, VBOs and Textures
	 * 
	 */
	@Override
	public void dispose() {
		Logger.log("Cleaning Resources for: " + window.getID());
		for (int vao : vaos) {
			glDeleteVertexArrays(vao);
		}
		for (int vbo : vbos) {
			glDeleteBuffers(vbo);
		}
	}

}