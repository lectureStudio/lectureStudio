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

package org.lecturestudio.presenter.swing.view;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.util.UIScale;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.*;
import javax.swing.text.JTextComponent;

import org.lecturestudio.core.app.view.Screens;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.View;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.presenter.api.view.MainView;
import org.lecturestudio.swing.AwtResourceLoader;
import org.lecturestudio.swing.components.GlassPane;
import org.lecturestudio.swing.converter.KeyEventConverter;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;

@SwingView(name = "main-view")
public class SwingMainView extends JPanel implements MainView, KeyEventDispatcher {

	private final Deque<Component> viewStack;

	private ConsumerAction<Rectangle2D> boundsAction;

	private ConsumerAction<Boolean> focusAction;

	private Predicate<org.lecturestudio.core.input.KeyEvent> keyAction;

	private Action shownAction;

	private Action closeAction;

	private Rectangle lastWindowBounds;


	SwingMainView() {
		super();

		this.viewStack = new ArrayDeque<>();
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);
		initialize();
	}

	@Override
	public Rectangle2D getViewBounds() {
		JFrame window = getWindow();

		return new Rectangle2D(window.getX(), window.getY(), window.getWidth(), window.getHeight());
	}

	@Override
	public void closeView() {
		getWindow().dispose();
	}

	@Override
	public void hideView() {
		getWindow().setVisible(false);
	}

	@Override
	public void removeView(View view, ViewLayer layer) {
		if (layer == ViewLayer.Window) {
			return;
		}

		checkComponentView(view);

		Component componentView = (Component) view;

		switch (layer) {
			case Content -> removeComponent(componentView);
			case Dialog, Notification -> removeComponentOnTop(componentView);
		}
	}

	@Override
	public void showView(View view, ViewLayer layer) {
		if (layer == ViewLayer.Window) {
			return;
		}

		checkComponentView(view);

		Component componentView = (Component) view;

		switch (layer) {
			case Content -> showComponent(componentView, true);
			case Dialog, Notification -> showComponentOnTop(componentView);
		}
	}

	@Override
	public void setFullscreen(boolean fullscreen) {
		JFrame window = getWindow();

		if (window.isUndecorated() == fullscreen) {
			return;
		}

		Rectangle windowBounds;
		boolean resizeable;

		if (fullscreen) {
			setUndecorated(window, true);

			GraphicsDevice device = Screens.getScreenDevice(window);
			windowBounds = device.getDefaultConfiguration().getBounds();

			resizeable = false;

			lastWindowBounds = window.getBounds();
		}
		else {
			setUndecorated(window, false);
			windowBounds = lastWindowBounds;
			resizeable = true;
		}

		if (isNull(windowBounds)) {
			window.pack();
			window.setLocationRelativeTo(null);
		}
		else {
			window.setBounds(windowBounds);
		}

		window.setResizable(resizeable);
		window.validate();
		window.setVisible(true);
	}

	@Override
	public void setMenuVisible(boolean visible) {

	}

	@Override
	public void setOnKeyEvent(Predicate<KeyEvent> action) {
		keyAction = action;
	}

	@Override
	public void setOnBounds(ConsumerAction<Rectangle2D> action) {
		boundsAction = action;
	}

	@Override
	public void setOnFocus(ConsumerAction<Boolean> action) {
		focusAction = action;
	}

	@Override
	public void setOnShown(Action action) {
		shownAction = action;
	}

	@Override
	public void setOnClose(Action action) {
		closeAction = action;
	}

	@Override
	public boolean dispatchKeyEvent(java.awt.event.KeyEvent event) {
		if (!getWindow().isFocused()) {
			return false;
		}
		if (event.getID() != java.awt.event.KeyEvent.KEY_PRESSED &&
			event.getID() != java.awt.event.KeyEvent.KEY_RELEASED) {
			return false;
		}

		Component focusOwner = event.getComponent();

		if (nonNull(focusOwner) && focusOwner.isShowing()) {
			boolean isTextComponent = focusOwner instanceof JTextComponent;
			boolean isAlphaNum = Character.isLetterOrDigit(event.getKeyCode()) |
					Character.isSpaceChar(event.getKeyCode());

			if (isTextComponent && isAlphaNum) {
				return !event.isControlDown() && !event.isShiftDown()
						&& !event.isAltDown() && !event.isMetaDown();
			}
		}

		org.lecturestudio.core.input.KeyEvent keyEvent = KeyEventConverter.INSTANCE.from(event);

		if (nonNull(keyAction) && keyAction.test(keyEvent)) {
			event.consume();
			return true;
		}

		return false;
	}

	private void removeComponent(Component componentView) {
		SwingUtils.invoke(() -> {
			int components = getComponentCount();
			remove(componentView);

			if (components == getComponentCount()) {
				showComponent(componentView, false);
			}
			else if (viewStack.peek() == componentView) {
				// Remove component from the view-stack and show the previous one.
				viewStack.pop();
				showComponent(viewStack.pop(), true);
			}
		});
	}

	private void removeComponentOnTop(Component componentView) {
		SwingUtils.invoke(() -> {
			JComponent glassPane = (JComponent) getWindow().getGlassPane();
			glassPane.remove(componentView);
			glassPane.setVisible(false);
		});
	}

	private void showComponent(Component componentView, boolean show) {
		BorderLayout contentLayout = (BorderLayout) getLayout();
		Component currentView = contentLayout.getLayoutComponent(BorderLayout.CENTER);
		boolean isSame = currentView == componentView;

		if (show) {
			if (!isSame) {
				viewStack.push(componentView);

				SwingUtils.invoke(() -> {
					if (nonNull(currentView)) {
						remove(currentView);
					}

					add(BorderLayout.CENTER, componentView);
					revalidate();
					repaint();
				});
			}
		}
		else if (isSame) {
			Component lastView = viewStack.pop();

			if (!viewStack.isEmpty()) {
				lastView = viewStack.pop();
			}

			showComponent(lastView, true);
		}
	}

	private void showComponentOnTop(Component componentView) {
		SwingUtils.invoke(() -> {
			JComponent glassPane = (JComponent) getWindow().getGlassPane();
			glassPane.add(componentView);
			glassPane.setVisible(true);
		});
	}

	private void setUndecorated(JFrame window, boolean decorated) {
		window.dispose();
		window.setUndecorated(decorated);
	}

	private JFrame getWindow() {
		return (JFrame) SwingUtilities.getWindowAncestor(this);
	}

	private void checkComponentView(View view) {
		if (!Component.class.isAssignableFrom(view.getClass())) {
			throw new IllegalArgumentException("View expected to be an AWT Component");
		}
	}

	private void initialize() {
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		KeyboardFocusManager keyboardManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		keyboardManager.addKeyEventDispatcher(this);

		addHierarchyListener(new HierarchyListener() {

			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0) {
					removeHierarchyListener(this);

					// Init view-stack with default component.
					BorderLayout contentLayout = (BorderLayout) getLayout();
					Component currentView = contentLayout.getLayoutComponent(BorderLayout.CENTER);
					viewStack.push(currentView);

					windowAttached();
				}
			}
		});
	}

	private void windowAttached() {
		final JFrame window = getWindow();

		List<Image> icons = new ArrayList<>();
		icons.add(AwtResourceLoader.getImage("gfx/app-icon/24.png"));
		icons.add(AwtResourceLoader.getImage("gfx/app-icon/32.png"));
		icons.add(AwtResourceLoader.getImage("gfx/app-icon/48.png"));
		icons.add(AwtResourceLoader.getImage("gfx/app-icon/128.png"));

		window.setIconImages(icons);
		window.setGlassPane(new GlassPane());
		window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		window.setPreferredSize(UIScale.scale(window.getPreferredSize()));

		window.addWindowListener(new WindowAdapter() {

			@Override
			public void windowOpened(WindowEvent e) {
				executeAction(shownAction);
			}

			@Override
			public void windowClosing(WindowEvent e) {
				executeAction(closeAction);
			}
		});
		window.addWindowFocusListener(new WindowFocusListener() {

			@Override
			public void windowGainedFocus(WindowEvent e) {
				executeAction(focusAction, true);
			}

			@Override
			public void windowLostFocus(WindowEvent e) {
				executeAction(focusAction, false);
			}
		});

		window.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentMoved(ComponentEvent e) {
				updateBounds();
			}

			@Override
			public void componentResized(ComponentEvent e) {
				updateBounds();
			}

			private void updateBounds() {
				final AffineTransform transform = window.getGraphicsConfiguration()
						.getDefaultTransform();
				final Rectangle bounds = window.getBounds();

				double sx = transform.getScaleX();
				double sy = transform.getScaleY();

				executeAction(boundsAction,
						new Rectangle2D(bounds.x * sx, bounds.y * sy,
								bounds.width * sx, bounds.height * sy));
			}
		});
	}
}
