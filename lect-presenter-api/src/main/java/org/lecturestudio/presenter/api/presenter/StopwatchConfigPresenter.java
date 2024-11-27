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

import javax.inject.Inject;

import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.model.Stopwatch;
import org.lecturestudio.presenter.api.view.StopwatchConfigView;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles the properties set in a StopwatchConfigView and updates the Presenter Context stopwatch.
 *
 * @author Dustin Ringel
 */
public class StopwatchConfigPresenter extends Presenter<StopwatchConfigView> {

	private final Stopwatch stopwatch;

	private final StringProperty stopwatchTime = new StringProperty("");

	private Action saveAction;

	private Stopwatch.StopwatchType stopwatchType = Stopwatch.StopwatchType.STOPWATCH;


	@Inject
	StopwatchConfigPresenter(PresenterContext context, StopwatchConfigView view) {
		super(context, view);

		stopwatch = context.getStopwatch();
	}

	@Override
	public void initialize() {
		setStopwatchType(Stopwatch.StopwatchType.STOPWATCH);

		view.setOnStopwatchType(this::setStopwatchType);
		view.setStopwatchTime(stopwatchTime);
		view.setOnStart(this::onSave);
		view.setOnClose(this::close);
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	@Override
	public void close() {
		dispose();
	}

	public void setOnSave(Action action) {
		saveAction = action;
	}

	private void onSave() {
		dispose();

		LocalTime time = null;
		String timeString = stopwatchTime.get();

		try {
			time = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"));
		}
		catch (Exception e) {
			// TODO: Show error
		}

		if (nonNull(saveAction) && nonNull(time)) {
			stopwatch.setType(stopwatchType);
			stopwatch.setStartTime(time);
			saveAction.execute();
		}
	}

	private void setStopwatchType(Stopwatch.StopwatchType type) {
		stopwatchType = type;
	}

	private void dispose() {
		super.close();
	}
}
