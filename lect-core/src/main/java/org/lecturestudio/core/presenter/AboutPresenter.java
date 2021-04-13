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

package org.lecturestudio.core.presenter;

import static java.util.Objects.isNull;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.io.ResourceLoader;
import org.lecturestudio.core.model.Contributor;
import org.lecturestudio.core.model.Sponsor;
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
		Configuration config = context.getConfiguration();
		Dictionary dict = context.getDictionary();

		Manifest manifest = new Manifest(ResourceLoader.getResourceAsStream(
				JarFile.MANIFEST_NAME));
		Attributes attr = manifest.getMainAttributes();

		DateTimeFormatter pkgDateFormatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm")
				.withLocale(config.getLocale());

		String pkgBuildDate = attr.getValue("Build-Date");
		String pkgVersion = attr.getValue("Package-Version");

		if (isNull(pkgBuildDate)) {
			pkgVersion = "dev";
		}
		if (isNull(pkgBuildDate)) {
			pkgBuildDate = LocalDateTime.now().format(pkgDateFormatter);
		}

		DateTimeFormatter dateFormatter = DateTimeFormatter
				.ofLocalizedDate(FormatStyle.LONG)
				.withLocale(config.getLocale());

		String buildDate = LocalDateTime.parse(pkgBuildDate, pkgDateFormatter).format(dateFormatter);
		String version = MessageFormat.format(dict.get("about.version"), pkgVersion);
		String lang = config.getLocale().getLanguage();
		String contributorsRes = String.format("/resources/contributors/contributors_%s.yaml", lang);
		String sponsorsRes = String.format("/resources/contributors/sponsors_%s.yaml", lang);

		DataSource<Contributor> contributorsSource = new ContributorsYamlSource(contributorsRes);
		DataSource<Sponsor> sponsorsSource = new SponsorsYamlSource(sponsorsRes);

		view.setAppName(config.getApplicationName());
		view.setAppVersion(version);
		view.setAppBuildDate(buildDate);
		view.setWebsite("https://www.lecturestudio.org");
		view.setIssueWebsite("https://github.com/lectureStudio/lectureStudio/issues");
		view.setContributors(contributorsSource.getAll());
		view.setSponsors(sponsorsSource.getAll());
		view.setProperties(System.getProperties());
		view.setOnClose(this::close);
	}

}
