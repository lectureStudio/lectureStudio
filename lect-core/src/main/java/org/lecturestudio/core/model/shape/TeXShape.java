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
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.listener.TextChangeListener;
import org.lecturestudio.core.text.TeXFont;
import org.lecturestudio.core.text.TextAttributes;
import org.lecturestudio.core.text.TeXFont.Type;

/**
 * A shape representing TeX input. It has a textbox handle to associate it
 * with some textbox widget on the GUI.
 * 
 * @author Alex Andres
 */
public class TeXShape extends Shape implements TextBoxShape<TeXFont> {

	private final List<TextChangeListener<TeXShape>> textListeners = new ArrayList<>();

	private final StringProperty text = new StringProperty("");
	
	private final TextAttributes attributes = new TextAttributes();
	
	private Color textColor = Color.BLACK;
	
	private TeXFont font = new TeXFont(Type.SERIF, 24);
	
	
	public TeXShape() {
		super();
		setBounds(0, 0, 1.0, 0.15);
	}
	
	public TeXShape(byte[] input) throws IOException {
		setBounds(0, 0, 1.0, 0.15);
		parseFrom(input);
	}
	
	@Override
	public void setText(String text) {
		if (getText().equals(text)) {
			return;
		}
		
		this.text.set(text);
		
		fireTextChange();
		fireShapeChanged(null);
	}
	
	/**
	 * Returns LaTeX text.
	 * 
	 * @return The LaTeX text.
	 */
	public String getText() {
		return text.get();
	}
	
	public StringProperty textProperty() {
		return text;
	}
	
	@Override
	public void setTextAttributes(TextAttributes attributes) {
		// No attributes to set yet.
	}
	
	public TextAttributes getTextAttributes() {
		return attributes;
	}
	
	@Override
	public void setTextColor(Color color) {
		if (textColor.equals(color)) {
			return;
		}
		
		this.textColor = color;
		
		fireTextFontChange();
		fireShapeChanged(null);
	}
	
	/**
	 * Returns the font color
	 * 
	 * @return
	 */
	public Color getTextColor() {
		return textColor;
	}
	
	@Override
	public void setFont(TeXFont font) {
		if (this.font.equals(font)) {
			return;
		}
		
		this.font = font;
		
		fireTextFontChange();
		fireShapeChanged(null);
	}
	
	/**
	 * Returns the TeX font.
	 * 
	 * @return
	 */
	public TeXFont getFont() {
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
	
	public Point2D getLocation() {
		return getBounds().getLocation();
	}

	/**
	 * Sets the size of the shape
	 * 
	 * @param size
	 */
	public void setSize(Dimension2D size) {
		getBounds().setRect(getBounds().getX(), getBounds().getY(), size.getWidth(), size.getHeight());
		fireShapeChanged(null);
	}

	public void setBounds(Rectangle2D rect) {
		super.setBounds(rect);
		fireShapeChanged(null);
	}

	public Dimension2D getSize() {
		return new Dimension2D(getBounds().getWidth(), getBounds().getHeight());
	}
	
	public void setOnRemove() {
		fireTextRemoved();
	}

	public void addTextChangeListener(TextChangeListener<TeXShape> listener) {
		textListeners.add(listener);
	}

	public void removeTextChangeListener(TextChangeListener<TeXShape> listener) {
		textListeners.remove(listener);
	}

	private void fireTextChange() {
		for (TextChangeListener<TeXShape> listener : textListeners) {
			listener.textChanged(this);
		}
	}
	
	private void fireTextFontChange() {
		for (TextChangeListener<TeXShape> listener : textListeners) {
			listener.textFontChanged(this);
		}
	}
	
	private void fireTextLocationChange() {
		for (TextChangeListener<TeXShape> listener : textListeners) {
			listener.textLocationChanged(this);
		}
	}
	
	private void fireTextRemoved() {
		for (TextChangeListener<TeXShape> listener : textListeners) {
			listener.textRemoved(this);
		}
	}
	
	@Override
	public TeXShape clone() {
		TeXShape shape = new TeXShape();
		shape.setBounds(getBounds().clone());
		shape.setText(getText());
		shape.setTextColor(getTextColor().clone());
		shape.setFont(getFont().clone());
		shape.setKeyEvent(getKeyEvent());

		return shape;
	}

	@Override
	public byte[] toByteArray() throws IOException {
		byte[] textData = getText().getBytes();
		
		int length = 16 + 12 + 4 + textData.length;
		
		ByteBuffer buffer = createBuffer(length);
		
		// Location: 16 bytes.
		Point2D location = getBounds().getLocation();
		buffer.putDouble(location.getX());
		buffer.putDouble(location.getY());
		
		// Text attributes: 12 bytes.
		buffer.putInt(getFont().getType().getValue());
		buffer.putFloat(getFont().getSize());
		buffer.putInt(getTextColor().getRGBA());
		
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
		
		// Text attributes
		int textType = buffer.getInt();
		float textSize = buffer.getFloat();
		
		TeXFont font = new TeXFont(TeXFont.Type.fromValue(textType), textSize);
		Color color = new Color(buffer.getInt());
		
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
	}

}
