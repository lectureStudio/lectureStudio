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

package org.lecturestudio.presenter.api.presenter;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.presenter.api.input.Shortcut;
import org.lecturestudio.presenter.api.view.ShortcutsView;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

public class ShortcutsPresenter extends Presenter<ShortcutsView> {

	@Inject
	ShortcutsPresenter(ApplicationContext context, ShortcutsView view) {
		super(context, view);
	}

	@Override
	public void initialize() throws IOException {
		Dictionary dict = context.getDictionary();
		Map<String, List<Shortcut>> shortcuts = new LinkedHashMap<>(); // Preserve insertion order

		for (var shortcut : Shortcut.values()) {
			String description = dict.get(shortcut.getDescription());
			List<Shortcut> mapList = shortcuts.get(description);

			if (isNull(mapList)) {
				mapList = new ArrayList<>();
			}

			mapList.add(shortcut);
			shortcuts.put(description, mapList);
		}

		view.setShortcuts(shortcuts);
		view.setOnClose(this::close);
	}
}
