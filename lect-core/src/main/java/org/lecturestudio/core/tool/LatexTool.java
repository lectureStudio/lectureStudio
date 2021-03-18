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
import org.lecturestudio.core.model.listener.TextChangeListener;
import org.lecturestudio.core.model.shape.TeXShape;
import org.lecturestudio.core.recording.action.LatexAction;
import org.lecturestudio.core.recording.action.LatexFontChangeAction;
import org.lecturestudio.core.recording.action.TextChangeAction;
import org.lecturestudio.core.recording.action.TextLocationChangeAction;
import org.lecturestudio.core.recording.action.TextRemoveAction;

/**
 * PaintTool for inserting and selecting text boxes in a page.
 *
 * @author Alex Andres
 */
public class LatexTool extends Tool implements TextChangeListener<TeXShape> {

	private final int handle;

	private Page page;

	private TeXShape shape;


	public LatexTool(ToolContext context) {
		this(context, -1);
	}

	public LatexTool(ToolContext context, int handle) {
		super(context);

		this.handle = handle;
	}

	public TeXShape getShape() {
		return shape;
	}

	public void copy(TeXShape shape) {
		this.shape.setLocation(shape.getLocation());
		this.shape.setFont(shape.getFont());
		this.shape.setText(shape.getText());
	}

	@Override
	public void begin(PenPoint2D point, Page page) {
		this.page = page;

		LatexToolSettings settings = context.getPaintSettings(getType());
		Matrix matrix = context.getViewTransform();

		shape = new TeXShape();
		shape.setTextColor(settings.getColor());
		shape.setFont(settings.getFont().clone());

		if (nonNull(matrix)) {
			// Scale font.
			shape.getFont().setSize((float) (shape.getFont().getSize() / matrix.getScaleX()));
		}

		if (handle != -1) {
			shape.setHandle(handle);
		}

		recordAction(new LatexAction(shape.getHandle()));

		super.begin(point, page);
	}

	@Override
	public void execute(PenPoint2D point) {
		// No action
	}

	@Override
	public void end(PenPoint2D point) {
		shape.setLocation(point);

		page.addShape(shape);

		// Fire tool-end-event first, then attach the listener.
		super.end(point);

		textFontChanged(shape);

		shape.addTextChangeListener(this);
	}

	@Override
	public ToolType getType() {
		return ToolType.LATEX;
	}

	@Override
	public void textChanged(TeXShape shape) {
		recordAction(new TextChangeAction(shape.getHandle(), shape.getText()));
	}

	@Override
	public void textFontChanged(TeXShape shape) {
		recordAction(new LatexFontChangeAction(shape.getHandle(),
				shape.getTextColor().clone(), shape.getFont().clone()));
	}

	@Override
	public void textLocationChanged(TeXShape shape) {
		recordAction(new TextLocationChangeAction(shape.getHandle(),
				shape.getLocation().clone()));
	}

	@Override
	public void textRemoved(TeXShape shape) {
		recordAction(new TextRemoveAction(shape.getHandle()));
	}

}
