package org.lecturestudio.editor.api.view;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.view.Action;

public class MediaControlsMockView implements MediaControlsView {
	public DoubleProperty bindVolumeProperty;
	public BooleanProperty bindPlayingProperty;
	public BooleanProperty bindMuteProperty;
	public DoubleProperty bindSeekProperty;
	public Time duration;
	public Integer currentPage;
	public Integer totalPages;
	public Action onSeekPressed;
	public Action onPreviousPage;
	public Action onNextPage;

	@Override
	public void bindPlaying(BooleanProperty property) {
		this.bindPlayingProperty = property;
	}

	@Override
	public void bindMute(BooleanProperty property) {
		this.bindMuteProperty = property;
	}

	@Override
	public void bindSeek(DoubleProperty property) {
		this.bindSeekProperty = property;
	}

	@Override
	public void bindVolume(DoubleProperty property) {
		this.bindVolumeProperty = property;
	}

	@Override
	public void setDuration(Time duration) {
		this.duration = duration;
	}

	@Override
	public void setCurrentPage(Integer current, Integer total) {
		this.currentPage = current;
		this.totalPages = total;
	}

	@Override
	public void setOnSeekPressed(Action action) {
		this.onSeekPressed = action;
	}

	@Override
	public void setOnPreviousPage(Action action) {
		this.onPreviousPage = action;
	}

	@Override
	public void setOnNextPage(Action action) {
		this.onNextPage = action;
	}
}