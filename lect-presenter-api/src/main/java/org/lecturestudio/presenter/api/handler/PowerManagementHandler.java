/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.handler;

import dev.onvoid.webrtc.media.video.desktop.PowerManagement;

import org.lecturestudio.core.app.configuration.DisplayConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;

/**
 * Handle power management according to the systems energy plan.
 *
 * @author Alex Andres
 */
public class PowerManagementHandler extends PresenterHandler {

	private final PowerManagement powerManager = new PowerManagement();


	/**
	 * Create a new {@code PowerManagementHandler} with the given context.
	 *
	 * @param context The presenter application context.
	 */
	public PowerManagementHandler(PresenterContext context) {
		super(context);
	}

	@Override
	public void initialize() {
		DisplayConfiguration config = context.getConfiguration().getDisplayConfig();
		config.screenPowerPlanProperty().addListener((observable, oldValue, newValue) -> {
			setScreenPowerPlan(newValue);
		});

		setScreenPowerPlan(config.getScreenPowerPlan());
	}

	/**
	 * If the power plan is enabled, disable user activity, to turn off the screen when the user is inactive.
	 *
	 * @param enabled {@code true} to turn off the screen on user inactivity.
	 */
	private void setScreenPowerPlan(boolean enabled) {
		if (enabled) {
			powerManager.disableUserActivity();
		}
		else {
			powerManager.enableUserActivity();
		}
	}
}
