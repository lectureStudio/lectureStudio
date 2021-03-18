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

package org.lecturestudio.swing.beans;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.lecturestudio.core.beans.StringProperty;

public class TextFieldProperty extends StringProperty {

	private final JTextComponent textComponent;


	public TextFieldProperty(JTextComponent textComponent) {
		final DocumentListener listener = new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				SwingUtilities.invokeLater(() -> TextFieldProperty.super
						.set(textComponent.getText()));
			}
		};

		textComponent.addPropertyChangeListener("document", e -> {
			Document d1 = (Document) e.getOldValue();
			Document d2 = (Document) e.getNewValue();

			if (nonNull(d1)) {
				d1.removeDocumentListener(listener);
			}
			if (nonNull(d2)) {
				d2.addDocumentListener(listener);
			}

			listener.changedUpdate(null);
		});

		this.textComponent = textComponent;
		this.textComponent.getDocument().addDocumentListener(listener);
	}

	@Override
	public String get() {
		return textComponent.getText();
	}

	@Override
	public void set(String value) {
		if (isNull(super.get()) || !super.get().equals(value)) {
			textComponent.setText(value);

			super.set(value);
		}
	}
}
