/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
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
 */

package org.lecturestudio.javafx.beans.converter;

import javafx.scene.transform.Transform;

import org.lecturestudio.core.beans.Converter;
import org.lecturestudio.core.geometry.Matrix;

public class MatrixConverter implements Converter<Matrix, Transform> {

	public static final MatrixConverter INSTANCE = new MatrixConverter();


	@Override
	public Transform to(Matrix m) {
		return Transform.affine(m.getScaleX(), m.getShearY(), m.getShearX(),
				m.getScaleY(), m.getTranslateX(), m.getTranslateY());
	}

	@Override
	public Matrix from(Transform t) {
		return new Matrix(t.getMxx(), t.getMyx(), t.getMxy(), t.getMyy(), t.getTx(), t.getTy());
	}
}
