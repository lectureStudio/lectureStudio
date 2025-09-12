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

package org.lecturestudio.editor.api.controller;

import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.tool.Tool;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.editor.api.bus.event.EditorRecordActionEvent;
import org.lecturestudio.editor.api.bus.event.EditorToolSelectionEvent;
import org.lecturestudio.editor.api.recording.AnnotationLectureRecorder;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.tool.EditorSelectTool;
import org.lecturestudio.editor.api.tool.EditorTextTool;

/**
 * Controller for managing editor tools and their interactions with the document.
 * This class extends the core ToolController to provide editor-specific functionality,
 * including annotation recording, tool state management, and shape event handling.
 * <p>
 * It coordinates tool selection, painting actions, and maintains the editing state
 * while also providing recording capabilities for user annotations during the editing process.
 * The controller integrates with the recording file service to persist annotation actions.
 *
 * @author Hendrik Rüthers
 */
@Singleton
public class EditorToolController extends ToolController {

	/** Service responsible for managing recording files and their operations. */
	private final RecordingFileService recordingFileService;

	/** Recorder for lecture annotations that handles the capture and persistence of annotation actions during editing. */
	private final AnnotationLectureRecorder annotationLectureRecorder;

	/** Controls whether tool actions should be recorded. The default value is true, except for certain tools like SELECT. */
	private final BooleanProperty isToolRecordingEnabled = new BooleanProperty(true);

	/** Indicates whether editing mode is currently enabled. Affects recording behavior and tool operations. */
	private final BooleanProperty isEditingProperty = new BooleanProperty(false);

	/** Collection of listeners that get notified when shapes are added to the page. */
	private final List<Consumer<Shape>> shapeAddedListeners = new ArrayList<>();


	/**
	 * Creates a new EditorToolController that manages tool interactions and recording capabilities.
	 *
	 * @param context                   The application context providing access to application-wide resources and services.
	 * @param documentService           Service for managing documents and their state.
	 * @param recordingFileService      Service for handling recording file operations.
	 * @param annotationLectureRecorder Recorder responsible for capturing and storing annotation actions.
	 */
	@Inject
	public EditorToolController(ApplicationContext context,
	                            DocumentService documentService,
								RecordingFileService recordingFileService,
	                            AnnotationLectureRecorder annotationLectureRecorder) {
		super(context, documentService);

		this.recordingFileService = recordingFileService;
		this.annotationLectureRecorder = annotationLectureRecorder;
	}

	/**
	 * Starts the recording only if the tool allows for recording and the editing is enabled.
	 *
	 * @param point The location on the painting surface where to start.
	 */
	@Override
	public void beginToolAction(PenPoint2D point) {
		try {
			if (getIsToolRecordingEnabled() && getIsEditing() && !annotationLectureRecorder.started()) {
				annotationLectureRecorder.start();
			}
			super.beginToolAction(point);
		}
		catch (ExecutableException e) {
			getContext().showError("annotationrecorder.failed.title", "annotationrecorder.failed.message");
		}
	}

	/**
	 * Ends the Recording only if the tool allows for recording and the editing is enabled.
	 * Saves the recorded annotations, unless a text was recorded, since this one has a separate saving logic.
	 *
	 * @param point The location on the painting surface where to end.
	 */
	@Override
	public void endToolAction(PenPoint2D point) {
		try {
			super.endToolAction(point);

			if (getIsToolRecordingEnabled() && getIsEditing() && !annotationLectureRecorder.suspended()) {
				annotationLectureRecorder.suspend();

				if (selectedTool.getType() != ToolType.TEXT) {
					persistPlaybackActions();
				}
			}
		}
		catch (ExecutableException e) {
			getContext().showError("annotationrecorder.failed.title", "annotationrecorder.failed.message");
		}
		catch (IllegalStateException e) {
			getContext().showError("annotationrecorder.error.title", e.getMessage());
		}
	}

	@Override
	public void simpleToolAction() {
		boolean previousValue = getIsEditing();

		setIsEditing(true);
		super.simpleToolAction();
		setIsEditing(previousValue);
	}

	/**
	 * Records a playback action to the editor's event bus.
	 * This method only forwards the action to the event bus if both the tool recording
	 * is enabled and the editor is currently in editing mode.
	 *
	 * @param action The playback action to be recorded.
	 */
	@Override
	public void recordAction(PlaybackAction action) {
		if (getIsToolRecordingEnabled() && getIsEditing()) {
			getContext().getEventBus().post(new EditorRecordActionEvent(action));
		}
	}

	/**
	 * Set the new painting tool.
	 */
	@Override
	public void setTool(Tool tool) {
		super.setTool(tool);

		if (nonNull(tool)) {
			pushEvent(new EditorToolSelectionEvent(tool.getType(), getPaintSettings(tool.getType())));

			setIsToolRecordingEnabled(Objects.requireNonNull(tool.getType()) != ToolType.SELECT);
		}
		else {
			pushEvent(new EditorToolSelectionEvent(null, getPaintSettings(null)));
		}
	}

	/**
	 * Select the text tool.
	 */
	@Override
	public void selectTextTool() {
		setTool(new EditorTextTool(this));
	}

	/**
	 * Select the text tool and assigns the given handle to the created TextShape.
	 *
	 * @param handle The shape handle.
	 */
	@Override
	public void selectTextTool(int handle) {
		setTool(new EditorTextTool(this, handle));
	}

	@Override
	public void selectSelectTool() {
		setTool(new EditorSelectTool(this));
	}

	/**
	 * Persists the currently recorded playback actions to storage.
	 * This method delegates to the annotation lecture recorder to save
	 * the current set of recorded actions.
	 *
	 * @return A CompletableFuture representing the completion of the persist operation.
	 */
	public CompletableFuture<Void> persistPlaybackActions() {
		try {
			return annotationLectureRecorder.persistPlaybackActions();
		}
		catch (IllegalStateException e) {
			getContext().showError("annotationrecorder.error.title", e.getMessage());
		}
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Resets all recorded playback actions in the annotation recorder.
	 * This effectively clears any pending or unsaved annotations.
	 */
	public void resetRecordedPlaybackActions() {
		annotationLectureRecorder.reset();
	}

	/**
	 * Provides access to the boolean property that indicates whether editing is enabled.
	 *
	 * @return The BooleanProperty representing the editing state.
	 */
	public BooleanProperty isEditingProperty() {
		return isEditingProperty;
	}

	/**
	 * Gets the current value of the editing property.
	 *
	 * @return true if editing is enabled, false otherwise.
	 */
	public boolean getIsEditing() {
		return isEditingProperty.get();
	}

	/**
	 * Sets the editing state of the tool controller.
	 *
	 * @param enabled true to enable editing mode, false to disable it.
	 */
	public void setIsEditing(boolean enabled) {
		isEditingProperty.set(enabled);
	}

	/**
	 * Registers a listener to be notified when shapes are added to the page.
	 *
	 * @param shapeListener the listener to be called when a shape is added.
	 */
	public void addPageShapeAddedListener(Consumer<Shape> shapeListener) {
		shapeAddedListeners.add(shapeListener);
	}

	/**
	 * Notifies all registered listeners that a shape has been added to the page.
	 *
	 * @param shape the Shape that was added to the page.
	 */
	public void fireShapeAdded(Shape shape) {
		for (Consumer<Shape> listener : shapeAddedListeners) {
			listener.accept(shape);
		}
	}

	/**
	 * Gets the recording file service associated with this controller.
	 *
	 * @return the RecordingFileService instance.
	 */
	public RecordingFileService getRecordingFileService() {
		return recordingFileService;
	}

	private boolean getIsToolRecordingEnabled() {
		return isToolRecordingEnabled.get();
	}

	private void setIsToolRecordingEnabled(boolean enabled) {
		isToolRecordingEnabled.set(enabled);
	}
}
