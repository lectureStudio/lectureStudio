/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

import java.awt.Desktop;
import java.net.URL;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.model.VersionInfo;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.core.view.NewVersionView;

public class NewVersionPresenter extends Presenter<NewVersionView> {

	private VersionInfo version;


	@Inject
	NewVersionPresenter(ApplicationContext context, NewVersionView view) {
		super(context, view);
	}

	@Override
	public void initialize() {
		view.setType(NotificationType.DEFAULT);
		view.setOnClose(this::close);
		view.setOnDownload(this::download);
		view.setOnOpenUrl(this::openUrl);
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	public void setVersion(VersionInfo version) {
		this.version = version;

		String versionStr = version.version;
		versionStr = versionStr.startsWith("v") ?
				versionStr.substring(1) :
				versionStr;

		DateTimeFormatter dateFormatter = DateTimeFormatter
				.ofLocalizedDate(FormatStyle.LONG)
				.withLocale(context.getConfiguration().getLocale());

		Dictionary dict = context.getDictionary();
		String message = MessageFormat.format(dict.get("version.message"),
				versionStr, version.published.format(dateFormatter),
				VersionInfo.getAppVersion(),
				VersionInfo.getAppPublishDate().format(dateFormatter));

		view.setTitle(dict.get("version.new"));
		view.setMessage(message);
	}

	private void openUrl() {
		browse(version.htmlUrl);
	}

	private void download() {
		browse(version.downloadUrl);
	}

	private void browse(URL url) {
		try {
			Desktop.getDesktop().browse(url.toURI());
		}
		catch (Exception e) {
			logException(e, "Browse URL failed");
		}
	}
}
