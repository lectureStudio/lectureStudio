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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.google.common.eventbus.Subscribe;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import org.lecturestudio.core.app.ApplicationContext;
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
import org.lecturestudio.core.view.PresentationViewContext;
import org.lecturestudio.core.view.Screen;
import org.lecturestudio.core.view.SlidePresentationView;
import org.lecturestudio.core.view.SlideViewOverlay;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.swing.components.SlideView;
import org.lecturestudio.swing.converter.ColorConverter;
import org.lecturestudio.swing.view.SwingScreenView;

/**
 * A presentation window implementation that displays slide and screen content.
 * <p>
 * This class manages the display of presentation content on a specified screen, handling
 * both slide views and screen views. It responds to application events and manages the
 * rendering of presentation content through a {@link RenderController}.
 * <p>
 * The window can display document pages with specific presentation parameters and supports
 * overlays for additional visual elements.
 *
 * @see AbstractWindow
 * @see SlidePresentationView
 *
 * @author Alex Andres
 */
public class PresentationWindow extends AbstractWindow implements SlidePresentationView {

	/** Controller responsible for rendering content in the presentation window. */
	private final RenderController renderController;

	/** Action to be executed when the window becomes visible. */
	private Action onVisibleAction;

	/** The main slide view component for displaying presentation content. */
	private SlideView slideView;

	/** View component for displaying screen content. */
	private SwingScreenView screenView;

	/** The current document being displayed. */
	private Document doc;


	/**
	 * Creates a new presentation window.
	 *
	 * @param context          The application context providing configuration and resources.
	 * @param screen           The screen on which to display the presentation.
	 * @param renderController The controller used for rendering presentation content.
	 */
	public PresentationWindow(ApplicationContext context, Screen screen, RenderController renderController) {
		super(context, screen.getDevice().getDefaultConfiguration());

		this.renderController = renderController;

		init(screen);
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
			updateSlidePage();
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
	public void setPresentationViewContext(PresentationViewContext context) {
		if (isNull(context)) {
			return;
		}

		SwingUtilities.invokeLater(() -> {
			Container contentPane = getWindow().getContentPane();

			switch (context.getViewType()) {
				case SLIDE -> {
					context.configure(slideView);

					contentPane.remove(screenView);
					contentPane.add(slideView);

					updateSlidePage();
				}
				case SCREEN -> {
					context.configure(screenView);

					contentPane.remove(slideView);
					contentPane.add(screenView);
				}
			}

			contentPane.doLayout();
			contentPane.repaint();
		});
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

	/**
	 * Initializes the presentation window with the given screen configuration.
	 * <p>
	 * This method sets up the window properties (background, bounds, always-on-top),
	 * creates and configures the slide view and screen view components, registers
	 * event listeners, and connects to the application event bus.
	 *
	 * @param screen The screen on which to display the presentation window.
	 */
	protected void init(Screen screen) {
		Color background = getContext().getConfiguration().getDisplayConfig().getBackgroundColor();
		Rectangle screenBounds = transformScreenBounds(screen);

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

		screenView = new SwingScreenView();

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

	private void updateSlidePage() {
		Page page = getPage();

		if (nonNull(page)) {
			centerSlideView();

			PresentationParameterProvider paramProvider = getContext()
					.getPagePropertyProvider(ViewType.Presentation);

			updateSlideView(page, paramProvider.getParameter(page));
		}
	}

	private void updateSlideView(Page page, PresentationParameter parameter) {
		if (slideView.getCanvasBounds().isEmpty()) {
			slideView.dispatchEvent(new ComponentEvent(slideView,
					ComponentEvent.COMPONENT_RESIZED));
		}

		slideView.parameterChanged(page, parameter);
		slideView.setPage(page);
	}

	private Rectangle transformScreenBounds(Screen screen) {
		GraphicsConfiguration gfxConfig = screen.getDevice().getDefaultConfiguration();

		if (isNull(gfxConfig)) {
			throw new RuntimeException("GraphicsConfiguration does not exist for screen: " + screen);
		}

		return gfxConfig.getBounds();
	}

	private void centerSlideView() {
		SwingUtilities.invokeLater(() -> {
			Dimension size = getSize();
			Dimension viewSize = slideView.getCanvasBounds().getSize();

			int x = (size.width - viewSize.width) / 2;
			int y = (size.height - viewSize.height) / 2;

			slideView.setSlideLocation(x, y);
			slideView.repaint();
		});
	}
}
