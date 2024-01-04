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

package org.lecturestudio.editor.javafx.view;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Collection;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.transform.TransformChangedEvent;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Matrix;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.stylus.StylusHandler;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PageObjectView;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.editor.api.view.SlidesView;
import org.lecturestudio.javafx.beans.converter.KeyEventConverter;
import org.lecturestudio.javafx.beans.converter.MatrixConverter;
import org.lecturestudio.javafx.control.SlideView;
import org.lecturestudio.javafx.control.ThumbnailPanel;
import org.lecturestudio.javafx.input.StylusListener;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.stylus.javafx.JavaFxStylusManager;

@FxmlView(name = "main-slides")
public class FxSlidesView extends VBox implements SlidesView {

	private final EventHandler<KeyEvent> keyEventHandler = this::onKeyEvent;

	private final ChangeListener<Node> sceneFocusListener = (o, oldNode, newNode) -> {
		onFocusChange(newNode);
	};

	private ConsumerAction<org.lecturestudio.core.input.KeyEvent> keyAction;

	private ConsumerAction<Document> selectDocumentAction;

	private ConsumerAction<Page> deletePageAction;

	private ConsumerAction<Page> selectPageAction;

	private ConsumerAction<Matrix> viewTransformAction;

	private RenderController pageRenderer;

	@FXML
	private ContextMenu contextMenu;

	@FXML
	private MenuItem deletePageMenuItem;

	@FXML
	private SplitPane tabSplitPane;

	@FXML
	private SlideView slideView;

	@FXML
	private TabPane tabPane;

	private StylusListener stylusListener;


	public FxSlidesView() {
		super();
	}

	@Override
	public void addDocument(Document doc, ApplicationContext context) {
		FxUtils.invoke(() -> {
			// Create the ThumbnailPanel for the TabPane.
			ThumbnailPanel thumbPanel = new ThumbnailPanel();
			thumbPanel.setPageRenderer(new RenderController(context, pageRenderer));
			thumbPanel.setDocument(doc, context.getPagePropertyProvider(
					ViewType.Preview), contextMenu);
			thumbPanel.addSelectListener(page -> {
				executeAction(selectPageAction, page);
			});

			// Create the Tab for the TabPane.
			Tab tab = new Tab();
			tab.setText(doc.getName());
			tab.setContent(thumbPanel);

			tabPane.getTabs().add(tab);
			tabPane.getSelectionModel().select(tab);
		});
	}

	@Override
	public void removeDocument(Document doc) {
		// Remove document tab.
		for (Tab tab : tabPane.getTabs()) {
			ThumbnailPanel thumbPanel = (ThumbnailPanel) tab.getContent();

			if (thumbPanel.getDocument().equals(doc)) {
				FxUtils.invoke(() -> {
					tabPane.getTabs().remove(tab);
				});
				break;
			}
		}
	}

	@Override
	public void selectDocument(Document doc) {
		// Select document tab.
		for (Tab tab : tabPane.getTabs()) {
			ThumbnailPanel thumbPanel = (ThumbnailPanel) tab.getContent();

			if (thumbPanel.getDocument().getName().equals(doc.getName())) {
				FxUtils.invoke(() -> {
					// Reload if document has changed.
					if (!thumbPanel.getDocument().equals(doc)) {
						// Prevent tab switching for quiz reloading.
						thumbPanel.setDocument(doc, null);
					}

					tabPane.getSelectionModel().select(tab);
				});
				break;
			}
		}
	}

	@Override
	public void repaint() {
		slideView.repaint();
	}

	@Override
	public Page getPage() {
		return slideView.getPage();
	}

	@Override
	public void setPage(Page page, PresentationParameter parameter) {
		FxUtils.invoke(() -> {
			slideView.parameterChanged(page, parameter);
			slideView.setPage(page);

			// Select page on the thumbnail panel.
			Tab tab = tabPane.getSelectionModel().getSelectedItem();

			if (nonNull(tab) && nonNull(tab.getContent())) {
				ThumbnailPanel thumbPanel = (ThumbnailPanel) tab.getContent();
				thumbPanel.selectPage(page);
			}
		});
	}

	@Override
	public void setPageRenderer(RenderController pageRenderer) {
		this.pageRenderer = pageRenderer;

		slideView.setPageRenderer(pageRenderer);
	}

	@Override
	public void setOnKeyEvent(ConsumerAction<org.lecturestudio.core.input.KeyEvent> action) {
		this.keyAction = action;
	}

	@Override
	public void setOnSelectDocument(ConsumerAction<Document> action) {
		this.selectDocumentAction = action;
	}

	@Override
	public void setOnDeletePage(ConsumerAction<Page> action) {
		this.deletePageAction = action;
	}

	@Override
	public void setOnSelectPage(ConsumerAction<Page> action) {
		this.selectPageAction = action;
	}

	@Override
	public void setOnViewTransform(ConsumerAction<Matrix> action) {
		this.viewTransformAction = action;
	}

	@Override
	public void removeAllPageObjectViews() {
		slideView.removeAllPageObjectViews();
	}

	@Override
	public Collection<PageObjectView<?>> getPageObjectViews() {
		return slideView.getPageObjectViews();
	}

	@Override
	public void addPageObjectView(PageObjectView<Shape> objectView) {
		slideView.addPageObjectView(objectView);
	}

	@Override
	public void removePageObjectView(PageObjectView<? extends Shape> objectView) {
		slideView.removePageObjectView(objectView);
	}

	@Override
	public void setStylusHandler(StylusHandler handler) {
		stylusListener = new StylusListener(handler, slideView);

		JavaFxStylusManager manager = JavaFxStylusManager.getInstance();
		manager.attachStylusListener(slideView, stylusListener);
	}

	@Override
	public void bindSeekProperty(BooleanProperty seekProperty) {
		slideView.seekProperty().set(seekProperty.get());

		seekProperty.addListener((observable, oldValue, newValue) -> {
			slideView.setSeek(seekProperty.get());
		});
	}

	@FXML
	private void initialize() {
		deletePageMenuItem.setOnAction(event -> {
			SlideView selectedView = (SlideView) deletePageMenuItem.getParentPopup().getOwnerNode();
			executeAction(deletePageAction, selectedView.getPage());
		});

		// Add the scene listener to attach shortcuts to the scene.
		slideView.sceneProperty().addListener((observable, oldScene, newScene) -> {
			onSceneSet(oldScene, newScene);
		});

		slideView.getPageTransform().addEventHandler(TransformChangedEvent.TRANSFORM_CHANGED, event -> {
			if (nonNull(viewTransformAction)) {
				viewTransformAction.execute(MatrixConverter.INSTANCE.from(slideView.getPageTransform()));
			}
		});

		// Add the width listener to keep the divider in the same position.
		tabSplitPane.widthProperty().addListener((observable, oldValue, newValue) -> {
			tabSplitPane.setDividerPositions(0.75);
		});

		// Add the tab listener to select the document that is shown within the tab.
		tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			onTabChange(newValue);
		});
	}

	private void onSceneSet(Scene oldScene, Scene newScene) {
		if (nonNull(oldScene)) {
			// Clean up.
			oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, keyEventHandler);
			oldScene.removeEventFilter(KeyEvent.KEY_RELEASED, keyEventHandler);

			oldScene.focusOwnerProperty().removeListener(sceneFocusListener);
		}
		if (nonNull(newScene)) {
			// Attach listeners to the new scene.
			newScene.addEventFilter(KeyEvent.KEY_PRESSED, keyEventHandler);
			newScene.addEventFilter(KeyEvent.KEY_RELEASED, keyEventHandler);

			newScene.focusOwnerProperty().addListener(sceneFocusListener);

			slideView.requestFocus();
		}
	}

	private void onKeyEvent(KeyEvent event) {
		if (slideView.isFocused()) {
			if (event.getEventType() == KeyEvent.KEY_PRESSED) {
				org.lecturestudio.core.input.KeyEvent keyEvent = KeyEventConverter.INSTANCE.from(event);

				executeAction(keyAction, keyEvent);
			}
		}
	}

	private void onFocusChange(Node node) {
		if (nonNull(node)) {
			Parent parent = node.getParent();

			if (nonNull(parent)) {
				if (PageObjectView.class.isAssignableFrom(parent.getClass())) {
					node.requestFocus();
					return;
				}
			}

			if (node instanceof TextField ||
					node instanceof TitledPane) {
				node.requestFocus();
				return;
			}
		}

		// Fall back to the SlideView.
		if (isVisible()) {
			slideView.requestFocus();
		}
	}

	private void onTabChange(Tab tab) {
		if (isNull(tab)) {
			executeAction(selectDocumentAction, null);
			return;
		}

		ThumbnailPanel thumbPanel = (ThumbnailPanel) tab.getContent();

		executeAction(selectDocumentAction, thumbPanel.getDocument());
	}
}
