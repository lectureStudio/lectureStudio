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

package org.lecturestudio.swing.converter;

import java.awt.geom.AffineTransform;

import org.lecturestudio.core.beans.Converter;
import org.lecturestudio.core.geometry.Matrix;

public class MatrixConverter implements Converter<Matrix, AffineTransform> {

	public static final MatrixConverter INSTANCE = new MatrixConverter();


	@Override
	public AffineTransform to(Matrix m) {
		return new AffineTransform(m.getScaleX(), m.getShearY(), m.getShearX(),
				m.getScaleY(), m.getTranslateX(), m.getTranslateY());
	}

	@Override
	public Matrix from(AffineTransform t) {
		return new Matrix(t.getScaleX(), t.getShearY(), t.getShearX(),
				t.getScaleY(), t.getTranslateX(), t.getTranslateY());
	}
}
