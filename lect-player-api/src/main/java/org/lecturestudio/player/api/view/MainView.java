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

package org.lecturestudio.player.api.view;

import java.util.function.Predicate;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.View;
import org.lecturestudio.core.view.ViewLayer;

public interface MainView extends View {

	Rectangle2D getBounds();

	void close();

	void hide();

	void removeView(View view, ViewLayer layer);

	void showView(View view, ViewLayer layer);

	void setFullscreen(boolean fullscreen);

	void setMenuVisible(boolean visible);

	void setOnKeyEvent(Predicate<KeyEvent> action);

	void setOnShown(Action action);

	void setOnClose(Action action);

}
