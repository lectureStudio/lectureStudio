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

package org.lecturestudio.swing.window;

import static java.util.Objects.nonNull;

import com.google.common.eventbus.Subscribe;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;

import javax.swing.JWindow;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.view.Screens;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.bus.event.DisplayConfigEvent;
import org.lecturestudio.core.bus.event.ShutdownEvent;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.Screen;
import org.lecturestudio.core.view.ScreenViewType;
import org.lecturestudio.core.view.SlidePresentationView;
import org.lecturestudio.core.view.SlideViewOverlay;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.swing.components.SlideView;
import org.lecturestudio.swing.converter.ColorConverter;
import org.lecturestudio.swing.converter.RectangleConverter;

public class PresentationWindow extends AbstractWindow implements SlidePresentationView {

	private final RenderController renderController;

	private Action onVisibleAction;

	private SlideView slideView;

	private Document doc;


	public PresentationWindow(ApplicationContext context, Screen screen, RenderController renderController) {
		super(context, Screens.getGraphicsConfiguration(screen));

		this.renderController = renderController;

		init(screen);
	}

	public ScreenViewType getType() {
		return ScreenViewType.SLIDE;
	}

	@Override
	public void setBackgroundColor(Color color) {
		setBackground(ColorConverter.INSTANCE.to(color));
	}

    @Subscribe
	public void onEvent(final ShutdownEvent event) {
		invoke(this::close);
	}

    @Subscribe
	public void onEvent(final DisplayConfigEvent event) {
		invoke(() -> {
			slideView.repaint();
		});
	}

	@Override
	public void setPage(Page page, PresentationParameter parameter) {
		setPage(page);

		if (!isVisible()) {
			return;
		}

		updateSlideView(page, parameter);

		if (!page.getDocument().equals(doc)) {
			// Do a re-layout. Documents may have different page layouts.
			getWindow().getContentPane().doLayout();
		}

		doc = page.getDocument();
	}

	@Override
	public void close() {
		ApplicationBus.unregister(this);

		setVisible(false);
		dispose();
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			Page page = getPage();

			if (nonNull(page)) {
				centerSlideView();

				PresentationParameterProvider paramProvider = getContext()
						.getPagePropertyProvider(ViewType.Presentation);

				updateSlideView(page, paramProvider.getParameter(page));
			}
		}

		super.setVisible(visible);
	}

	@Override
	public void setOnVisible(Action action) {
		this.onVisibleAction = action;
	}

	@Override
	public void addOverlay(SlideViewOverlay overlay) {
		slideView.addOverlay(overlay);
	}

	@Override
	public void removeOverlay(SlideViewOverlay overlay) {
		slideView.removeOverlay(overlay);
	}

	@Override
	protected Window createWindow(GraphicsConfiguration gc) {
		return new JWindow(gc);
	}

	@Override
	public void setBackground(java.awt.Color color) {
		getWindow().getContentPane().setBackground(color);
	}

	@Override
	public JWindow getWindow() {
		return (JWindow) super.getWindow();
	}

	protected void init(Screen screen) {
		Rectangle screenBounds = RectangleConverter.INSTANCE.to(screen.getBounds());
		Color background = getContext().getConfiguration().getDisplayConfig().getBackgroundColor();

		transformScreenBounds(screenBounds);

		setBackground(ColorConverter.INSTANCE.to(background));
		setBounds(screenBounds);
		setAlwaysOnTop(true);

		setDocument(getContext().getDocuments().getSelectedDocument());

		getWindow().addWindowListener(new WindowAdapter() {

			@Override
			public void windowOpened(WindowEvent e) {
				if (nonNull(onVisibleAction)) {
					onVisibleAction.execute();
				}
			}
		});

		slideView = new SlideView();
		slideView.setViewType(ViewType.Presentation);
		slideView.setPageRenderer(renderController);
		slideView.setPage(getPage());
		slideView.setSize(new Dimension(screenBounds.width, screenBounds.height));
		slideView.setPreferredSize(new Dimension(screenBounds.width, screenBounds.height));
		slideView.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				centerSlideView();
			}
		});

		getWindow().getContentPane().add(slideView);

		ApplicationBus.register(this);
	}

	private void updateSlideView(Page page, PresentationParameter parameter) {
		if (slideView.getCanvasBounds().isEmpty()) {
			slideView.dispatchEvent(new ComponentEvent(slideView,
					ComponentEvent.COMPONENT_RESIZED));
		}

		slideView.parameterChanged(page, parameter);
		slideView.setPage(page);
	}

	private void transformScreenBounds(Rectangle screenBounds) {
		GraphicsDevice dev = Screens.getDefaultScreenDevice();
		AffineTransform defaultTx = dev.getDefaultConfiguration().getDefaultTransform();
		AffineTransform windowTx = getWindow().getGraphicsConfiguration().getDefaultTransform();

		// Main screen / current screen scale ratio.
		double sx = defaultTx.getScaleX() / windowTx.getScaleX();
		double sy = defaultTx.getScaleY() / windowTx.getScaleY();

		// Scale current screen size.
		double x = screenBounds.getX() / sx;
		double y = screenBounds.getY() / sy;
		double w = screenBounds.getWidth() / sx;
		double h = screenBounds.getHeight() / sy;

		screenBounds.setRect(x, y, w, h);
	}

	private void centerSlideView() {
		Dimension size = getSize();
		Dimension viewSize = slideView.getCanvasBounds().getSize();

		int x = (size.width - viewSize.width) / 2;
		int y = (size.height - viewSize.height) / 2;

		slideView.setSlideLocation(x, y);
		slideView.repaint();
	}
}
