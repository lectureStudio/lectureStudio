package org.lecturestudio.editor.api.controller;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Objects;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.TextBoxShape;
import org.lecturestudio.core.model.shape.TextShape;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.tool.ShapePaintEvent;
import org.lecturestudio.core.tool.TextTool;
import org.lecturestudio.core.tool.Tool;
import org.lecturestudio.core.tool.ToolEventType;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.editor.api.bus.event.EditorToolSelectionEvent;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.recording.AnnotationLectureRecorder;
import org.lecturestudio.editor.api.tool.EditorTextTool;

@Singleton
public class EditorToolController extends org.lecturestudio.core.controller.ToolController {
	private final ApplicationContext context;
	private final DocumentService documentService;
	private final AnnotationLectureRecorder annotationLectureRecorder;

	@Inject
	public EditorToolController(ApplicationContext context,
	                            DocumentService documentService,
	                            AnnotationLectureRecorder annotationLectureRecorder) {
		super(context, documentService);
		this.context = context;
		this.documentService = documentService;
		this.annotationLectureRecorder = annotationLectureRecorder;

		// We have to start and end recording separately in case an Action does not consist of a TOOL_BEGIN and TOOL_END
		// SimpleToolActions are one example of these
		((EditorContext) getContext()).isEditingProperty().addListener((observable, oldValue, newValue) ->
				{

				}
		);
	}

	@Override
	public void beginToolAction(PenPoint2D point) {
		try {
			if (Boolean.TRUE.equals(((EditorContext) getContext()).getIsEditing()) && !annotationLectureRecorder.started()) {
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
			if (Boolean.TRUE.equals(((EditorContext) getContext()).getIsEditing()) && !annotationLectureRecorder.suspended()) {
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
		boolean previousValue = ((EditorContext) getContext()).getIsEditing();

		((EditorContext) getContext()).setIsEditing(true);
		super.simpleToolAction();
		((EditorContext) getContext()).setIsEditing(previousValue);
	}

	@Override
	public void recordAction(PlaybackAction action) {
		if (((EditorContext) getContext()).getIsEditing()) {
			super.recordAction(action);
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


	/**
	 * Set text of a text shape with the specified handle.
	 *
	 * @param handle The handle of a text shape.
	 * @param text   The new text to set.
	 *
	 * @throws NullPointerException If the text shape could not be found.
	 */
	public void setText(int handle, StringProperty text) throws NullPointerException {
		TextBoxShape<?> textShape = getTextShape(handle);
		Objects.requireNonNull(textShape).setText(text.get());

		fireToolEvent(new ShapePaintEvent(ToolEventType.BEGIN,
				(Shape) textShape, null));
	}

	public void persistPlaybackActions() {
		try {
			annotationLectureRecorder.persistPlaybackActions();
		}
		catch (IllegalStateException e) {
			getContext().showError("annotationrecorder.error.title", e.getMessage());
		}
	}

	public void setTextLocation(int handle, Rectangle2D location) {
		TextBoxShape<?> textShape = getTextShape(handle);
		Objects.requireNonNull(textShape).setLocation(location.getLocation());

		fireToolEvent(new ShapePaintEvent(ToolEventType.BEGIN,
				(Shape) textShape, null));

	}
}
