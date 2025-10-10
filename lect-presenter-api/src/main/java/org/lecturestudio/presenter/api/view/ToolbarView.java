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

package org.lecturestudio.presenter.api.view;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.tool.PaintSettings;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.View;
import org.lecturestudio.presenter.api.model.ManualStateObserver;

/**
 * Interface defining a toolbar view for a presentation application.
 * Provides functionality for document navigation, recording, streaming, and various drawing/annotation tools.
 *
 * @author Alex Andres
 */
public interface ToolbarView extends View {

	/**
	 * Sets the current document to update the view state.
	 *
	 * @param doc the document to set.
	 */
	void setDocument(Document doc);

	/**
	 * Sets the current page and its presentation parameters to update the view state.
	 *
	 * @param page      the page to set.
	 * @param parameter the presentation parameters for the page.
	 */
	void setPage(Page page, PresentationParameter parameter);

	/**
	 * Sets whether additional screens are available for the presentation.
	 *
	 * @param screensAvailable true if additional screens are available, false otherwise.
	 */
	void setScreensAvailable(boolean screensAvailable);

	/**
	 * Sets the visibility of presentation views.
	 *
	 * @param viewsVisible true if presentation views should be visible, false otherwise.
	 */
	void setPresentationViewsVisible(boolean viewsVisible);

	/**
	 * Sets the current recording state.
	 *
	 * @param state the state of the recording.
	 */
	void setRecordingState(ExecutableState state);

	/**
	 * Sets the current streaming state.
	 *
	 * @param state the state of the streaming.
	 */
	void setStreamingState(ExecutableState state);

	/**
	 * Shows or hides the recording notification.
	 *
	 * @param show true to show the notification, false to hide it.
	 */
	void showRecordNotification(boolean show);

	/**
	 * Sets the action to perform when undo is triggered.
	 *
	 * @param action the undo action.
	 */
	void setOnUndo(Action action);

	/**
	 * Sets the action to perform when redo is triggered.
	 *
	 * @param action the redo action.
	 */
	void setOnRedo(Action action);

	/**
	 * Sets the action to perform when navigating to the previous slide.
	 *
	 * @param action the previous slide action.
	 */
	void setOnPreviousSlide(Action action);

	/**
	 * Sets the action to perform when navigating to the next slide.
	 *
	 * @param action the next slide action.
	 */
	void setOnNextSlide(Action action);

	/**
	 * Sets the action to perform when navigating to the previous bookmark.
	 *
	 * @param action the previous bookmark action.
	 */
	void setOnPreviousBookmark(Action action);

	/**
	 * Sets the action to perform when navigating to the next bookmark.
	 *
	 * @param action the next bookmark action.
	 */
	void setOnNextBookmark(Action action);

	/**
	 * Sets the action to perform when creating a new bookmark.
	 *
	 * @param action the new bookmark action.
	 */
	void setOnNewBookmark(Action action);

	/**
	 * Sets the action to perform when a custom palette color is selected.
	 *
	 * @param action the consumer action for the custom palette color.
	 */
	void setOnCustomPaletteColor(ConsumerAction<Color> action);

	/**
	 * Sets the action to perform when custom color selection is triggered.
	 *
	 * @param action the custom color action.
	 */
	void setOnCustomColor(Action action);

	/**
	 * Sets the action to perform when color 1 is selected.
	 *
	 * @param action the color 1 action.
	 */
	void setOnColor1(Action action);

	/**
	 * Sets the action to perform when color 2 is selected.
	 *
	 * @param action the color 2 action.
	 */
	void setOnColor2(Action action);

	/**
	 * Sets the action to perform when color 3 is selected.
	 *
	 * @param action the color 3 action.
	 */
	void setOnColor3(Action action);

	/**
	 * Sets the action to perform when color 4 is selected.
	 *
	 * @param action the color 4 action.
	 */
	void setOnColor4(Action action);

	/**
	 * Sets the action to perform when color 5 is selected.
	 *
	 * @param action the color 5 action.
	 */
	void setOnColor5(Action action);

	/**
	 * Sets the action to perform when color 6 is selected.
	 *
	 * @param action the color 6 action.
	 */
	void setOnColor6(Action action);

	/**
	 * Sets the action to perform when the pen tool is selected.
	 *
	 * @param action the pen tool action.
	 */
	void setOnPenTool(Action action);

	/**
	 * Sets the action to perform when the highlighter tool is selected.
	 *
	 * @param action the highlighter tool action.
	 */
	void setOnHighlighterTool(Action action);

	/**
	 * Sets the action to perform when the pointer tool is selected.
	 *
	 * @param action the pointer tool action.
	 */
	void setOnPointerTool(Action action);

	/**
	 * Sets the action to perform when the text selection tool is selected.
	 *
	 * @param action the text selection tool action.
	 */
	void setOnTextSelectTool(Action action);

	/**
	 * Sets the action to perform when the line tool is selected.
	 *
	 * @param action the line tool action.
	 */
	void setOnLineTool(Action action);

	/**
	 * Sets the action to perform when the arrow tool is selected.
	 *
	 * @param action the arrow tool action.
	 */
	void setOnArrowTool(Action action);

	/**
	 * Sets the action to perform when the rectangle tool is selected.
	 *
	 * @param action the rectangle tool action.
	 */
	void setOnRectangleTool(Action action);

	/**
	 * Sets the action to perform when the ellipse tool is selected.
	 *
	 * @param action the ellipse tool action.
	 */
	void setOnEllipseTool(Action action);

	/**
	 * Sets the action to perform when the selection tool is selected.
	 *
	 * @param action the selection tool action.
	 */
	void setOnSelectTool(Action action);

	/**
	 * Sets the action to perform when the eraser tool is selected.
	 *
	 * @param action the eraser tool action.
	 */
	void setOnEraseTool(Action action);

	/**
	 * Sets the action to perform when the text tool is selected.
	 *
	 * @param action the text tool action.
	 */
	void setOnTextTool(Action action);

	/**
	 * Sets the action to perform when a text box font is selected.
	 *
	 * @param action the consumer action for the text box font.
	 */
	void setOnTextBoxFont(ConsumerAction<Font> action);

	/**
	 * Sets the action to perform when the clear tool is selected.
	 *
	 * @param action the clear tool action.
	 */
	void setOnClearTool(Action action);

	/**
	 * Sets the action to perform when show grid is toggled.
	 *
	 * @param action the show grid action.
	 */
	void setOnShowGrid(Action action);

	/**
	 * Sets the action to perform when extend is triggered.
	 *
	 * @param action the extend view action.
	 */
	void setOnExtend(Action action);

	/**
	 * Sets the action to perform when whiteboard is triggered.
	 *
	 * @param action the whiteboard action.
	 */
	void setOnWhiteboard(Action action);

	/**
	 * Sets the action to perform when enabling or disabling displays.
	 *
	 * @param action the consumer action for enabling displays.
	 */
	void setOnEnableDisplays(ConsumerAction<Boolean> action);

	/**
	 * Sets the action to perform when the zoom in tool is selected.
	 *
	 * @param action the zoom in tool action.
	 */
	void setOnZoomInTool(Action action);

	/**
	 * Sets the action to perform when the zoom out tool is selected.
	 *
	 * @param action the zoom out tool action.
	 */
	void setOnZoomOutTool(Action action);

	/**
	 * Sets the action to perform when the pan tool is selected.
	 *
	 * @param action the pan tool action.
	 */
	void setOnPanTool(Action action);

	/**
	 * Sets the action to perform when starting recording.
	 *
	 * @param action the start recording action.
	 */
	void setOnStartRecording(Action action);

	/**
	 * Sets the action to perform when stopping recording.
	 *
	 * @param action the stop recording action.
	 */
	void setOnStopRecording(Action action);

	/**
	 * Selects the appropriate color button based on the tool type and paint settings.
	 *
	 * @param toolType the type of tool.
	 * @param settings the paint settings.
	 */
	void selectColorButton(ToolType toolType, PaintSettings settings);

	/**
	 * Selects the new bookmark button based on whether a bookmark exists.
	 *
	 * @param hasBookmark true if a bookmark exists, false otherwise.
	 */
	void selectNewBookmarkButton(boolean hasBookmark);

	/**
	 * Selects the tool button corresponding to the specified tool type.
	 *
	 * @param toolType the type of tool to select.
	 */
	void selectToolButton(ToolType toolType);

	/**
	 * Opens the dialog for customizing the toolbar.
	 */
	void openCustomizeToolbarDialog();

	/**
	 * Binds the enable stream property to the toolbar.
	 *
	 * @param enable the boolean property to bind.
	 */
	void bindEnableStream(BooleanProperty enable);

	/**
	 * Binds the enable stream microphone property to the toolbar.
	 *
	 * @param enable the boolean property to bind.
	 */
	void bindEnableStreamMicrophone(BooleanProperty enable);

	/**
	 * Binds the enable stream camera property to the toolbar.
	 *
	 * @param enable the boolean property to bind.
	 */
	void bindEnableStreamCamera(BooleanProperty enable);

	/**
	 * Binds the enabling screen sharing action to the toolbar.
	 *
	 * @param action the screen sharing action to bind.
	 */
	void bindEnableScreenSharing(Action action);

	/**
	 * Sets the manual state observer for the toolbar.
	 *
	 * @param observer the manual state observer.
	 */
	void setManualStateObserver(ManualStateObserver observer);

	/**
	 * Sets the action to perform when a quiz is selected.
	 *
	 * @param action the select quiz action.
	 */
	void setOnSelectQuiz(Action action);

	/**
	 * Sets the action to perform when an audience message is triggered.
	 *
	 * @param action the audience message action.
	 */
	void setOnAudienceMessage(Action action);

}
