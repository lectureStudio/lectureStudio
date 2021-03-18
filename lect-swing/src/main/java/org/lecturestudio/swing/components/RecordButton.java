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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.Timer;

import org.lecturestudio.core.ExecutableState;

public class RecordButton extends JButton {

	private Timer blinkTimer;

	private int blinkInterval;

	private boolean blinked;

	private Icon blinkIcon;

	private Icon pauseIcon;

	private Icon pausedIcon;

	private Icon icon;


	public RecordButton() {
		super();

		setBlinkInterval(500);
	}

	@Override
	public void setIcon(Icon defaultIcon) {
		super.setIcon(defaultIcon);

		icon = defaultIcon;
	}

	public void setPauseIcon(Icon icon) {
		pauseIcon = icon;
	}

	public void setPausedIcon(Icon icon) {
		pausedIcon = icon;
	}

	/**
	 * @param icon The icon that is shown with the frequency set by
	 *             {@link #setBlinkInterval(int)}.
	 */
	public void setBlinkIcon(Icon icon) {
		blinkIcon = icon;
	}

	/**
	 * @param interval The blink interval in milliseconds.
	 */
	public void setBlinkInterval(int interval) {
		blinkInterval = interval;
	}

	public void setBlink(boolean blink) {
		if (blink) {
			startBlinking();
		}
		else {
			stopBlinking();
		}
	}

	public void setState(ExecutableState state) {
		if (state == ExecutableState.Started) {
			super.setIcon(pauseIcon);
		}
		else if (state == ExecutableState.Suspended) {
			super.setIcon(pausedIcon);
		}
		else {
			super.setIcon(icon);
		}
	}

	private void startBlinking() {
		if (nonNull(blinkTimer)) {
			return;
		}

		icon = getIcon();

		blinkTimer = new Timer(blinkInterval, e -> {
			blinked = !blinked;

			if (blinked) {
				super.setIcon(blinkIcon);
			}
			else {
				super.setIcon(icon);
			}

			repaint();
		});
		blinkTimer.setRepeats(true);
		blinkTimer.start();
	}

	private void stopBlinking() {
		if (isNull(blinkTimer)) {
			return;
		}

		blinked = false;

		blinkTimer.stop();
		blinkTimer = null;
	}
}
