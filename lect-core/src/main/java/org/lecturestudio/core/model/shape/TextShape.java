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

package org.lecturestudio.core.model.shape;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.listener.TextChangeListener;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.text.FontPosture;
import org.lecturestudio.core.text.FontWeight;
import org.lecturestudio.core.text.TextAttributes;

/**
 * A shape representing a text input. It has a textbox handle to associate it with some textbox widget on the GUI.
 * 
 * @author Alex Andres
 * @author Tobias
 */
public class TextShape extends Shape implements TextBoxShape<Font> {

	private final List<TextChangeListener<TextShape>> textListeners = new ArrayList<>();
	
	private final StringProperty text = new StringProperty("");

	private Color color = Color.BLACK;

	private Font font = new Font("Verdana", 24);
	
	private TextAttributes attributes = new TextAttributes();

	/**
	 * Creates a {@link TextShape}.
	 * (Calls the default constructor of {@link Shape} and calls {@link #initProperties()}.)
	 */
	public TextShape() {
		super();
		
		initProperties();
	}

	/**
	 * Creates a new {@link TextShape} with the specified input byte array containing the data for the {@link TeXShape}.
	 * (Calls {@link #initProperties().)
	 *
	 * @param input The input byte array.
	 */
	public TextShape(byte[] input) throws IOException {
		initProperties();
		parseFrom(input);
	}

	/**
	 * Get the text.
	 *
	 * @return The text.
	 */
	public String getText() {
		return text.get();
	}

	/**
	 * Get the text property.
	 *
	 * @return The text property.
	 */
	public StringProperty textProperty() {
		return text;
	}

	@Override
	public void setText(String text) {
		if (getText().equals(text)) {
			return;
		}
		
		this.text.set(text);
	}

	@Override
	public void setTextAttributes(TextAttributes attributes) {
		if (this.attributes.equals(attributes)) {
			return;
		}
		
		this.attributes = attributes;
		
		fireTextFontChange();
		fireShapeChanged(null);
	}

	/**
	 * Get the text attributes.
	 *
	 * @return The text attributes.
	 */
	public TextAttributes getTextAttributes() {
		return attributes;
	}
	
	@Override
	public void setTextColor(Color color) {
		if (this.color.equals(color)) {
			return;
		}

		this.color = color;
		
		fireTextFontChange();
		fireShapeChanged(null);
	}

	/**
	 * Get the font color.
	 *
	 * @return The font color.
	 */
	public Color getTextColor() {
		return color;
	}
	
	@Override
	public void setFont(Font font) {
		if (this.font.equals(font)) {
			return;
		}
		
		this.font = font;
		
		fireTextFontChange();
		fireShapeChanged(null);
	}
	
	/**
	 * Get the  font.
	 *
	 * @return The font.
	 */
	public Font getFont() {
		return font;
	}
	
	@Override
	public void setLocation(Point2D location) {
		if (getBounds().getLocation().equals(location)) {
			return;
		}
		
		if (location != null) {
			getBounds().setLocation(location.getX(), location.getY());
			
			fireTextLocationChange();
			fireShapeChanged(null);
		}
	}

	/**
	 * Get the location of the bounding rectangle of the shape.
	 *
	 * @return The location of the bounding rectangle of the shape.
	 */
	public Point2D getLocation() {
		return getBounds().getLocation();
	}
	
	/**
	 * Returns whether text is underlined or not.
	 * 
	 * @return {@code true} if text is underlined, otherwise {@code false}.
	 */
	public boolean isUnderline() {
		return attributes.isUnderline();
	}
	
	/**
	 * Underline the text.
	 */
	public void setUnderline(boolean underline) {
		this.attributes.setAttribute("underline", underline);
	}

	/**
	 * Specifies whether text is struck out or not.
	 * 
	 * @return {@code true} if text is struck out, otherwise {@code false}.
	 */
	public boolean isStrikethrough() {
		return attributes.isStrikethrough();
	}
	
	/**
	 * Strike through the text.
	 */
	public void setStrikethrough(boolean strikethrough) {
		this.attributes.setAttribute("strikethrough", strikethrough);
	}

	@Override
	public void setBounds(Rectangle2D rect) {
		super.setBounds(rect);
		fireShapeChanged(null);
	}

	public void setOnRemove() {
		fireTextRemoved();
	}

	/**
	 * Adds a new text change listener to {@link #textListeners}.
	 *
	 * @param listener The text change listener to add.
	 */
	public void addTextChangeListener(TextChangeListener<TextShape> listener) {
		textListeners.add(listener);
	}

	/**
	 * Removes the specified text change listener from {@link #textListeners}.
	 *
	 * @param listener The text change listener to remove.
	 */
	public void removeTextChangeListener(TextChangeListener<TextShape> listener) {
		textListeners.remove(listener);
	}

	public void fireTextChange() {
		for (TextChangeListener<TextShape> listener : textListeners) {
			listener.textChanged(this);
		}
	}
	
	public void fireTextFontChange() {
		for (TextChangeListener<TextShape> listener : textListeners) {
			listener.textFontChanged(this);
		}
	}
	
	public void fireTextLocationChange() {
		for (TextChangeListener<TextShape> listener : textListeners) {
			listener.textLocationChanged(this);
		}
	}
	
	public void fireTextRemoved() {
		for (TextChangeListener<TextShape> listener : textListeners) {
			listener.textRemoved(this);
		}
	}
	
	@Override
	public TextShape clone() {
		TextShape shape = new TextShape();
		shape.setBounds(getBounds().clone());
		shape.setTextColor(getTextColor().clone());
		shape.setFont(getFont().clone());
		shape.setText(getText());
		shape.setStrikethrough(isStrikethrough());
		shape.setUnderline(isUnderline());
		shape.setKeyEvent(getKeyEvent());

		return shape;
	}

	@Override
	public byte[] toByteArray() throws IOException {
		byte[] fontFamily = font.getFamilyName().getBytes();
		byte[] textData = getText().getBytes();
		
		int length = 16 + 4 + 14 + fontFamily.length + 2 + 4 + textData.length;
		
		ByteBuffer buffer = createBuffer(length);
		
		// Location: 16 bytes.
		Point2D location = getBounds().getLocation();
		buffer.putDouble(location.getX());
		buffer.putDouble(location.getY());

		buffer.putInt(getTextColor().getRGBA());

		// Font: 14 + X bytes.
		buffer.putInt(fontFamily.length);
		buffer.put(fontFamily);
		buffer.putDouble(font.getSize());
		buffer.put((byte) font.getPosture().ordinal());
		buffer.put((byte) font.getWeight().ordinal());

		// Text attributes: 2 bytes.
		buffer.put((byte) (isStrikethrough() ? 1 : 0));
		buffer.put((byte) (isUnderline() ? 1 : 0));
		
		// Text: 4 + X bytes.
		buffer.putInt(textData.length);
		
		if (textData.length > 0) {
			buffer.put(textData);
		}
		
		return buffer.array();
	}

	@Override
	protected void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = createBuffer(input);
		
		// Location
		double x = buffer.getDouble();
		double y = buffer.getDouble();

		// Color
		Color color = new Color(buffer.getInt());

		// Font
		int familyLength = buffer.getInt();
		byte[] familyStr = new byte[familyLength];
		buffer.get(familyStr);
		double fontSize = buffer.getDouble();
		FontPosture posture = FontPosture.values()[buffer.get()];
		FontWeight weight = FontWeight.values()[buffer.get()];

		Font font = new Font(new String(familyStr), fontSize);
		font.setPosture(posture);
		font.setWeight(weight);

		// Text attributes
		boolean strikethrough = buffer.get() > 0;
		boolean underline = buffer.get() > 0;
		
		// Text
		int textLength = buffer.getInt();
		
		if (textLength > 0) {
			byte[] textData = new byte[textLength];
			buffer.get(textData);
			
			setText(new String(textData));
		}
		
		setLocation(new Point2D(x, y));
		setTextColor(color);
		setFont(font);
		setStrikethrough(strikethrough);
		setUnderline(underline);
	}

	private void initProperties() {
		text.addListener((observable, oldValue, newValue) -> {
			fireTextChange();
			fireShapeChanged(null);
		});
	}

}
