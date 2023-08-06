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

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.recording.action.PlaybackAction;

public abstract class StrokeTool<T extends Shape> extends Tool {

	protected T shape;

	protected Integer shapeHandle;

	protected ObjectProperty<StrokeWidthSettings> strokeWidthMultiplier = new ObjectProperty<>(StrokeWidthSettings.NORMAL);


	protected abstract PlaybackAction createPlaybackAction();

	protected abstract T createShape();


	protected StrokeTool(ToolContext context) {
		super(context);
	}

	protected StrokeTool(ToolContext context, Integer shapeHandle) {
		super(context);

		this.shapeHandle = shapeHandle;
	}

	@Override
	public void begin(PenPoint2D point, Page page) {
		shape = createShape();

		if (nonNull(shapeHandle)) {
			shape.setHandle(shapeHandle);
		}

		recordAction(createPlaybackAction());

		super.begin(point, page);

		beginInternal(point, page);

		fireToolEvent(new ShapePaintEvent(ToolEventType.BEGIN, shape, shape.getBounds()));
	}

	@Override
	public void execute(PenPoint2D point) {
		super.execute(point);

		Rectangle2D dirtyArea = shape.getBounds().clone();

		executeInternal(point);

		dirtyArea.union(shape.getBounds());

		fireToolEvent(new ShapePaintEvent(ToolEventType.EXECUTE, shape, dirtyArea));
	}

	@Override
	public void end(PenPoint2D point) {
		super.end(point);

		Rectangle2D dirtyArea = shape.getBounds().clone();

		endInternal(point);

		dirtyArea.union(shape.getBounds());

		fireToolEvent(new ShapePaintEvent(ToolEventType.END, shape, dirtyArea));
	}

	protected Stroke createStroke() {
		StrokeSettings settings = context.getPaintSettings(getType());

		Stroke stroke = new Stroke();
		stroke.setColor(settings.getColor().derive(settings.getAlpha()));
		stroke.setWidth(settings.getWidth() * settings.getStrokeWidthSettings().getMultiplier());

		return stroke;
	}

	protected void beginInternal(PenPoint2D point, Page page) {

	}

	protected void executeInternal(PenPoint2D point) {

	}

	protected void endInternal(PenPoint2D point) {

	}
}
