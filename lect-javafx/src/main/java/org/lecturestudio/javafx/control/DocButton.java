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

package org.lecturestudio.javafx.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;

import org.lecturestudio.core.model.RecentDocument;

public class DocButton extends Button {

	private static final String DEFAULT_STYLE_CLASS = "doc-button";

	private ObjectProperty<Node> documentIcon;

	private ObjectProperty<RecentDocument> document;
	
	
	public DocButton() {
		super();

		initialize();
	}
	
	public DocButton(RecentDocument doc) {
		super();
		
		setDocument(doc);
		initialize();
	}
	
	public final ObjectProperty<RecentDocument> documentProperty() {
		if (document == null) {
			document = new SimpleObjectProperty<>(this, "document", null);
		}
		return document;
	}

	public final void setDocument(RecentDocument doc) {
		documentProperty().setValue(doc);
	}

	public final RecentDocument getDocument() {
		return document == null ? null : document.getValue();
	}

	public final ObjectProperty<Node> iconProperty() {
		if (documentIcon == null) {
			documentIcon = new SimpleObjectProperty<>(this, "icon", null);
		}
		return documentIcon;
	}

	public final void setIcon(Node icon) {
		iconProperty().setValue(icon);
	}

	public final Node getIcon() {
		return documentIcon == null ? null : documentIcon.getValue();
	}

	@Override
	public String getUserAgentStylesheet() {
		return getClass().getResource("/resources/css/doc-button.css").toExternalForm();
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new DocButtonSkin(this);
	}

	private void initialize() {
		getStyleClass().add(DEFAULT_STYLE_CLASS);
	}
}
