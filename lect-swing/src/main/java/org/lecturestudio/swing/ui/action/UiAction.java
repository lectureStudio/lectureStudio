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

package org.lecturestudio.swing.ui.action;

import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

public abstract class UiAction extends AbstractAction {

	private static final long serialVersionUID = 3863363394041406912L;


	public UiAction() {
		super();
	}

	public UiAction(String text) {
		super(text);
	}

	public UiAction(String text, KeyStroke keyStroke) {
		super(text);

		putValue(ACCELERATOR_KEY, keyStroke);
	}

	public void setAccelerator(KeyStroke keyStroke) {
		putValue(ACCELERATOR_KEY, keyStroke);

		String desc = (String) getValue(SHORT_DESCRIPTION);

		if (desc != null) {
			desc += "    " + getAcceleratorText(keyStroke);
			putValue(SHORT_DESCRIPTION, desc);
		}
	}

	public void setText(String text) {
		putValue(NAME, text);
	}

	public void setIcon(Icon icon) {
		putValue(SMALL_ICON, icon);
	}

	public void setTooltipText(String tooltipText) {
		KeyStroke keyStroke = (KeyStroke) getValue(ACCELERATOR_KEY);
		String desc = tooltipText;

		if (keyStroke != null) {
			desc += "    " + getAcceleratorText(keyStroke);
		}

		putValue(SHORT_DESCRIPTION, desc);
	}

	/**
	 * Converts KeyStroke to readable string format. This can be used to show
	 * the accelerator along with the tooltip for toolbar buttons.
	 * 
	 * This code is extracted from javax.swing.plaf.basic.BasicMenuItemUI class.
	 */
	public static String getAcceleratorText(KeyStroke accelerator) {
		String acceleratorDelimiter = UIManager.getString("MenuItem.acceleratorDelimiter");

		if (acceleratorDelimiter == null)
			acceleratorDelimiter = "+";

		String acceleratorText = "";

		if (accelerator != null) {
			int modifiers = accelerator.getModifiers();
			if (modifiers > 0) {
				acceleratorText = KeyEvent.getModifiersExText(modifiers);
				acceleratorText += acceleratorDelimiter;
			}

			int keyCode = accelerator.getKeyCode();

			if (keyCode != 0)
				acceleratorText += KeyEvent.getKeyText(keyCode);
			else
				acceleratorText += accelerator.getKeyChar();
		}
		return acceleratorText;
	}

}
