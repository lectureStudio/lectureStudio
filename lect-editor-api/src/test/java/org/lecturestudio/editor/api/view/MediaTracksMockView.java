package org.lecturestudio.editor.api.view;

import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.media.track.MediaTrack;

public class MediaTracksMockView implements MediaTracksView {
	public DoubleProperty bindZoomLevelProperty;
	public DoubleProperty bindPrimarySelectionProperty;
	public DoubleProperty bindLeftSelectionProperty;
	public DoubleProperty bindRightSelectionProperty;
	public boolean stickSlidersBoolean;
	public Time setDurationTime;
	public MediaTrack<?>[] setMediaTracksArray;
	public Action setOnSeekPressedAction;
	public ConsumerAction<RecordedPage> setOnMovePageAction;
	public ConsumerAction<RecordedPage> setOnHidePageAction;
	public ConsumerAction<RecordedPage> setOnHideAndMoveNextPageAction;

	@Override
	public void bindPrimarySelection(DoubleProperty property) {
		this.bindPrimarySelectionProperty = property;
	}

	@Override
	public void bindLeftSelection(DoubleProperty property) {
		this.bindLeftSelectionProperty = property;
	}

	@Override
	public void bindRightSelection(DoubleProperty property) {
		this.bindRightSelectionProperty = property;
	}

	@Override
	public void bindZoomLevel(DoubleProperty property) {
		this.bindZoomLevelProperty = property;
	}

	@Override
	public void stickSliders() {
		this.stickSlidersBoolean = true;
	}

	@Override
	public void setDuration(Time duration) {
		this.setDurationTime = duration;
	}

	@Override
	public void setMediaTracks(MediaTrack<?>... tracks) {
		this.setMediaTracksArray = tracks;
	}

	@Override
	public void setOnSeekPressed(Action action) {
		this.setOnSeekPressedAction = action;
	}

	@Override
	public void setOnMovePage(ConsumerAction<RecordedPage> action) {
		this.setOnMovePageAction = action;
	}

	@Override
	public void setOnHidePage(ConsumerAction<RecordedPage> action) {
		this.setOnHidePageAction = action;
	}

	@Override
	public void setOnHideAndMoveNextPage(ConsumerAction<RecordedPage> action) {
		this.setOnHideAndMoveNextPageAction = action;
	}
}