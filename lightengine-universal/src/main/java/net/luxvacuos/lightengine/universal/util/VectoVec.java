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

package net.luxvacuos.lightengine.universal.util;

import org.joml.Quaternionf;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public final class VectoVec {

	private VectoVec() {
	}

	public static Vector3 toVec3(org.joml.Vector3f vec) {
		return new Vector3(vec.x(), vec.y(), vec.z());
	}

	public static org.joml.Vector3f toVec3(Vector3 vec) {
		return new org.joml.Vector3f(vec.x, vec.y, vec.z);
	}

	public static Quaternion toQuat4(Quaternionf q) {
		return new Quaternion(q.x, q.y, q.z, q.w);
	}

}
