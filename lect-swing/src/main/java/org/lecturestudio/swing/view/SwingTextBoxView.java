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

package org.lecturestudio.swing.view;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicTextAreaUI;

import org.lecturestudio.core.beans.ChangeListener;
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.shape.TextShape;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.text.TextAttributes;
import org.lecturestudio.core.view.TextBoxView;
import org.lecturestudio.swing.components.TextInputPageObject;
import org.lecturestudio.swing.converter.ColorConverter;
import org.lecturestudio.swing.converter.FontConverter;

public class SwingTextBoxView extends TextInputPageObject<TextShape> implements TextBoxView {

	private static final java.awt.Color THEME_COLOR = new java.awt.Color(253, 224, 71, 125);

	private JTextArea textArea;

	private double fontSize;

	private DocumentListener documentListener;

	private ChangeListener<String> textChangeListener;


	public SwingTextBoxView() {
		super();
	}

	@Override
	public void setFocus(boolean focus) {
		super.setFocus(focus);

		if (focus) {
			textArea.requestFocus();
		}
	}

	@Override
	public String getText() {
		return nonNull(getPageShape()) ? getPageShape().getText() : null;
	}

	@Override
	public void setText(String text) {
		if (nonNull(getPageShape())) {
			getPageShape().setText(text);
		}
	}

	@Override
	public TextAttributes getTextAttributes() {
		return nonNull(getPageShape()) ? getPageShape().getTextAttributes() : null;
	}

	@Override
	public void setTextAttributes(TextAttributes attributes) {
		if (nonNull(getPageShape())) {
			getPageShape().setTextAttributes(attributes);
		}
	}

	@Override
	public Color getTextColor() {
		return nonNull(getPageShape()) ? getPageShape().getTextColor() : null;
	}

	@Override
	public void setTextColor(Color color) {
		if (nonNull(getPageShape())) {
			getPageShape().setTextColor(color);
		}
	}

	@Override
	public Font getTextFont() {
		return nonNull(getPageShape()) ? getPageShape().getFont() : null;
	}

	@Override
	public void setTextFont(Font font) {
		if (nonNull(getPageShape())) {
			getPageShape().setFont(font);

			TextAttributes attributes = font.getTextAttributes();

			if (nonNull(attributes)) {
				getPageShape().getTextAttributes().setStrikethrough(attributes.isStrikethrough());
				getPageShape().getTextAttributes().setUnderline(attributes.isUnderline());
			}
		}
	}

	@Override
	public void dispose() {
		if (nonNull(textChangeListener)) {
			getPageShape().textProperty().removeListener(textChangeListener);
		}
		if (nonNull(documentListener)) {
			textArea.getDocument().removeDocumentListener(documentListener);
		}

		super.dispose();
	}

	@Override
	protected void updateToShapeContent(TextShape shape) {
		if (isNull(shape)) {
			return;
		}

		fontSize = shape.getFont().getSize();

		textArea.setFont(FontConverter.INSTANCE.to(shape.getFont()));
		textArea.setText(shape.getText());

		textChangeListener = (observable, oldValue, newValue) -> {
			if (!textArea.getText().equals(newValue)) {
				textArea.setText(newValue);
			}
		};

		documentListener = new DocumentListener() {

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
				shape.setText(textArea.getText());
			}
		};

		shape.textProperty().addListener(textChangeListener);
		textArea.getDocument().addDocumentListener(documentListener);
	}

	@Override
	protected void updateToTransform(AffineTransform transform) {
		TextShape shape = getPageShape();
		Insets padding = getInsets();
		Rectangle textBounds = textArea.getBounds();
		Rectangle2D shapeRect = shape.getBounds();

		double xOffset = textBounds.getMinX() + padding.left + 4;
		double yOffset = textBounds.getMinY() + padding.top + 1;

		double s = transform.getScaleX();
		double tx = transform.getTranslateX();
		double ty = transform.getTranslateY();
		double x = Math.ceil((shapeRect.getX() - tx) * s) - xOffset;
		double y = Math.ceil((shapeRect.getY() - ty) * s) - yOffset;

		Font font = shape.getFont().clone();
		font.setSize(fontSize * s);

		textArea.setFont(FontConverter.INSTANCE.to(font));

		setLocation((int) x, (int) y);
		updateContentSize();
	}

	@Override
	protected java.awt.Color getThemeColor() {
		return THEME_COLOR;
	}

	@Override
	protected JComponent createContent() {
		textArea = new JTextArea();
		textArea.setUI(new BasicTextAreaUI());
		textArea.setOpaque(false);
		textArea.setForeground(new java.awt.Color(0, 0, 0, 0));
		textArea.setFocusable(true);
		textArea.requestFocus();
		textArea.addCaretListener(e -> updateContentSize());
		textArea.addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {
				setFocus(true);
			}

			@Override
			public void focusLost(FocusEvent e) {
				setFocus(false);
			}
		});
		textArea.getDocument().addDocumentListener(new DocumentListener() {

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
				updateShapeSize(textArea.getText());
			}
		});

		return textArea;
	}

	@Override
	protected void onRelocateShape(Point2D location) {
		TextShape shape = getPageShape();

		getPageShape().setLocation(location);

		// Add some margin to the changed location.
		AffineTransform transform = getPageTransform();
		double m = 10 / transform.getScaleX();

		shape.getDirtyBounds().setLocation(location.getX() - m, location.getY() - m);
	}

	private void updateShapeSize(String text) {
		TextShape shape = getPageShape();
		AffineTransform transform = getPageTransform();

		if (isNull(transform)) {
			return;
		}

		AffineTransform affinetransform = new AffineTransform();
		FontRenderContext frc = new FontRenderContext(affinetransform, true, true);

		org.lecturestudio.core.text.Font f = shape.getFont().clone();
		f.setSize(f.getSize() * transform.getScaleX());

		java.awt.Font font = FontConverter.INSTANCE.to(f);

		Map<TextAttribute, Object> attrs = (Map<TextAttribute, Object>) font.getAttributes();
		attrs.put(TextAttribute.UNDERLINE, toAwtFontUnderline(shape.isUnderline()));
		attrs.put(TextAttribute.STRIKETHROUGH, shape.isStrikethrough());
		attrs.put(TextAttribute.FOREGROUND, ColorConverter.INSTANCE.to(shape.getTextColor()));

		String[] lines = text.split("\\n");

		double layoutX = 0;
		double layoutY = 0;

		for (String line : lines) {
			if (line.isEmpty()) {
				line = " ";
			}

			TextLayout layout = new TextLayout(line, attrs, frc);

			layoutX = Math.max(layoutX, layout.getBounds().getWidth());
			layoutY += layout.getAscent() + layout.getDescent() + layout.getLeading();
		}

		Rectangle2D shapeBounds = shape.getBounds();
		Rectangle2D dirtyBounds = shapeBounds.clone();

		shapeBounds.setSize(layoutX / transform.getScaleX(), layoutY / transform.getScaleY());
		dirtyBounds.union(shapeBounds);

		// Add some margin to the changed rectangle.
		double m = 10 / transform.getScaleX();
		dirtyBounds.setRect(dirtyBounds.getX() - m, dirtyBounds.getY() - m,
				dirtyBounds.getWidth() + 2 * m, dirtyBounds.getHeight() + 2 * m);

		shape.setDirtyBounds(dirtyBounds);
	}

	public static Number toAwtFontUnderline(boolean underline) {
		if (underline) {
			return TextAttribute.UNDERLINE_ON;
		}
		else {
			return -1;
		}
	}
}
