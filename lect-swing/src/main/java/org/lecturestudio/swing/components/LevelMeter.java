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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class LevelMeter extends JPanel {

	private static final long serialVersionUID = 2543752419235067727L;

	private static final Color BG_COLOR = new Color(193, 220, 226);

	private static final Color MAX_COLOR = new Color(240, 90, 0);

	private static final Color MEDIUM_COLOR = new Color(250, 228, 29);

	private static final Color LOW_COLOR = new Color(31, 231, 36);

	private static final Color PEAK_COLOR = new Color(0, 120, 210);

	private static final int PEAK_SIZE = 3;

	private JComponent silentIndicator;

	private JComponent loudIndicator;

	private LevelView levelView;

	private int orientation;

	private double level = 0;

	private double peak = 0;


	public LevelMeter() {
		this(SwingConstants.HORIZONTAL);
	}

	public LevelMeter(int orientation) {
		setOrientation(orientation);

		init();
		initListeners();
	}

	public void setOrientation(int orientation) {
		if (orientation != SwingConstants.HORIZONTAL &&
			orientation != SwingConstants.VERTICAL) {
			throw new IllegalArgumentException();
		}

		this.orientation = orientation;
	}

	public void setSilentIndicator(JComponent indicator) {
		this.silentIndicator = indicator;

		layoutUI();
	}

	public void setLoudIndicator(JComponent indicator) {
		this.loudIndicator = indicator;

		layoutUI();
	}

	public void setLevel(double level) {
		this.level = level;
		this.peak = Math.max(this.peak, level);

		repaint();
	}

	private void init() {
		setLayout(new GridBagLayout());
		setOpaque(false);

		levelView = new LevelView();

		layoutUI();
	}

	private void layoutUI() {
		removeAll();

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		constraints.weightx = 1;
		constraints.weighty = 1;

		add(levelView, constraints);

		constraints.gridwidth = 1;
		constraints.gridy++;

		if (silentIndicator != null) {
			add(silentIndicator, constraints);
		}
		if (loudIndicator != null) {
			constraints.gridx++;
			constraints.anchor = GridBagConstraints.EAST;
			constraints.fill = GridBagConstraints.NONE;

			add(loudIndicator, constraints);
		}
	}

	private void initListeners() {
		levelView.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				repaint();
			}
		});
	}



	private class LevelView extends JPanel {

		private static final long serialVersionUID = 4485497190430019693L;


		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			Graphics2D g2d = (Graphics2D) g;
			Dimension size = getSize();

			g2d.setPaint(BG_COLOR);

			if (orientation == SwingConstants.VERTICAL) {
				int levelHeight = (int) (level * size.height);

				int loLevel = (int) (size.height * 0.7);
				int hiLevel = (int) (size.height * 0.3);

				g2d.fillRect(0, 0, size.width, size.height);
				g2d.setClip(0, 0, size.width, 1 - levelHeight);

				// green-yellow
				GradientPaint gradient = new GradientPaint(0, size.height, LOW_COLOR, 0, -loLevel, MEDIUM_COLOR);
				g2d.setPaint(gradient);
				g2d.fillRect(0, size.height, size.width, -loLevel);

				// yellow-red
				gradient = new GradientPaint(0, hiLevel, MEDIUM_COLOR, 0, -hiLevel, MAX_COLOR);
				g2d.setPaint(gradient);
				g2d.fillRect(0, hiLevel, size.width, -hiLevel);

				if (peak > 0) {
					int peakPos = (int) (peak * size.height);

					g2d.setClip(null);
					g2d.setPaint(PEAK_COLOR);
					g2d.fillRect(0, peakPos - PEAK_SIZE, size.width, PEAK_SIZE);
				}
			}
			else {
				int levelWidth = (int) (level * size.width);
				int loLevel = (int) (size.width * 0.7);
				int hiLevel = (int) (size.width * 0.3);

				g2d.fillRect(0, 0, size.width, size.height);
				g2d.setClip(0, 0, levelWidth, size.height);

				// green-yellow
				GradientPaint gradient = new GradientPaint(0, 0, LOW_COLOR, loLevel, 0, MEDIUM_COLOR);
				g2d.setPaint(gradient);
				g2d.fillRect(0, 0, loLevel, size.height);

				// yellow-red
				gradient = new GradientPaint(loLevel, 0, MEDIUM_COLOR, loLevel + hiLevel, 0, MAX_COLOR);
				g2d.setPaint(gradient);
				g2d.fillRect(loLevel, 0, hiLevel, size.height);

				if (peak > 0) {
					int peakPos = (int) (peak * size.width);

					g2d.setClip(null);
					g2d.setPaint(PEAK_COLOR);
					g2d.fillRect(peakPos - PEAK_SIZE, 0, PEAK_SIZE, size.height);
				}
			}
		}
	}
}
