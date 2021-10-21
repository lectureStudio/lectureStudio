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

package org.lecturestudio.presenter.api.presenter;

import static java.util.Objects.nonNull;

import com.google.common.eventbus.Subscribe;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.audio.bus.event.AudioSignalEvent;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.presenter.api.view.AdjustAudioCaptureLevelView;

public class AdjustAudioCaptureLevelPresenter extends Presenter<AdjustAudioCaptureLevelView> {

	/** This action is executed when the capturing process has been started. */
	private Action beginAction;

	/** This action is executed when the capturing process has been canceled. */
	private Action cancelAction;

	private double audioLevel;

	private boolean capturing;


	@Inject
	AdjustAudioCaptureLevelPresenter(ApplicationContext context, AdjustAudioCaptureLevelView view) {
		super(context, view);
	}

	@Subscribe
	public void onEvent(final AudioSignalEvent event) {
		setAudioCaptureLevel(event.getSignalValue());
	}

	@Override
	public void close() {
		unbindFromAudioBus();

		super.close();
	}

	@Override
	public void initialize() {
		Configuration config = context.getConfiguration();

		view.setCaptureDeviceName(config.getAudioConfig().getCaptureDeviceName());
		view.setOnBegin(this::beginCapture);
		view.setOnCancel(this::cancelCapture);
		view.setOnFinish(this::saveCaptureLevel);
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	public void setOnBegin(Action action) {
		beginAction = Action.concatenate(beginAction, action);
	}

	public void setOnCancel(Action action) {
		cancelAction = Action.concatenate(cancelAction, action);
	}

	private void cancelCapture() {
		if (nonNull(cancelAction)) {
			cancelAction.execute();
		}

		close();
	}

	private void beginCapture() {
		if (nonNull(beginAction)) {
			beginAction.execute();
		}

		audioLevel = 0;

		bindToAudioBus();

		view.setAudioLevelCaptureStarted(true);
	}

	private void saveCaptureLevel() {
		unbindFromAudioBus();

		AudioConfiguration config = context.getConfiguration().getAudioConfig();
		config.setRecordingVolume(config.getCaptureDeviceName(), 1 - audioLevel * 0.5);

		close();
	}

	private void setAudioCaptureLevel(double value) {
		audioLevel = Math.max(this.audioLevel, value);

		view.setAudioLevel(value);
	}

	private void bindToAudioBus() {
		if (!capturing) {
			context.getAudioBus().register(this);
		}

		capturing = true;
	}

	private void unbindFromAudioBus() {
		if (capturing) {
			context.getAudioBus().unregister(this);
		}

		capturing = false;
	}

}
