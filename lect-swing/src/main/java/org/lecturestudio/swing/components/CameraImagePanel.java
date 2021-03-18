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

package org.lecturestudio.swing.components;

import com.google.common.eventbus.Subscribe;

import org.lecturestudio.core.camera.bus.event.CameraClientEvent;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.bus.event.DisplayConfigEvent;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.swing.converter.ColorConverter;

import javax.swing.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Map;

public class CameraImagePanel extends JPanel {

	private static final long serialVersionUID = 7581815825774848151L;

	private final ApplicationContext context;

	/**
	 * Image currently being displayed.
	 */
	private BufferedImage image = null;

	private Color bgColor;

	private Map<String, Number> stats;
	
	private String message;


	public CameraImagePanel(ApplicationContext context) {
		this.context = context;
		this.message = context.getDictionary().get("connecting");
		
		loadBackgroundColor();
		
		ApplicationBus.register(this);
	}

	@Subscribe
	public void onEvent(final DisplayConfigEvent event) {
		loadBackgroundColor();
	}
	
	@Subscribe
	public void onEvent(final CameraClientEvent event) {
		if (event.getState() == ExecutableState.Initializing)
			this.message = context.getDictionary().get("initializing");
		else
			this.message = context.getDictionary().get("no.connection");
		
		repaint();
	}

	public void paintImage(BufferedImage image) {
		paintImage(image, null);
	}

	public void paintImage(BufferedImage image, Map<String, Number> stats) {
		if (this.image != null) {
			this.image.flush();
			this.image = null;
		}
		this.image = image;
		this.stats = stats;

		repaint();
	}

	public void paintStats(Graphics2D g) {
		g.setColor(new Color(0, 0, 0, 70));
		g.fillRect(0, 0, 120, 90);
		g.setColor(Color.WHITE);

		long total = 0;
		float kbps = (Float) stats.get("kbps");
		
		if (stats.get("total") != null)
			total = ((Long) stats.get("total")) / 1024;

		g.drawString("fps: \t" + stats.get("fps"), 10, 15);
		g.drawString("kbps: \t" + (long) kbps, 10, 35);
		g.drawString("total KiB: \t" + total, 10, 55);
		g.drawString("latency: \t" + stats.get("latency") + " ms", 10, 75);
	}
	
	public void dispose() {
		ApplicationBus.unregister(this);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		if (image == null) {
			paintBlankPanel(g2);
		}
		else {
			paintImage(g2);
		}
	}

	private void paintBlankPanel(Graphics2D g2) {
		double scale = 1.5;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setBackground(Color.BLACK);
		g2.fillRect(0, 0, getWidth(), getHeight());

		int cx = (int) ((getWidth() - 70 * scale) / 2 / scale);
		int cy = (int) ((getHeight() - 70 * scale) / 2 / scale);

		g2.setTransform(AffineTransform.getScaleInstance(scale, scale));
		g2.setStroke(new BasicStroke(2));
		g2.setColor(Color.LIGHT_GRAY);
		g2.fillRoundRect(cx, cy, 70, 40, 10, 10);
		g2.setColor(Color.WHITE);
		g2.fillOval(cx + 5, cy + 5, 30, 30);
		g2.setColor(Color.LIGHT_GRAY);
		g2.fillOval(cx + 10, cy + 10, 20, 20);
		g2.setColor(Color.WHITE);
		g2.fillOval(cx + 12, cy + 12, 16, 16);
		g2.fillRoundRect(cx + 50, cy + 5, 15, 10, 5, 5);
		g2.fillRect(cx + 63, cy + 25, 7, 2);
		g2.fillRect(cx + 63, cy + 28, 7, 2);
		g2.fillRect(cx + 63, cy + 31, 7, 2);

		g2.setColor(Color.GRAY);
		g2.setStroke(new BasicStroke(2));
		g2.drawLine(cx + 10, cy + 10, cx + 30, cy + 30);
		g2.drawLine(cx + 10, cy + 30, cx + 30, cy + 10);

		FontMetrics metrics = g2.getFontMetrics(getFont());
		int w = metrics.stringWidth(message);
		int h = metrics.getHeight();

		g2.setColor(Color.WHITE);
		g2.drawString(message, (int) ((getWidth() - w * scale) / 2 / scale), cy + h + 45);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	private void paintImage(Graphics2D g2) {
		int w = getWidth();
		int h = getHeight();
		int x = 0;
		int y = 0;

		g2.setColor(bgColor);
		g2.fillRect(0, 0, w, h);

		float panelRatio = h / (float) w;
		float imageRatio = image.getHeight() / (float) image.getWidth();

		if (panelRatio > imageRatio) {
			h = (int) (image.getHeight() / (float) image.getWidth() * w);
			y = (getHeight() - h) / 2;
		}
		else {
			w = (int) (image.getWidth() / (float) image.getHeight() * h);
			x = (getWidth() - w) / 2;
		}

		g2.drawImage(image, x, y, w, h, null);

		if (stats != null) {
			g2.translate(x, y);
			paintStats(g2);
		}
	}

	private void loadBackgroundColor() {
		Configuration config = context.getConfiguration();
		org.lecturestudio.core.graphics.Color color = config.getDisplayConfig().getBackgroundColor();
		this.bgColor = ColorConverter.INSTANCE.to(color);
	}

}
