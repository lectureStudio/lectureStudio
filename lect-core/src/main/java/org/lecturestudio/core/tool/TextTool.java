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

package org.lecturestudio.core.tool;

import static java.util.Objects.nonNull;

import org.lecturestudio.core.geometry.Matrix;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.action.CreateShapeAction;
import org.lecturestudio.core.model.listener.TextChangeListener;
import org.lecturestudio.core.model.shape.TextShape;
import org.lecturestudio.core.recording.action.TextAction;
import org.lecturestudio.core.recording.action.TextChangeAction;
import org.lecturestudio.core.recording.action.TextFontChangeAction;
import org.lecturestudio.core.recording.action.TextLocationChangeAction;
import org.lecturestudio.core.recording.action.TextRemoveAction;

/**
 * PaintTool for inserting and selecting text boxes on a page.
 * This tool allows users to create, edit, position, and manage text elements on the slide.
 * It tracks text modifications and records appropriate actions for playback functionality.
 * <p>
 * When activated, it creates a new TextShape at the specified location and allows
 * the user to edit its content. The tool handles text changes, font modifications,
 * location changes, and text removal events through the TextChangeListener interface.
 *
 * @author Alex Andres
 * @author Tobias
 */
public class TextTool extends Tool implements TextChangeListener<TextShape> {

	/**
	 * The unique identifier for the text shape being created or manipulated.
	 * A value of -1 indicates no specific handle has been assigned.
	 */
	protected final int handle;

	/** The text shape instance that is currently being created or edited by this tool. */
	protected TextShape shape;

	/** The page on which the text shape is being created or edited. */
	protected Page page;


	/**
	 * Creates a new TextTool instance with a default handle value (-1).
	 *
	 * @param context The tool context containing settings and configuration for the tool.
	 */
	public TextTool(ToolContext context) {
		this(context, -1);
	}

	/**
	 * Creates a new TextTool instance with the specified handle value.
	 *
	 * @param context The tool context containing settings and configuration for the tool.
	 * @param handle  The handle identifier for the text shape, or -1 if not specified.
	 */
	public TextTool(ToolContext context, int handle) {
		super(context);

		this.handle = handle;
	}

	/**
	 * Copies all properties from the provided TextShape to this tool's shape.
	 * Properties include location, font, text content, and text color.
	 *
	 * @param shape The TextShape to copy properties from.
	 */
	public void copy(TextShape shape) {
		this.shape.setLocation(shape.getLocation());
		this.shape.setFont(shape.getFont());
		this.shape.setText(shape.getText());
		this.shape.setTextColor(shape.getTextColor());
	}

	@Override
	public void begin(PenPoint2D point, Page page) {
		this.page = page;

		TextSettings settings = context.getPaintSettings(getType());
		Matrix matrix = context.getViewTransform();

		shape = new TextShape();
		shape.setTextColor(settings.getColor());
		shape.setTextAttributes(settings.getTextAttributes().clone());
		shape.setFont(settings.getFont().clone());

		if (nonNull(matrix)) {
			// Scale font.
			shape.getFont().setSize(shape.getFont().getSize() / matrix.getScaleX());
		}

		if (handle != -1) {
			shape.setHandle(handle);
		}

		recordAction(new TextAction(shape.getHandle()));

		super.begin(point, page);

		fireToolEvent(new ShapePaintEvent(ToolEventType.BEGIN, shape, shape.getBounds()));
	}

	@Override
	public void execute(PenPoint2D point) {
		// No action
	}

	@Override
	public void end(PenPoint2D point) {
		shape.setLocation(point);
		shape.getBounds().setSize(1, 1); // Real size is calculated by the UI element

		page.addAction(new CreateShapeAction(page, shape));

		// Fire tool-end-event first, then attach the listener.
		super.end(point);

		textFontChanged(shape);

		shape.addTextChangeListener(this);
	}

	@Override
	public ToolType getType() {
		return ToolType.TEXT;
	}

	@Override
	public void textChanged(TextShape shape) {
		recordAction(new TextChangeAction(shape.getHandle(), shape.getText()));

		fireToolEvent(new ShapePaintEvent(ToolEventType.EXECUTE, shape,
				shape.getDirtyBounds().clone()));
	}

	@Override
	public void textFontChanged(TextShape shape) {
		recordAction(new TextFontChangeAction(shape.getHandle(),
				shape.getTextColor().clone(), shape.getFont().clone(),
				shape.getTextAttributes().clone()));

		fireToolEvent(new ShapePaintEvent(ToolEventType.EXECUTE, shape,
				shape.getDirtyBounds().clone()));
	}

	@Override
	public void textLocationChanged(TextShape shape) {
		recordAction(new TextLocationChangeAction(shape.getHandle(),
				shape.getBounds().clone().getLocation()));

		fireToolEvent(new ShapePaintEvent(ToolEventType.EXECUTE, shape,
				shape.getDirtyBounds().clone()));
	}

	@Override
	public void textRemoved(TextShape shape) {
		recordAction(new TextRemoveAction(shape.getHandle()));
	}

}
