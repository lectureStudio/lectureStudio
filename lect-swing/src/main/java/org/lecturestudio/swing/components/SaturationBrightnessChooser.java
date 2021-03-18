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

import javax.swing.JComponent;
import javax.swing.UIManager;

import org.lecturestudio.swing.model.SaturationBrightnessChooserModel;
import org.lecturestudio.swing.ui.SaturationBrightnessChooserUI;

public class SaturationBrightnessChooser extends JComponent {

	private static final long serialVersionUID = -8986453326423849790L;

	private static final String uiClassID = "SaturationBrightnessChooserUI";

	private final SaturationBrightnessChooserModel model = new SaturationBrightnessChooserModel();


	public SaturationBrightnessChooser() {
		updateUI();
	}

	public String getUIClassID() {
		return uiClassID;
	}

	public void setUI(SaturationBrightnessChooserUI ui) {
		super.setUI(ui);
	}

	public SaturationBrightnessChooserUI getUI() {
		return (SaturationBrightnessChooserUI) ui;
	}

	public void updateUI() {
		if (UIManager.get(getUIClassID()) != null) {
			setUI((SaturationBrightnessChooserUI) UIManager.getUI(this));
		}
		else {
			setUI(new SaturationBrightnessChooserUI());
		}
	}

	public SaturationBrightnessChooserModel getModel() {
		return model;
	}

}
