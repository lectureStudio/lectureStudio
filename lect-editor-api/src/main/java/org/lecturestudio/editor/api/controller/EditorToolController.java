package org.lecturestudio.editor.api.controller;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.TextShape;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.tool.TextTool;
import org.lecturestudio.core.tool.Tool;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.editor.api.bus.event.EditorRecordActionEvent;
import org.lecturestudio.editor.api.bus.event.EditorToolSelectionEvent;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.recording.AnnotationLectureRecorder;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.tool.EditorSelectTool;
import org.lecturestudio.editor.api.tool.EditorTextTool;

@Singleton
public class EditorToolController extends ToolController {
	private final EditorContext context;
	private final DocumentService documentService;
	private final RecordingFileService recordingFileService;
	private final AnnotationLectureRecorder annotationLectureRecorder;
	private final BooleanProperty isToolRecordingEnabled = new BooleanProperty(true);
	private final BooleanProperty isEditingProperty = new BooleanProperty(false);
	private final List<Consumer<Shape>> shapeAddedListeners = new ArrayList<>();

	@Inject
	public EditorToolController(ApplicationContext context,
	                            DocumentService documentService,
								RecordingFileService recordingFileService,
	                            AnnotationLectureRecorder annotationLectureRecorder) {
		super(context, documentService);
		this.context = (EditorContext) context;
		this.documentService = documentService;
		this.recordingFileService = recordingFileService;
		this.annotationLectureRecorder = annotationLectureRecorder;
	}

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

	@Override
	public void recordAction(PlaybackAction action) {
		if (getIsToolRecordingEnabled() && getIsEditing()) {
			getContext().getEventBus().post(new EditorRecordActionEvent(action));
		}
	}

	private boolean getIsToolRecordingEnabled() {
		return isToolRecordingEnabled.get();
	}

	/**
	 * Set the new painting tool.
	 */
	@Override
	public void setTool(Tool tool) {
		super.setTool(tool);

		if (nonNull(tool)) {
			pushEvent(new EditorToolSelectionEvent(tool.getType(), getPaintSettings(tool.getType())));

			switch (tool.getType()) {
				case SELECT -> setIsToolRecordingEnabled(false);
				default -> setIsToolRecordingEnabled(true);
			}
		}
		else {
			pushEvent(new EditorToolSelectionEvent(null, getPaintSettings(null)));
		}
	}

	private void setIsToolRecordingEnabled(boolean enabled) {
		isToolRecordingEnabled.set(enabled);
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


	/**
	 * Copy a TextShape.
	 *
	 * @param shape The TextShape to copy.
	 */
	@Override
	public void copyText(TextShape shape) {
		Document doc = documentService.getDocuments().getSelectedDocument();

		if (isNull(doc)) {
			return;
		}

		Page page = doc.getCurrentPage();

		PenPoint2D loc = new PenPoint2D(shape.getLocation().getX(), shape.getLocation().getY());

		TextTool tool = new EditorTextTool(this, shape.getHandle());
		tool.begin(loc, page);
		tool.execute(loc);
		tool.end(loc);
		tool.copy(shape);
	}

	public CompletableFuture<Void> persistPlaybackActions() {
		try {
			return annotationLectureRecorder.persistPlaybackActions();
		}
		catch (IllegalStateException e) {
			getContext().showError("annotationrecorder.error.title", e.getMessage());
		}
		return CompletableFuture.completedFuture(null);
	}

	public void resetRecordedPlaybackActions() {
		annotationLectureRecorder.reset();
	}

	public BooleanProperty isEditingProperty() {
		return isEditingProperty;
	}

	public boolean getIsEditing() {
		return isEditingProperty.get();
	}

	public void setIsEditing(boolean enabled) {
		isEditingProperty.set(enabled);
	}

	public void addPageShapeAddedListener(Consumer<Shape> shapeListener) {
		shapeAddedListeners.add(shapeListener);
	}

	public void fireShapeAdded(Shape shape) {
		for (Consumer<Shape> listener : shapeAddedListeners) {
			listener.accept(shape);
		}
	}

	@Override
	public void selectSelectTool() {
		setTool(new EditorSelectTool(this));
	}

	public RecordingFileService getRecordingFileService() {
		return recordingFileService;
	}
}
