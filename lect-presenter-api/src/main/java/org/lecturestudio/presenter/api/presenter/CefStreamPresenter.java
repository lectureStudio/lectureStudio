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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.File;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.CefSettings.LogSeverity;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefCommandLine;
import org.cef.handler.*;
import org.cef.misc.BoolRef;
import org.cef.network.CefRequest;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.View;
import org.lecturestudio.presenter.api.config.StreamConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;

import me.friwi.jcefmaven.CefAppBuilder;

public abstract class CefStreamPresenter<T extends View> extends Presenter<T> {

	protected CefClient client;

	protected CefBrowser browser;


	protected CefStreamPresenter(ApplicationContext context, T view) {
		super(context, view);
	}

	@Override
	public void initialize() {
		try {
			initCef();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		disposeCef();

		super.close();
	}

	protected void initCefClient() {

	}

	private void initCef() throws Exception {
		PresenterContext ctx = (PresenterContext) context;
		CefApp cefApp = ctx.getCefApp();

		if (isNull(cefApp)) {
			File jcefDir = new File(context.getDataLocator().toAppDataPath("jcef"));

			CefAppBuilder builder = new CefAppBuilder();
			builder.setInstallDir(jcefDir);

			CefSettings settings = builder.getCefSettings();
			settings.log_file = context.getDataLocator().toAppDataPath("jcef-debug.log");
			settings.log_severity = LogSeverity.LOGSEVERITY_DISABLE;
			settings.windowless_rendering_enabled = false;
			settings.locale = context.getConfiguration().getLocale().toLanguageTag();
			settings.cache_path = new File(jcefDir, "cache").getAbsolutePath();

			cefApp = builder.build();

			ctx.setCefApp(cefApp);

			CefApp.addAppHandler(new CefAppHandlerAdapter(null) {

				@Override
				public void onBeforeCommandLineProcessing(String processType,
						CefCommandLine commandLine) {
					super.onBeforeCommandLineProcessing(processType, commandLine);
					if (processType.isEmpty()) {
						commandLine.appendSwitchWithValue("enable-media-stream",
								"true");
					}
				}
			});
		}

		client = cefApp.createClient();
		client.addMessageRouter(CefMessageRouter.create());
		client.addRequestHandler(new RequestHandler(ctx));
		client.addDisplayHandler(new CefDisplayHandlerAdapter() {
			@Override
			public boolean onConsoleMessage(CefBrowser browser, LogSeverity level, String message, String source, int line) {
				return super.onConsoleMessage(browser, level, message, source, line);
			}
		});

		initCefClient();

		// Open the running course url.
		StreamConfiguration streamConfig = ctx.getConfiguration().getStreamConfig();
		String accessLink = ctx.getCourse().getDefaultAccessLink();
		String serverName = streamConfig.getServerName();
		String serverUrl = String.format("https://%s", serverName);
		String courseApiUrl = String.format("%s/course/api/%s", serverUrl, accessLink);

		browser = client.createBrowser(courseApiUrl, false, false);
	}

	private void disposeCef() {
		if (nonNull(client)) {
			client.dispose();
		}
	}



	/**
	 * This request handler is used to auto-authenticate us and view the stream
	 * as a legit participant.
	 */
	static class RequestHandler extends CefRequestHandlerAdapter {

		StreamConfiguration streamConfig;

		/** Stream api request handler. */
		CefResourceRequestHandler requestHandler;


		RequestHandler(PresenterContext context) {
			streamConfig = context.getConfiguration().getStreamConfig();

			requestHandler = new CefResourceRequestHandlerAdapter() {

				@Override
				public boolean onBeforeResourceLoad(CefBrowser browser,
						CefFrame frame, CefRequest request) {
					// Authenticate with user's access token to avoid entering
					// credentials in the login form.
					String token = streamConfig.getAccessToken();

					request.setHeaderByName("ApiKey", token, true);

					return false;
				}
			};
		}

		@Override
		public CefResourceRequestHandler getResourceRequestHandler(
				CefBrowser browser, CefFrame frame, CefRequest request,
				boolean isNavigation, boolean isDownload,
				String requestInitiator, BoolRef disableDefaultHandling) {
			String serverName = streamConfig.getServerName();
			String serverUrl = String.format("https://%s", serverName);
			String courseApiUrl = String.format("%s/course/api/", serverUrl);
			String apiUrl = String.format("%s/api/v1/course/", serverUrl);

			// Check if we hit an api url that requires authentication.
			if (request.getURL().startsWith(courseApiUrl) || request.getURL()
					.startsWith(apiUrl)) {
				return requestHandler;
			}

			return null;
		}
	}
}
