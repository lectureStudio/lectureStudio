package org.lecturestudio.editor.api.view;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.editor.api.model.ZoomConstraints;
import org.lecturestudio.media.search.SearchState;

public class MediaTrackControlsMockView implements MediaTrackControlsView {
	public BooleanProperty canCut;
	public BooleanProperty canDeletePage;
	public BooleanProperty canUndo;
	public BooleanProperty canRedo;
	public DoubleProperty zoomLevel;
	public BooleanProperty canSplitAndSaveRecording;
	public Action onCollapseSelectionAction;
	public Action onUndoAction;
	public Action onRedoAction;
	public Action onCutAction;
	public Action onAdjustVolumeAction;
	public Action onDeletePageAction;
	public Action onReplacePageAction;
	public Action onImportRecordingAction;
	public Action onZoomInAction;
	public Action onZoomOutAction;
	public ConsumerAction<String> onSearchAction;
	public Action onPreviousFoundPageAction;
	public Action onNextFoundPageAction;
	public Action onSplitAndSaveRecordingAction;
	public SearchState searchState;
	public ZoomConstraints zoomConstraints;

	@Override
	public void bindCanCut(BooleanProperty property) {
		this.canCut = property;
	}

	@Override
	public void bindCanDeletePage(BooleanProperty property) {
		this.canDeletePage = property;
	}

	@Override
	public void bindCanUndo(BooleanProperty property) {
		this.canUndo = property;
	}

	@Override
	public void bindCanRedo(BooleanProperty property) {
		this.canRedo = property;
	}

	@Override
	public void bindZoomLevel(ZoomConstraints constraints, DoubleProperty property) {
		this.zoomConstraints = constraints;
		this.zoomLevel = property;
	}

	@Override
	public void bindCanSplitAndSaveRecording(BooleanProperty property) {
		this.canSplitAndSaveRecording = property;
	}

	@Override
	public void setOnCollapseSelection(Action action) {
		this.onCollapseSelectionAction = action;
	}

	@Override
	public void setOnUndo(Action action) {
		this.onUndoAction = action;
	}

	@Override
	public void setOnRedo(Action action) {
		this.onRedoAction = action;
	}

	@Override
	public void setOnCut(Action action) {
		this.onCutAction = action;
	}

	@Override
	public void setOnAdjustVolume(Action action) {
		this.onAdjustVolumeAction = action;
	}

	@Override
	public void setOnDeletePage(Action action) {
		this.onDeletePageAction = action;
	}

	@Override
	public void setOnReplacePage(Action action) {
		this.onReplacePageAction = action;
	}

	@Override
	public void setOnImportRecording(Action action) {
		this.onImportRecordingAction = action;
	}

	@Override
	public void setOnZoomIn(Action action) {
		this.onZoomInAction = action;
	}

	@Override
	public void setOnZoomOut(Action action) {
		this.onZoomOutAction = action;
	}

	@Override
	public void setOnSearch(ConsumerAction<String> action) {
		this.onSearchAction = action;
	}

	@Override
	public void setOnPreviousFoundPage(Action action) {
		this.onPreviousFoundPageAction = action;
	}

	@Override
	public void setOnNextFoundPage(Action action) {
		this.onNextFoundPageAction = action;
	}

	@Override
	public void setOnSplitAndSaveRecording(Action action) {
		this.onSplitAndSaveRecordingAction = action;
	}

	@Override
	public void setSearchState(SearchState searchState) {
		this.searchState = searchState;
	}
}