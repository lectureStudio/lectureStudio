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

package org.lecturestudio.player.api.presenter;

import java.io.IOException;
import java.util.Locale;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.model.Contributor;
import org.lecturestudio.core.model.Sponsor;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.service.ContributorsYamlSource;
import org.lecturestudio.core.service.DataSource;
import org.lecturestudio.core.service.SponsorsYamlSource;
import org.lecturestudio.core.view.AboutView;

public class AboutPresenter extends Presenter<AboutView> {

	@Inject
	AboutPresenter(ApplicationContext context, AboutView view) {
		super(context, view);
	}

	@Override
	public void initialize() throws IOException {
		Locale locale = context.getConfiguration().getLocale();
		String lang = locale.getLanguage();
		String contributorsRes = String.format("/resources/contributors/contributors_%s.yaml", lang);
		String sponsorsRes = String.format("/resources/contributors/sponsors_%s.yaml", lang);

		DataSource<Contributor> contributorsSource = new ContributorsYamlSource(contributorsRes);
		DataSource<Sponsor> sponsorsSource = new SponsorsYamlSource(sponsorsRes);

		view.setContributors(contributorsSource.getAll());
		view.setSponsors(sponsorsSource.getAll());
		view.setProperties(System.getProperties());
		view.setOnClose(this::close);
	}

}
