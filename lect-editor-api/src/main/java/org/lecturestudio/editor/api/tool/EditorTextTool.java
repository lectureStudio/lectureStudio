package org.lecturestudio.editor.api.tool;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.shape.TextShape;
import org.lecturestudio.core.recording.action.TextLocationChangeAction;
import org.lecturestudio.core.recording.action.TextRemoveAction;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.text.TextAttributes;
import org.lecturestudio.core.tool.ShapePaintEvent;
import org.lecturestudio.core.tool.TextTool;
import org.lecturestudio.core.tool.ToolContext;
import org.lecturestudio.core.tool.ToolEventType;
import org.lecturestudio.editor.api.recording.action.EditorTextChangeAction;
import org.lecturestudio.editor.api.recording.action.EditorTextFontChangeAction;
import org.lecturestudio.editor.api.recording.action.EditorTextLocationChangeAction;

public class EditorTextTool extends TextTool {

	private StringProperty textProperty;
	private ObjectProperty<Color> textColorProperty = new ObjectProperty<>();
	private ObjectProperty<Font> fontProperty = new ObjectProperty<>();
	private ObjectProperty<TextAttributes> textAttributesProperty = new ObjectProperty<>();

	public EditorTextTool(ToolContext context) {
		super(context);
	}

	public EditorTextTool(ToolContext context, int handle) {
		super(context, handle);
	}

	@Override
	public void end(PenPoint2D point) {
		super.end(point);

		this.textProperty = shape.textProperty();
		this.textColorProperty.set(shape.getTextColor().clone());
		this.fontProperty.set(shape.getFont().clone());
		this.textAttributesProperty.set(shape.getTextAttributes().clone());

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
	}

	@Override
	public void textChanged(TextShape shape) {
		// No subsequent events
		fireToolEvent(new ShapePaintEvent(ToolEventType.EXECUTE, shape,
				shape.getDirtyBounds().clone()));
	}

	@Override
	public void textFontChanged(TextShape shape) {
		this.textColorProperty.set(shape.getTextColor().clone());
		this.fontProperty.set(shape.getFont().clone());
		this.textAttributesProperty.set(shape.getTextAttributes().clone());

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
		recordAction(new TextRemoveAction(shape.getHandle()));
	}

}
