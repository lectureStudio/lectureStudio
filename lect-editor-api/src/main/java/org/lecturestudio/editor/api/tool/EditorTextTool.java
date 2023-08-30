package org.lecturestudio.editor.api.tool;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.shape.TextShape;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.text.TextAttributes;
import org.lecturestudio.core.tool.ShapePaintEvent;
import org.lecturestudio.core.tool.TextTool;
import org.lecturestudio.core.tool.ToolEventType;
import org.lecturestudio.editor.api.controller.EditorToolController;
import org.lecturestudio.editor.api.recording.action.EditorTextChangeAction;
import org.lecturestudio.editor.api.recording.action.EditorTextFontChangeAction;
import org.lecturestudio.editor.api.recording.action.EditorTextLocationChangeAction;

public class EditorTextTool extends TextTool {
	private final EditorToolController toolController;
	private StringProperty textProperty;
	private ObjectProperty<Color> textColorProperty;
	private ObjectProperty<Font> fontProperty;
	private ObjectProperty<TextAttributes> textAttributesProperty;

	public EditorTextTool(EditorToolController toolController) {
		this(toolController, -1);
	}

	public EditorTextTool(EditorToolController toolController, int handle) {
		super(toolController, handle);
		this.toolController = toolController;
	}

	@Override
	public void begin(PenPoint2D point, Page page) {
		textColorProperty = new ObjectProperty<>();
		fontProperty = new ObjectProperty<>();
		textAttributesProperty = new ObjectProperty<>();

		super.begin(point, page);
	}

	@Override
	public void end(PenPoint2D point) {
		super.end(point);

		this.textProperty = shape.textProperty();
		this.textColorProperty.set(shape.getTextColor());
		this.fontProperty.set(shape.getFont());
		this.textAttributesProperty.set(shape.getTextAttributes());

		// Text font changed
		recordAction(new EditorTextFontChangeAction(shape.getHandle(),
				textColorProperty, fontProperty, textAttributesProperty));

		// Text changed
		recordAction(new EditorTextChangeAction(shape.getHandle(), textProperty));

		// Location changed
		recordAction(new EditorTextLocationChangeAction(shape.getHandle(),
				shape.getBounds()));

		fireToolEvent(new ShapePaintEvent(ToolEventType.EXECUTE, shape,
				shape.getDirtyBounds().clone()));

		toolController.fireShapeAdded(shape);
		shape.addTextChangeListener(this);
	}

	@Override
	public void textChanged(TextShape shape) {
		// No subsequent events
		fireToolEvent(new ShapePaintEvent(ToolEventType.EXECUTE, shape,
				shape.getDirtyBounds().clone()));
	}

	@Override
	public void textFontChanged(TextShape shape) {
		this.textColorProperty.set(shape.getTextColor());
		this.fontProperty.set(shape.getFont());
		this.textAttributesProperty.set(shape.getTextAttributes());

		fireToolEvent(new ShapePaintEvent(ToolEventType.EXECUTE, shape,
				shape.getDirtyBounds().clone()));
	}

	@Override
	public void textLocationChanged(TextShape shape) {
		fireToolEvent(new ShapePaintEvent(ToolEventType.EXECUTE, shape,
				shape.getDirtyBounds().clone()));
	}

	@Override
	public void textRemoved(TextShape shape) {
		fireToolEvent(new ShapePaintEvent(ToolEventType.EXECUTE, shape,
				shape.getDirtyBounds().clone()));
	}
}
