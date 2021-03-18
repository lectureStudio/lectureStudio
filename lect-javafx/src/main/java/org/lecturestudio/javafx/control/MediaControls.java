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

package org.lecturestudio.javafx.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.Pair;

import org.lecturestudio.core.model.Time;
import org.lecturestudio.javafx.util.FxUtils;

public class MediaControls extends Control {

	private final static String DEFAULT_STYLE_CLASS = "media-controls";

	private final ObjectProperty<Time> duration = new SimpleObjectProperty<>();

	private final ObjectProperty<Pair<Integer, Integer>> pages = new SimpleObjectProperty<>();

	private final DoubleProperty time = new SimpleDoubleProperty();

	private final DoubleProperty volume = new SimpleDoubleProperty();

	private final BooleanProperty mute = new SimpleBooleanProperty();

	private final BooleanProperty playing = new SimpleBooleanProperty();

	private final ObjectProperty<EventHandler<ActionEvent>> seekAction = new SimpleObjectProperty<>();

	private final ObjectProperty<EventHandler<ActionEvent>> prevAction = new SimpleObjectProperty<>();

	private final ObjectProperty<EventHandler<ActionEvent>> nextAction = new SimpleObjectProperty<>();


	public MediaControls() {
		initialize();
	}

	public final void setTime(double time) {
		this.time.set(time);
	}

	public final DoubleProperty timeProperty() {
		return time;
	}

	public final void setVolume(double volume) {
		this.volume.set(volume);
	}

	public final DoubleProperty volumeProperty() {
		return volume;
	}

	public final void setOnSeekPressed(EventHandler<ActionEvent> handler) {
		seekAction.set(handler);
	}

	public final ObjectProperty<EventHandler<ActionEvent>> onSeekActionProperty() {
		return seekAction;
	}

	public final void setOnPrevAction(EventHandler<ActionEvent> handler) {
		prevAction.set(handler);
	}

	public final ObjectProperty<EventHandler<ActionEvent>> onPrevActionProperty() {
		return prevAction;
	}

	public final void setOnNextAction(EventHandler<ActionEvent> handler) {
		nextAction.set(handler);
	}

	public final ObjectProperty<EventHandler<ActionEvent>> onNextActionProperty() {
		return nextAction;
	}

	public final void setMute(boolean mute) {
		FxUtils.invoke(() -> this.mute.set(mute));
	}

	public final BooleanProperty muteProperty() {
		return mute;
	}

	public final void setPlaying(boolean playing) {
		FxUtils.invoke(() -> this.playing.set(playing));
	}

	public final BooleanProperty playingProperty() {
		return playing;
	}

	public final void setDuration(Time duration) {
		FxUtils.invoke(() -> this.duration.set(duration));
	}

	public final ObjectProperty<Time> durationProperty() {
		return duration;
	}

	public final void setCurrentPage(Pair<Integer, Integer> pages) {
		FxUtils.invoke(() -> this.pages.set(pages));
	}

	public final ObjectProperty<Pair<Integer, Integer>> pagesProperty() {
		return pages;
	}

	@Override
	public String getUserAgentStylesheet() {
		return getClass().getResource("/resources/css/media-controls.css").toExternalForm();
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new MediaControlsSkin(this);
	}

	private void initialize() {
		getStyleClass().setAll(DEFAULT_STYLE_CLASS);
	}

}
