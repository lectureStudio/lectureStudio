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

import java.io.IOException;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.configuration.GridConfiguration;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentType;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.ParameterChangeListener;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.view.GridSettingsView;

public class GridSettingsPresenter extends Presenter<GridSettingsView> {

	@Inject
	GridSettingsPresenter(ApplicationContext context, GridSettingsView view) {
		super(context, view);
	}

	@Override
	public void initialize() {
		Configuration config = context.getConfiguration();
		GridConfiguration gridConfig = config.getGridConfig();

		gridConfig.verticalLinesIntervalProperty().addListener((observable, oldValue, newValue) -> {
			gridConfig.setHorizontalLinesInterval(newValue);
		});

		Document whiteboard;

		try {
			whiteboard = new Document();
			whiteboard.setDocumentType(DocumentType.WHITEBOARD);
			whiteboard.createPage();
			//whiteboard.close();

			Page whiteboardPage = whiteboard.getCurrentPage();

			PresentationParameterProvider ppProvider = context.getPagePropertyProvider(ViewType.User);
			ppProvider.addParameterChangeListener(new ParameterChangeListener() {

				@Override
				public Page forPage() {
					return whiteboardPage;
				}

				@Override
				public void parameterChanged(Page page, PresentationParameter parameter) {
					view.setWhiteboardPage(page, parameter);
				}
			});

			PresentationParameter parameter = ppProvider.getParameter(whiteboardPage);
			parameter.setShowGrid(true);

			view.setWhiteboardPage(whiteboardPage, parameter);
		}
		catch (IOException e) {
			logException(e, "Create whiteboard document failed");
		}

		view.setGridColor(gridConfig.colorProperty());
		view.setGridInterval(gridConfig.verticalLinesIntervalProperty());
		view.setShowGridOnDisplays(gridConfig.showGridOnDisplaysProperty());
		view.setShowHorizontalGridLines(gridConfig.horizontalLinesVisibleProperty());
		view.setShowVerticalGridLines(gridConfig.verticalLinesVisibleProperty());
		view.setOnReset(this::reset);
	}

	private void reset() {
		GridConfiguration config = context.getConfiguration().getGridConfig();
		GridConfiguration defaultConfig = new DefaultConfiguration().getGridConfig();

		config.setShowGridOnDisplays(defaultConfig.getShowGridOnDisplays());
		config.setColor(defaultConfig.getColor());
		config.setVerticalLinesVisible(defaultConfig.getVerticalLinesVisible());
		config.setVerticalLinesInterval(defaultConfig.getVerticalLinesInterval());
		config.setHorizontalLinesVisible(defaultConfig.getHorizontalLinesVisible());
		config.setHorizontalLinesInterval(defaultConfig.getHorizontalLinesInterval());
	}
}
