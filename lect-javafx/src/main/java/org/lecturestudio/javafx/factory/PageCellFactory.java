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

package org.lecturestudio.javafx.factory;

import java.util.function.Consumer;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.PresentationParameterProvider;

public class PageCellFactory implements Callback<ListView<Page>, ListCell<Page>> {

	private final PresentationParameterProvider ppProvider;

	private final RenderController pageRenderer;

	private final Consumer<Page> selectionCallback;

	private final ContextMenu contextMenu;


	public PageCellFactory(RenderController pageRenderer,
						   PresentationParameterProvider ppProvider,
						   Consumer<Page> selectionCallback,
						   ContextMenu contextMenu) {
		this.pageRenderer = pageRenderer;
		this.ppProvider = ppProvider;
		this.selectionCallback = selectionCallback;
		this.contextMenu = contextMenu;
	}

	@Override
	public ListCell<Page> call(ListView<Page> param) {
		return new PageListCell(pageRenderer, ppProvider, selectionCallback, contextMenu);
	}

}
