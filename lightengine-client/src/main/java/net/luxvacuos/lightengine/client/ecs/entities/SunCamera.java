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

package net.luxvacuos.lightengine.client.ecs.entities;

import net.luxvacuos.igl.vector.Matrix4d;
import net.luxvacuos.igl.vector.Vector2d;
import net.luxvacuos.lightengine.client.resources.CastRay;
import net.luxvacuos.lightengine.client.util.Maths;

public class SunCamera extends CameraEntity {

	private Vector2d center;

	private Matrix4d[] projectionArray;

	public SunCamera(Matrix4d[] projectionArray) {
		super("sun");
		this.projectionArray = projectionArray;
		center = new Vector2d(1024, 1024);
		castRay = new CastRay(this.getProjectionMatrix(), Maths.createViewMatrix(this), center, 2048, 2048);
		setProjectionMatrix(projectionArray[0]);
		setViewMatrix(Maths.createViewMatrix(this));
	}

	public void updateShadowRay(boolean inverted) {
		if (inverted)
			castRay.update(this.getProjectionMatrix(), Maths.createViewMatrixPos(this.getPosition(), Maths
					.createViewMatrixRot(getRotation().getX() + 180, getRotation().getY(), getRotation().getZ(), null)),
					center, 2048, 2048);
		else
			castRay.update(this.getProjectionMatrix(), Maths.createViewMatrix(this), center, 2048, 2048);
		setViewMatrix(Maths.createViewMatrix(this));
	}

	public void switchProjectionMatrix(int id) {
		setProjectionMatrix(this.projectionArray[id]);
	}

	public Matrix4d[] getProjectionArray() {
		return projectionArray;
	}

	public CastRay getDRay() {
		return castRay;
	}

}