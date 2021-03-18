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

package org.lecturestudio.swing.font;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.swing.Icon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FontAwesomeIcon implements Icon {

	private static final Logger LOG = LogManager.getLogger(FontAwesomeIcon.class);
	
	private static final String FONT_PATH = "resources/fonts/fontawesome-webfont.ttf";
	
	private static final Font AWESOME;
	
	static {
		try {
			InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(FONT_PATH);
			AWESOME = Font.createFont(Font.TRUETYPE_FONT, stream);
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(AWESOME);
			stream.close();
		}
		catch (Exception e) {
			LOG.error(e);
			
			throw new RuntimeException(e);
		}
	}
	
	
	private int width;
    private int height;
    private Color color;
    private Font font;
    private BufferedImage buffer;
    
	private FontAwesome fontAwesome;
	
	
	public FontAwesomeIcon(FontAwesome fontAwesome) {
		this(fontAwesome, 16);
	}
	
	public FontAwesomeIcon(FontAwesome fontAwesome, int fontSize) {
		this(fontAwesome, fontSize, Color.BLACK);
	}
	
	public FontAwesomeIcon(FontAwesome fontAwesome, int fontSize, Color color) {
		this.fontAwesome = fontAwesome;
		this.color = color;
		
		setFontSize(fontSize);
	}
    
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.drawImage(buffer, x, y, null);
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}

	private void setFontSize(int size) {
		if (size > 0) {
			font = AWESOME.deriveFont(Font.PLAIN, size);

			BufferedImage tmp = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = GraphicsEnvironment.getLocalGraphicsEnvironment().createGraphics(tmp);
			g2.setFont(font);
			
			this.width = g2.getFontMetrics().charWidth(fontAwesome.getCode()) + 1;
			this.height = g2.getFontMetrics().getHeight();
			
			g2.dispose();
			
			createBuffer();
		}
	}
	
	private void createBuffer() {
		if (buffer != null) {
			buffer.flush();
		}
		
		buffer = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2 = (Graphics2D) buffer.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setFont(font);
		g2.setColor(color);
		g2.drawString(String.valueOf(fontAwesome.getCode()), 0, height - g2.getFontMetrics().getDescent());
		g2.dispose();
	}
	
}
