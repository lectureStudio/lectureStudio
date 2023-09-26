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

import javax.inject.Inject;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefFocusHandlerAdapter;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.view.StreamView;

public class StreamViewPresenter extends CefStreamPresenter<StreamView> {

	@Inject
	StreamViewPresenter(ApplicationContext context, StreamView view) {
		super(context, view);
	}

	@Override
	public void initialize() {
		super.initialize();

		setOnClose(this::closeWindow);

		view.setTitle(context.getDictionary().get("stream.preview.title"));
		view.setBrowserComponent(browser.getUIComponent());
		view.setOnClose(this::close);
		view.setOnOpenUrl(this::openUrl);
		view.setOnReloadUrl(this::reloadUrl);
		view.open();
	}

	@Override
	public void destroy() {
		super.destroy();

		closeWindow();
	}

	@Override
	public void close() {
		PresenterContext ctx = (PresenterContext) context;
		ctx.getEventBus().unregister(this);
		ctx.setViewStream(false);

		super.close();
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Window;
	}

	@Override
	protected void initCefClient() {
		// Clear focus from the address field when the browser gains focus.
		client.addFocusHandler(new CefFocusHandlerAdapter() {

			@Override
			public void onGotFocus(CefBrowser browser) {
				view.setBrowserFocus(true);

				browser.setFocus(true);
			}

			@Override
			public void onTakeFocus(CefBrowser browser, boolean next) {
				view.setBrowserFocus(false);
			}
		});

		// Update the address field when the browser URL changes.
		client.addDisplayHandler(new CefDisplayHandlerAdapter() {

			@Override
			public void onAddressChange(CefBrowser browser, CefFrame frame,
					String url) {
				view.setBrowseAddress(url);
			}
		});
	}

	public void closeWindow() {
		view.close();
	}

	private void openUrl(String url) {
		browser.loadURL(url);
	}

	private void reloadUrl() {
		browser.reload();
	}
}
