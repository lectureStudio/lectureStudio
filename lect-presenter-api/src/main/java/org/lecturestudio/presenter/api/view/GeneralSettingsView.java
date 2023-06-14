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

import org.lecturestudio.core.app.Theme;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.geometry.Dimension2D;

import java.util.List;
import java.util.Locale;

public interface GeneralSettingsView extends SettingsBaseView {

	void setTheme(ObjectProperty<Theme> theme);

	void setThemes(List<Theme> themes);

	void setLocale(ObjectProperty<Locale> locale);

	void setLocales(List<Locale> locales);

	void setCheckNewVersion(BooleanProperty check);

	void setStartMaximized(BooleanProperty maximized);

	void setStartFullscreen(BooleanProperty fullscreen);

	void setTabletMode(BooleanProperty tabletMode);

	void setSaveAnnotationsOnClose(BooleanProperty saveAnnotations);

	void setPageSelectionDelay(IntegerProperty delay);

	void setExtendedFullscreen(BooleanProperty extended);

	void setExtendPageDimension(ObjectProperty<Dimension2D> dimension);

	void setTextSize(DoubleProperty size);

}
