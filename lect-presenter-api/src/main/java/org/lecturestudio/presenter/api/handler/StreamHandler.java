/*
 * Copyright (C) 2023 TU Darmstadt, Department of Computer Science,
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

import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.service.ScreenShareService;
import org.lecturestudio.presenter.api.service.StreamService;

public class StreamHandler extends PresenterHandler {

	private final StreamService streamService;

	private final ScreenShareService screenShareService;


	/**
	 * Create a new {@code StreamHandler} with the given context.
	 *
	 * @param context The presenter application context.
	 */
	public StreamHandler(PresenterContext context, StreamService streamService,
			ScreenShareService screenShareService) {
		super(context);

		this.streamService = streamService;
		this.screenShareService = screenShareService;
	}

	@Override
	public void initialize() {
		context.streamStartedProperty().addListener((o, oldValue, newValue) -> {
			streamService.enableStream(newValue);

			if (newValue) {
				if (screenShareService.isScreenCaptureActive()) {
					// Local screen share active. Stop local and start screen
					// share with the stream.
					screenShareService.stopScreenCapture();

					streamService.enableScreenSharing(true);
				}
			}
			else {
				// Stop screen-share as well.
				streamService.enableScreenSharing(false);
			}
		});
	}
}
