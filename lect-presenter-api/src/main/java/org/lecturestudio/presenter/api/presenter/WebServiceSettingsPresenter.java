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

import static java.util.Objects.nonNull;

import java.util.List;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.util.ListChangeListener;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.config.QuizConfiguration;
import org.lecturestudio.presenter.api.view.WebServiceSettingsView;
import org.lecturestudio.web.api.filter.RegexFilter;
import org.lecturestudio.web.api.filter.RegexRule;

public class WebServiceSettingsPresenter extends Presenter<WebServiceSettingsView> {

	private final DefaultConfiguration defaultConfig;


	@Inject
	public WebServiceSettingsPresenter(ApplicationContext context, WebServiceSettingsView view) {
		super(context, view);

		this.defaultConfig = new DefaultConfiguration();
	}

	public void addQuizRegex() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		QuizConfiguration quizConfig = config.getQuizConfig();
		RegexFilter regexFilter = quizConfig.getInputFilter();

		regexFilter.registerRule(new RegexRule());
	}

	public void deleteQuizRegex(RegexRule regexRule) {
		if (nonNull(regexRule)) {
			PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
			QuizConfiguration quizConfig = config.getQuizConfig();
			RegexFilter regexFilter = quizConfig.getInputFilter();

			regexFilter.unregisterRule(regexRule);
		}
	}

	public void reset() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();

		config.setClassroomName(defaultConfig.getClassroomName());
		config.setClassroomShortName(defaultConfig.getClassroomShortName());
		config.getQuizConfig().getInputFilter().setRules(defaultConfig.getQuizConfig().getInputFilter().getRules());
		config.getDisplayConfig().setIpPosition(defaultConfig.getDisplayConfig().getIpPosition());
	}

	@Override
	public void initialize() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		QuizConfiguration quizConfig = config.getQuizConfig();

		RegexFilter regexFilter = quizConfig.getInputFilter();
		List<RegexRule> regexRules = regexFilter.getRules();

		regexFilter.addListener(new ListChangeListener<>() {

			@Override
			public void listChanged(ObservableList<RegexRule> list) {
				view.setQuizRegexRules(list);
			}
		});

		view.setClassroomName(config.classroomNameProperty());
		view.setClassroomShortName(config.classroomShortNameProperty());
		view.setQuizRegexRules(regexRules);
		view.setDisplayIpPosition(config.getDisplayConfig().ipPositionProperty());
		view.setOnAddQuizRegex(this::addQuizRegex);
		view.setOnDeleteQuizRegex(this::deleteQuizRegex);
		view.setOnReset(this::reset);
	}
}
