/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.config;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.geometry.Rectangle2D;

public class DocumentTemplateConfiguration {

	private final StringProperty templatePath = new StringProperty();

	private final ObjectProperty<Rectangle2D> bounds = new ObjectProperty<>(new Rectangle2D(0.05, 0.05, 0.9, 0.9));


	public String getTemplatePath() {
		return templatePath.get();
	}

	public void setTemplatePath(String path) {
		templatePath.set(path);
	}

	public StringProperty templatePathProperty() {
		return templatePath;
	}

	public Rectangle2D getBounds() {
		return bounds.get();
	}

	public void setBounds(Rectangle2D rect) {
		bounds.set(rect);
	}

	public ObjectProperty<Rectangle2D> boundsProperty() {
		return bounds;
	}
}
