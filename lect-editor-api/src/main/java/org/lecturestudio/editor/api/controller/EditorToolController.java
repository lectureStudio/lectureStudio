package org.lecturestudio.editor.api.controller;

import static java.util.Objects.nonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.recording.action.ActionType;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.tool.Tool;
import org.lecturestudio.editor.api.bus.event.EditorToolSelectionEvent;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.recording.AnnotationLectureRecorder;

@Singleton
public class EditorToolController extends org.lecturestudio.core.controller.ToolController {
	private final AnnotationLectureRecorder annotationLectureRecorder;

	@Inject
	public EditorToolController(ApplicationContext context, DocumentService documentService, AnnotationLectureRecorder annotationLectureRecorder) {
		super(context, documentService);
		this.annotationLectureRecorder = annotationLectureRecorder;

		// We have to start and end recording separately in case an Action does not consist of a TOOL_BEGIN and TOOL_END
		// SimpleToolActions are one example of these
		((EditorContext) getContext()).isEditingProperty().addListener((observable, oldValue, newValue) ->
				{
					try {
						if (Boolean.TRUE.equals(newValue) && !annotationLectureRecorder.started()) {
							annotationLectureRecorder.start();
						}
						else if (Boolean.FALSE.equals(newValue) && !annotationLectureRecorder.suspended()) {
							annotationLectureRecorder.suspend();
							annotationLectureRecorder.persistPlaybackActions();
						}
					}
					catch (ExecutableException e) {
						getContext().showError("annotationrecorder.failed.title", "annotationrecorder.failed.message");
					}
					catch (IllegalStateException e) {
						getContext().showError("annotationrecorder.error.title", e.getMessage());
					}
				}
		);
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
}
