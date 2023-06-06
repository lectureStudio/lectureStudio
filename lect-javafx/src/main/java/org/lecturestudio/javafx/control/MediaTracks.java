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

import static java.util.Objects.nonNull;

import java.util.Objects;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.transform.Affine;

import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.media.track.MediaTrack;

public class MediaTracks extends Control {

	private static final String DEFAULT_STYLE_CLASS = "media-tracks";

	private final DoubleProperty primarySelection = new SimpleDoubleProperty();

	private final DoubleProperty leftSelection = new SimpleDoubleProperty();

	private final DoubleProperty rightSelection = new SimpleDoubleProperty();

	private final ObjectProperty<Affine> transform = new SimpleObjectProperty<>();

	private final ObjectProperty<Time> duration = new SimpleObjectProperty<>();

	private final ObservableList<MediaTrack<?>> tracks = FXCollections.observableArrayList();

	private final ObjectProperty<EventHandler<ActionEvent>> seekAction = new SimpleObjectProperty<>();

	private final ObjectProperty<EventHandler<ActionEvent>> stickSlidersAction = new SimpleObjectProperty<>();
	private ConsumerAction<RecordedPage> onMovePageAction;
	private ConsumerAction<RecordedPage> onHidePageAction;
	private ConsumerAction<RecordedPage> onHideAndMoveNextPageAction;


	public MediaTracks() {
		initialize();

		setTransform(new Affine());
	}

	public final double getPrimarySelection() {
		return primarySelection.get();
	}

	public final void setPrimarySelection(double time) {
		primarySelection.set(time);
	}

	public final DoubleProperty primarySelectionProperty() {
		return primarySelection;
	}

	public final double getLeftSelection() {
		return leftSelection.get();
	}

	public final void setLeftSelection(double time) {
		leftSelection.set(time);
	}

	public final DoubleProperty leftSelectionProperty() {
		return leftSelection;
	}

	public final double getRightSelection() {
		return rightSelection.get();
	}

	public final void setRightSelection(double time) {
		rightSelection.set(time);
	}

	public final DoubleProperty rightSelectionProperty() {
		return rightSelection;
	}

	public final Affine getTransform() {
		return transform.get();
	}

	public final void setTransform(Affine affineTransform) {
		transform.set(affineTransform);
	}

	public final ObjectProperty<Affine> transformProperty() {
		return transform;
	}

	public final Time getDuration() {
		return duration.get();
	}

	public final void setDuration(Time duration) {
		this.duration.set(duration);
	}

	public final ObjectProperty<Time> durationProperty() {
		return duration;
	}

	public final ObservableList<MediaTrack<?>> getTracks() {
		return tracks;
	}

	public final void addTrack(MediaTrack<?> track) {
		tracks.add(track);
	}

	public final void removeTrack(MediaTrack<?> track) {
		tracks.remove(track);
	}

	public final void setOnSeekPressed(EventHandler<ActionEvent> handler) {
		seekAction.set(handler);
	}

	public final ObjectProperty<EventHandler<ActionEvent>> onSeekActionProperty() {
		return seekAction;
	}

	public final void stickSlidersTogether() {
		if (nonNull(stickSlidersAction.get())) {
			stickSlidersAction.get().handle(new ActionEvent(this, Event.NULL_SOURCE_TARGET));
		}
	}

	final void setOnStickSliders(EventHandler<ActionEvent> handler) {
		stickSlidersAction.set(handler);
	}

	@Override
	public String getUserAgentStylesheet() {
		return Objects.requireNonNull(getClass().getResource("/resources/css/media-tracks.css")).toExternalForm();
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new MediaTracksSkin(this);
	}

	private void initialize() {
		getStyleClass().setAll(DEFAULT_STYLE_CLASS);
	}

	protected void setOnMovePage(ConsumerAction<RecordedPage> action) {
		this.onMovePageAction = action;
	}

	public ConsumerAction<RecordedPage> getOnMovePage() {
		return this.onMovePageAction;
	}

	protected void setOnHidePage(ConsumerAction<RecordedPage> action) {
		this.onHidePageAction = action;
	}

	public ConsumerAction<RecordedPage> getOnHidePage() {
		return this.onHidePageAction;
	}

	protected void setOnHideAndMoveNextPage(ConsumerAction<RecordedPage> action) {
		this.onHideAndMoveNextPageAction = action;
	}

	public ConsumerAction<RecordedPage> getOnHideAndMoveNextPage() {
		return this.onHideAndMoveNextPageAction;
	}
}
