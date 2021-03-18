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
import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.lecturestudio.swing.model.HueChooserModel;

public class HsbChooser extends JPanel {

	private static final long serialVersionUID = 266600722354400181L;

	private final EventListenerList listenerList = new EventListenerList();

	private HueChooser hueChooser;

	private SaturationBrightnessChooser sbChooser;


	public HsbChooser() {
		initComponents();
		initListeners();
	}

	public void setColor(Color color) {
		updateComponents(color);

		fireStateChanged();
	}

	public Color getColor() {
		float hue = hueChooser.getModel().getValue();
		float saturation = sbChooser.getModel().getSaturation();
		float brightness = sbChooser.getModel().getBrightness();

		return new Color(Color.HSBtoRGB(hue, saturation, brightness));
	}

	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);
	}

	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
	}

	protected void fireStateChanged() {
		ChangeEvent event = new ChangeEvent(this);
		ChangeListener[] listeners = listenerList.getListeners(ChangeListener.class);

		for (ChangeListener listener : listeners) {
			listener.stateChanged(event);
		}
	}

	private void updateComponents(Color color) {
		float[] hsb = new float[3];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);

		hueChooser.getModel().setValue(hsb[0]);
		sbChooser.getModel().setSaturation(hsb[1]);
		sbChooser.getModel().setBrightness(hsb[2]);
	}

	private void initComponents() {
		setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));

		hueChooser = new HueChooser(SwingConstants.VERTICAL);
		hueChooser.setPreferredSize(new Dimension(30, 256));

		sbChooser = new SaturationBrightnessChooser();
		sbChooser.setPreferredSize(new Dimension(256, 256));

		add(sbChooser);
		add(hueChooser);
	}

	private void initListeners() {
		hueChooser.getModel().addChangeListener(e -> {
			HueChooserModel model = (HueChooserModel) e.getSource();
			sbChooser.getModel().setHue(model.getValue());

			fireStateChanged();
		});

		sbChooser.getModel().addChangeListener(e -> fireStateChanged());
	}

}
