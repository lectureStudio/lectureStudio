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

package org.lecturestudio.presenter.javafx.view;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.function.Predicate;

import javax.inject.Inject;

import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.Theme;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.View;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.javafx.beans.converter.KeyEventConverter;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.MainView;

@FxmlView(name = "main-window")
public class FxMainView extends StackPane implements MainView {

	private final ApplicationContext context;

	private final Deque<Node> viewStack;

	private TranslateTransition menuTransition;

	private EventHandler<MouseEvent> menuBarVisibilityHandler;

	private EventHandler<MouseEvent> menuBarMouseHandler;

	private ConsumerAction<Rectangle2D> boundsAction;

	private Predicate<org.lecturestudio.core.input.KeyEvent> keyAction;

	private Action shownAction;

	private Action closeAction;

	private boolean showMenuBar;

	@FXML
	private Pane menuBar;

	@FXML
	private BorderPane contentPane;


	@Inject
	public FxMainView(ApplicationContext context) {
		super();

		this.context = context;
		this.viewStack = new ArrayDeque<>();
	}

	@Override
	public Rectangle2D getViewBounds() {
		Window window = getScene().getWindow();

		return new Rectangle2D(window.getX(), window.getY(), window.getWidth(), window.getHeight());
	}

	@Override
	public void closeView() {
		// Fire close request in order to shutdown appropriately.
		Window window = getScene().getWindow();
		window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
	}

	@Override
	public void hideView() {
		FxUtils.invoke(() -> {
			Window window = getScene().getWindow();
			window.hide();
		});
	}

	@Override
	public void removeView(View view, ViewLayer layer) {
		if (layer == ViewLayer.Window) {
			return;
		}

		checkNodeView(view);

		Node nodeView = (Node) view;

		removeNode(nodeView);
	}

	@Override
	public void showView(View view, ViewLayer layer) {
		if (layer == ViewLayer.Window) {
			return;
		}

		checkNodeView(view);

		Node nodeView = (Node) view;

		switch (layer) {
			case Content:
				showNode(nodeView, true);
				break;

			case Dialog:
			case Notification:
				showNodeOnTop(nodeView);
				break;
		}
	}

	@Override
	public void setFullscreen(boolean fullscreen) {
		FxUtils.invoke(() -> {
			Stage stage = (Stage) getScene().getWindow();
			stage.setFullScreen(fullscreen);
		});
	}

	@Override
	public void setMenuVisible(boolean visible) {
		showMenuBar = visible;

		if (isNull(getScene())) {
			return;
		}

		Stage stage = (Stage) getScene().getWindow();

		if (stage.isFullScreen()) {
			attachMenuBar(visible);
		}
	}

	@Override
	public void setOnKeyEvent(Predicate<org.lecturestudio.core.input.KeyEvent> action) {
		this.keyAction = action;
	}

	@Override
	public void setOnBounds(ConsumerAction<Rectangle2D> action) {
		this.boundsAction = action;
	}

	@Override
	public void setOnShown(Action action) {
		this.shownAction = action;
	}

	@Override
	public void setOnClose(Action action) {
		this.closeAction = action;
	}

	private void onKeyEvent(KeyEvent event) {
		if (event.getEventType() == KeyEvent.KEY_PRESSED) {
			org.lecturestudio.core.input.KeyEvent keyEvent = KeyEventConverter.INSTANCE.from(event);

			if (nonNull(keyAction) && keyAction.test(keyEvent)) {
				event.consume();
			}
		}
	}

	private void removeNode(Node nodeView) {
		FxUtils.invoke(() -> {
			boolean removed = getChildren().remove(nodeView);

			if (!removed) {
				showNode(nodeView, false);
			}
		});
	}

	private void showNode(Node nodeView, boolean show) {
		Node currentView = contentPane.getCenter();
		boolean isSame = currentView == nodeView;

		if (show) {
			if (!isSame) {
				viewStack.push(nodeView);

				FxUtils.invoke(() -> {
					contentPane.setCenter(nodeView);
				});
			}
		}
		else if (isSame) {
			Node lastView = viewStack.pop();

			if (!viewStack.isEmpty()) {
				lastView = viewStack.pop();
			}

			showNode(lastView, true);
		}
	}

	private void showNodeOnTop(Node nodeView) {
		FxUtils.invoke(() -> {
			getChildren().add(nodeView);
		});
	}

	@FXML
	private void initialize() {
		Configuration config = context.getConfiguration();

		// Set application wide font size.
		setStyle(String.format(Locale.US, "-fx-font-size: %.2fpt;", config.getUIControlSize()));

		addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyEvent);

		config.themeProperty().addListener((observable, oldTheme, newTheme) -> {
			// Load new theme.
			loadTheme(newTheme);
			// Unload old theme.
			unloadTheme(oldTheme);
		});

		// Init view-stack with default node.
		viewStack.push(contentPane.getCenter());

		menuTransition = new TranslateTransition(new Duration(300), menuBar);

		menuBarVisibilityHandler = (event) -> {
			if (event.getSceneY() < 5) {
				showMenuBar();
			}
		};

		menuBarMouseHandler = (event) -> {
			Node target = (Node) event.getTarget();
			Node parent = target.getParent();

			while (parent != null) {
				if (parent.equals(menuBar)) {
					// Mouse is inside the menu-bar.
					return;
				}

				parent = parent.getParent();
			}

			// Mouse exited the menu-bar bounds. Hide the menu-bar.
			hideMenuBar();
		};

		sceneProperty().addListener(new ChangeListener<>() {

			@Override
			public void changed(ObservableValue<? extends Scene> observableValue, Scene oldScene, Scene newScene) {
				if (nonNull(newScene)) {
					sceneProperty().removeListener(this);

					onSceneSet(newScene);
				}
			}
		});
	}

	private void loadTheme(Theme theme) {
		if (nonNull(theme.getFile())) {
			String themeUrl = getClass().getResource(theme.getFile()).toExternalForm();
			getScene().getStylesheets().add(themeUrl);
		}
	}

	private void unloadTheme(Theme theme) {
		if (nonNull(theme.getFile())) {
			String themeUrl = getClass().getResource(theme.getFile()).toExternalForm();
			getScene().getStylesheets().remove(themeUrl);
		}
	}

	private void attachMenuBar(boolean attach) {
		if (attach) {
			if (menuBar != contentPane.getTop()) {
				// Reset state changed by the translate transition.
				menuBar.setTranslateY(0);

				contentPane.setTop(menuBar);

				removeEventFilter(MouseEvent.MOUSE_MOVED, menuBarMouseHandler);
				removeEventFilter(MouseEvent.MOUSE_MOVED, menuBarVisibilityHandler);
			}
		}
		else {
			// Detach menu-bar.
			contentPane.setTop(null);

			addEventFilter(MouseEvent.MOUSE_MOVED, menuBarVisibilityHandler);
		}
	}

	private void showMenuBar() {
		removeEventFilter(MouseEvent.MOUSE_MOVED, menuBarVisibilityHandler);

		getChildren().add(menuBar);

		menuTransition.setOnFinished(e -> {
			addEventFilter(MouseEvent.MOUSE_MOVED, menuBarMouseHandler);
		});
		menuTransition.setFromY(-menuBar.prefHeight(-1));
		menuTransition.setToY(0);
		menuTransition.play();
	}

	private void hideMenuBar() {
		removeEventFilter(MouseEvent.MOUSE_MOVED, menuBarMouseHandler);

		menuTransition.setFromY(0);
		menuTransition.setToY(-menuBar.prefHeight(-1));
		menuTransition.setOnFinished(e -> {
			getChildren().remove(menuBar);
			addEventFilter(MouseEvent.MOUSE_MOVED, menuBarVisibilityHandler);
		});
		menuTransition.play();
	}

	private void onSceneSet(Scene scene) {
		scene.windowProperty().addListener(new ChangeListener<>() {

			@Override
			public void changed(ObservableValue<? extends Window> observable, Window oldWindow, Window newWindow) {
				if (nonNull(newWindow)) {
					scene.windowProperty().removeListener(this);

					onStageSet((Stage) newWindow);
				}
			}
		});
	}

	private void onStageSet(Stage stage) {
		// It's imperative to load fxml-defined stylesheets prior to the user-defined theme
		// stylesheet, so the theme can override initial styles.
		getStylesheets().forEach(file -> {
			loadTheme(new Theme("defined", file));
		});
		// Remove loaded stylesheets to avoid additional loading by the scene itself.
		getStylesheets().clear();

		loadTheme(context.getConfiguration().getTheme());

		stage.setOnShown(event -> {
			executeAction(shownAction);
		});
		stage.setOnCloseRequest(event -> {
			// Consume event. Don't close the window yet.
			event.consume();

			executeAction(closeAction);
		});
		stage.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
			attachMenuBar(!newValue || showMenuBar);
		});

		ChangeListener<Number> boundsListener = new WindowBoundsListener();

		stage.xProperty().addListener(boundsListener);
		stage.yProperty().addListener(boundsListener);
		stage.widthProperty().addListener(boundsListener);
		stage.heightProperty().addListener(boundsListener);
	}

	private void checkNodeView(View view) {
		if (!Node.class.isAssignableFrom(view.getClass())) {
			throw new IllegalArgumentException("View expected to be a JavaFX Node.");
		}
	}



	private class WindowBoundsListener implements ChangeListener<Number> {

		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
			Window stage = getScene().getWindow();
			Rectangle2D bounds = new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());

			executeAction(boundsAction, bounds);
		}
	}
}
