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

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.lecturestudio.swing.model.HueChooserModel;
import org.lecturestudio.swing.ui.HueChooserUI;

public class HueChooser extends JComponent {

	private static final long serialVersionUID = 496198513380972971L;

	private static final String uiClassID = "HueChooserUI";

	private HueChooserModel model = new HueChooserModel();

	private int orientation = SwingConstants.VERTICAL;


	public HueChooser(int orientation) {
		this.orientation = orientation;

		updateUI();
	}

	public String getUIClassID() {
		return uiClassID;
	}

	public void setUI(HueChooserUI ui) {
		super.setUI(ui);
	}

	public HueChooserUI getUI() {
		return (HueChooserUI) ui;
	}

	public void updateUI() {
		if (UIManager.get(getUIClassID()) != null) {
			setUI((HueChooserUI) UIManager.getUI(this));
		}
		else {
			setUI(new HueChooserUI());
		}
	}

	public HueChooserModel getModel() {
		return model;
	}

	public int getOrientation() {
		return orientation;
	}

	@Override
	public void setPreferredSize(Dimension size) {
		Dimension newSize = new Dimension(size);

		if (orientation == SwingConstants.HORIZONTAL) {
			newSize.width += 10;
		}
		else {
			newSize.height += 10;
		}

		super.setPreferredSize(newSize);
	}

}
