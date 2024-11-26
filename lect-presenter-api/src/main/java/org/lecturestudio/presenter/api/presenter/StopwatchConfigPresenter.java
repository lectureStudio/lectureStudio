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

import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.model.Stopwatch;
import org.lecturestudio.presenter.api.view.StopwatchConfigView;

import javax.inject.Inject;

import static java.util.Objects.nonNull;

/**
 * Handles the properties set in a StopwatchConfigView and updates the Presenter Context stopwatch.
 *
 * @author Dustin Ringel
 */
public class StopwatchConfigPresenter extends Presenter<StopwatchConfigView> {

    private Action startAction;

    private Stopwatch stopwatch;

    private Stopwatch.StopwatchType stopwatchType = Stopwatch.StopwatchType.STOPWATCH;

    private StringProperty stopwatchTime = new StringProperty("0");

    @Inject
    StopwatchConfigPresenter(PresenterContext context, StopwatchConfigView view) {
        super(context, view);
        this.stopwatch = context.getStopwatch();
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

    private void onSave() {
        dispose();

        if (nonNull(startAction)) {
            stopwatch.setType(stopwatchType);
            stopwatch.setStopwatchIntervalByString(stopwatchTime.get());
            startAction.execute();
        }
    }

    private void setStopwatchType(Stopwatch.StopwatchType type){
        stopwatchType = type;
    }
    private void dispose() {
        super.close();
    }

    public void setOnStart(Action action) {
        startAction = action;
    }

}
