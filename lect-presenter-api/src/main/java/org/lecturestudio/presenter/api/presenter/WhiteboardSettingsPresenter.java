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

package org.lecturestudio.presenter.api.presenter;

import java.io.IOException;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.WhiteboardConfiguration;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentType;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.ParameterChangeListener;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.view.WhiteboardSettingsView;

public class WhiteboardSettingsPresenter extends Presenter<WhiteboardSettingsView> {

	private final WhiteboardConfiguration config;


	@Inject
	public WhiteboardSettingsPresenter(ApplicationContext context,
			WhiteboardSettingsView view) {
		super(context, view);

		config = context.getConfiguration().getWhiteboardConfig();
	}

	@Override
	public void initialize() {
		view.setBackgroundColor(config.backgroundColorProperty());
		view.setGridColor(config.gridColorProperty());
		view.setGridInterval(config.verticalLinesIntervalProperty());
		view.setShowGridAutomatically(config.showGridAutomaticallyProperty());
		view.setShowGridOnDisplays(config.showGridOnDisplaysProperty());
		view.setShowHorizontalGridLines(config.horizontalLinesVisibleProperty());
		view.setShowVerticalGridLines(config.verticalLinesVisibleProperty());
		view.setOnReset(this::reset);

		initWhiteboard();

		config.verticalLinesIntervalProperty().addListener((o, oldValue, newValue) -> {
			config.setHorizontalLinesInterval(newValue);
		});
	}

	public void reset() {
		WhiteboardConfiguration defaultConfig = new DefaultConfiguration().getWhiteboardConfig();

		config.setBackgroundColor(defaultConfig.getBackgroundColor());
		config.setShowGridAutomatically(defaultConfig.getShowGridAutomatically());
		config.setShowGridOnDisplays(defaultConfig.getShowGridOnDisplays());
		config.setGridColor(defaultConfig.getGridColor());
		config.setVerticalLinesVisible(defaultConfig.getVerticalLinesVisible());
		config.setVerticalLinesInterval(defaultConfig.getVerticalLinesInterval());
		config.setHorizontalLinesVisible(defaultConfig.getHorizontalLinesVisible());
		config.setHorizontalLinesInterval(defaultConfig.getHorizontalLinesInterval());
	}

	private void initWhiteboard() {
		Document whiteboard;

		try {
			whiteboard = new Document();
			whiteboard.setDocumentType(DocumentType.WHITEBOARD);
			whiteboard.createPage();
			//whiteboard.close();

			Page page = whiteboard.getCurrentPage();

			PresentationParameterProvider ppProvider = context.getPagePropertyProvider(
					ViewType.User);
			ppProvider.addParameterChangeListener(new ParameterChangeListener() {

				@Override
				public Page forPage() {
					return page;
				}

				@Override
				public void parameterChanged(Page page, PresentationParameter parameter) {
					view.setWhiteboardPage(page, parameter);
				}
			});

			PresentationParameter parameter = ppProvider.getParameter(page);
			parameter.setShowGrid(true);

			view.setWhiteboardPage(page, parameter);
		}
		catch (IOException e) {
			logException(e, "Create whiteboard document failed");
		}
	}
}
