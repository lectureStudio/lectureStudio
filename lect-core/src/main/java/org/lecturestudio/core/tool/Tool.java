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

import static java.util.Objects.isNull;

import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.input.KeyEvent.EventType;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.recording.action.KeyAction;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.action.ToolBeginAction;
import org.lecturestudio.core.recording.action.ToolEndAction;
import org.lecturestudio.core.recording.action.ToolExecuteAction;

/**
 * Abstract class for all PaintTools. PaintTools can be applied on the current
 * Page an modify there state dependent on the positions at which the Tool is
 * used.
 *
 * @author Alex Andres
 * @author Tobias
 */
public abstract class Tool {

	protected final ToolContext context;

	private KeyEvent keyEvent = new KeyEvent(0, EventType.TYPED);


	public Tool(ToolContext context) {
		this.context = context;
	}

	/**
	 * Gets the type of the Tool.
	 *
	 * @return the tool type.
	 */
	public abstract ToolType getType();

	/**
	 * Indicates whether the specific tool supports the given key event. This
	 * method is meant to be overridden by a specific tool. By default this
	 * method returns false.
	 *
	 * @param event The key event.
	 *
	 * @return true, if key event is supported by this tool, false otherwise.
	 */
	public boolean supportsKeyEvent(KeyEvent event) {
		return false;
	}

	/**
	 * Begins the Tool action
	 *
	 * @param page
	 */
	public void begin(PenPoint2D point, Page page) {
		recordAction(new ToolBeginAction(point));
	}

	/**
	 * Uses the Tool at the given position.
	 *
	 * @param point
	 */
	public void execute(PenPoint2D point) {
		recordAction(new ToolExecuteAction(point));
	}

	/**
	 * Ends the Tool action
	 */
	public void end(PenPoint2D point) {
		recordAction(new ToolEndAction(point));
	}

	public void setKeyEvent(KeyEvent event) {
		if (isNull(event)) {
			return;
		}
		if (event.equals(this.keyEvent)) {
			return;
		}

		boolean supportedRelease = supportsKeyEvent(keyEvent) && event.isReleased();
		boolean supported = supportsKeyEvent(event);

		this.keyEvent = event;

		if (supported || supportedRelease) {
			recordAction(new KeyAction(event));
		}
	}

	protected KeyEvent getKeyEvent() {
		return keyEvent;
	}

	protected void recordAction(PlaybackAction action) {
		context.recordAction(action);
	}

	protected void fireToolEvent(ToolEvent event) {
		context.fireToolEvent(event);
	}
}
