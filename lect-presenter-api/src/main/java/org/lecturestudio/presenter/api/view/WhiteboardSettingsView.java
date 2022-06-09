/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.PresentationParameter;

public interface WhiteboardSettingsView extends SettingsBaseView {

	void setBackgroundColor(ObjectProperty<Color> color);

	void setGridColor(ObjectProperty<Color> color);

	void setGridInterval(DoubleProperty interval);

	void setShowGridAutomatically(BooleanProperty show);

	void setShowGridOnDisplays(BooleanProperty show);

	void setShowVerticalGridLines(BooleanProperty show);

	void setShowHorizontalGridLines(BooleanProperty show);

	void setWhiteboardPage(Page page, PresentationParameter parameter);

}
