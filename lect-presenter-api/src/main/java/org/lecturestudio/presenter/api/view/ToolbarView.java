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
import org.lecturestudio.core.text.TeXFont;
import org.lecturestudio.core.tool.PaintSettings;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.View;

public interface ToolbarView extends View {

	void setDocument(Document doc);

	void setPage(Page page, PresentationParameter parameter);

	void setScreensAvailable(boolean screensAvailable);

	void setPresentationViewsVisible(boolean viewsVisible);

	void setRecordingState(ExecutableState state);

	void setStreamingState(ExecutableState state);

	void showRecordNotification(boolean show);

	void setOnUndo(Action action);

	void setOnRedo(Action action);

	void setOnPreviousSlide(Action action);

	void setOnNextSlide(Action action);

	void setOnPreviousBookmark(Action action);

	void setOnNextBookmark(Action action);

	void setOnNewBookmark(Action action);

	void setOnCustomPaletteColor(ConsumerAction<Color> action);

	void setOnCustomColor(Action action);

	void setOnColor1(Action action);

	void setOnColor2(Action action);

	void setOnColor3(Action action);

	void setOnColor4(Action action);

	void setOnColor5(Action action);

	void setOnColor6(Action action);

	void setOnPenTool(Action action);

	void setOnHighlighterTool(Action action);

	void setOnPointerTool(Action action);

	void setOnTextSelectTool(Action action);

	void setOnLineTool(Action action);

	void setOnArrowTool(Action action);

	void setOnRectangleTool(Action action);

	void setOnEllipseTool(Action action);

	void setOnSelectTool(Action action);

	void setOnEraseTool(Action action);

	void setOnTextTool(Action action);

	void setOnTextBoxFont(ConsumerAction<Font> action);

	void setOnTeXTool(Action action);

	void setOnTeXBoxFont(ConsumerAction<TeXFont> action);

	void setOnClearTool(Action action);

	void setOnShowGrid(Action action);

	void setOnExtend(Action action);

	void setOnWhiteboard(Action action);

	void setOnEnableDisplays(ConsumerAction<Boolean> action);

	void setOnZoomInTool(Action action);

	void setOnZoomOutTool(Action action);

	void setOnPanTool(Action action);

	void setOnStartRecording(Action action);

	void setOnStopRecording(Action action);

	void selectColorButton(ToolType toolType, PaintSettings settings);

	void selectNewBookmarkButton(boolean hasBookmark);

	void selectToolButton(ToolType toolType);

	void openCustomizeToolbarDialog();

	void bindEnableStream(BooleanProperty enable);

	void bindEnableStreamMicrophone(BooleanProperty enable);

	void bindEnableStreamCamera(BooleanProperty enable);

	void bindEnableScreenSharing(Action action);

	void setOnSelectQuiz(Action action);

	void setOnAudienceMessage(Action action);

}
