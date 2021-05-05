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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.transform.TransformChangedEvent;
import javafx.stage.Stage;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.model.DocumentOutline;
import org.lecturestudio.core.model.DocumentOutlineItem;
import org.lecturestudio.presenter.javafx.input.StylusListener;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Matrix;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.SlideNote;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PageObjectView;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.javafx.beans.converter.KeyEventConverter;
import org.lecturestudio.javafx.beans.converter.MatrixConverter;
import org.lecturestudio.javafx.control.ExtTab;
import org.lecturestudio.javafx.control.ExtTabPane;
import org.lecturestudio.javafx.control.MessageView;
import org.lecturestudio.javafx.control.SlideView;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.stylus.StylusHandler;
import org.lecturestudio.presenter.api.view.SlidesView;
import org.lecturestudio.presenter.javafx.control.EditableThumbnailPanel;
import org.lecturestudio.stylus.javafx.JavaFxStylusManager;
import org.lecturestudio.web.api.message.MessengerMessage;

@FxmlView(name = "main-slides")
public class FxSlidesView extends VBox implements SlidesView {

	private final ChangeListener<Node> sceneFocusListener = (observable, oldNode, newNode) -> {
		onFocusChange(newNode);
	};

	private final ChangeListener<Boolean> fullscreenListener = (observable, oldValue, newValue) -> {
		ReadOnlyProperty property = (ReadOnlyProperty) observable;

		showBottomTabPane((Stage) property.getBean());
	};

	private final EventHandler<KeyEvent> keyEventHandler = event -> onKeyEvent(event);

	private final List<Node> focusRegistry;

	private final ResourceBundle resources;

	private ConsumerAction<org.lecturestudio.core.input.KeyEvent> keyAction;

	private ConsumerAction<Document> selectDocumentAction;

	private ConsumerAction<Page> selectPageAction;

	private ConsumerAction<Matrix> viewTransformAction;

	private Action newPageAction;

	private Action deletePageAction;

	private double notesDividerPosition;

	private boolean extendedFullscreen;

	private RenderController pageRenderer;

	@FXML
	private SplitPane tabSplitPane;

	@FXML
	private SplitPane notesSplitPane;

	@FXML
	private SlideView slideView;

	@FXML
	private TabPane tabPane;

	@FXML
	private MessageView messageView;

	@FXML
	private ExtTabPane bottomTabPane;

	@FXML
	private ExtTab notesTab;

	@FXML
	private ExtTab latexTab;

	@FXML
	private TextArea notesTextArea;

	@FXML
	private TextArea latexTextArea;


	@Inject
	public FxSlidesView(ResourceBundle resources) {
		super();

		this.resources = resources;
		this.focusRegistry = new ArrayList<>();
	}

	@Override
	public void addPageObjectView(PageObjectView<?> objectView) {
		slideView.addPageObjectView(objectView);
	}

	@Override
	public void removePageObjectView(PageObjectView<?> objectView) {
		slideView.removePageObjectView(objectView);
	}

	@Override
	public void removeAllPageObjectViews() {
		slideView.removeAllPageObjectViews();
	}

	@Override
	public List<PageObjectView<?>> getPageObjectViews() {
		return slideView.getPageObjectViews();
	}

	@Override
	public void addDocument(Document doc, PresentationParameterProvider ppProvider) {
		FxUtils.invoke(() -> {
			// Create the ThumbnailPanel for the TabPane.
			EditableThumbnailPanel thumbPanel = new EditableThumbnailPanel(resources);
			thumbPanel.setPageRenderer(pageRenderer);
			thumbPanel.setDocument(doc, ppProvider);
			thumbPanel.addSelectListener(page -> {
				executeAction(selectPageAction, page);
			});

			if (doc.isWhiteboard()) {
				thumbPanel.setOnNewPage(() -> {
					executeAction(newPageAction);
				});
				thumbPanel.setOnDeletePage(() -> {
					executeAction(deletePageAction);
				});
			}

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
			EditableThumbnailPanel thumbPanel = (EditableThumbnailPanel) tab.getContent();

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
			EditableThumbnailPanel thumbPanel = (EditableThumbnailPanel) tab.getContent();

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
				EditableThumbnailPanel thumbPanel = (EditableThumbnailPanel) tab.getContent();
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
	public void setPageNotes(List<SlideNote> notes) {
		StringBuffer buffer = new StringBuffer();

		if (nonNull(notes)) {
			for (Iterator<SlideNote> notesIter = notes.iterator(); notesIter.hasNext();) {
				buffer.append(notesIter.next().getText());

				if (notesIter.hasNext()) {
					buffer.append("\n");
				}
			}

			// Show red highlight, if notes-view is hidden and page notes are available.
			ExtTab tab = bottomTabPane.getSelectionModel().getSelectedItem();

			if ((isNull(tab) || tab != notesTab) && !notes.isEmpty()) {
				notesTab.getStyleClass().add("notes-highlight");
				notesTab.getGraphic().getStyleClass().add("notes-highlight");
			}
		}

		notesTextArea.setText(buffer.toString());
	}

	@Override
	public void setOutline(DocumentOutline outline) {

	}

	@Override
	public void bindShowOutline(BooleanProperty showProperty) {

	}

	@Override
	public void setExtendedFullscreen(boolean extended) {
		extendedFullscreen = extended;

		if (isNull(getScene())) {
			return;
		}

		Stage stage = (Stage) getScene().getWindow();

		showBottomTabPane(stage);
	}

	@Override
	public void setStylusHandler(StylusHandler handler) {
		StylusListener stylusListener = new StylusListener(handler, slideView);

		JavaFxStylusManager manager = JavaFxStylusManager.getInstance();
		manager.attachStylusListener(slideView, stylusListener);

		// For testing purposes.
		/*
		MouseListener mouseListener = new MouseListener(handler, slideView);
		slideView.setOnMouseDragged(mouseListener);
		slideView.setOnMouseMoved(mouseListener);
		slideView.setOnMousePressed(mouseListener);
		slideView.setOnMouseReleased(mouseListener);
		*/
	}

	@Override
	public void setLaTeXText(String text) {
		FxUtils.invoke(() -> {
			latexTextArea.setText(text);
		});
	}

	@Override
	public void setMessengerState(ExecutableState state) {
		boolean started = state == ExecutableState.Started;

		FxUtils.invoke(() -> {
			messageView.setVisible(started);
			messageView.setManaged(started);
		});
	}

	@Override
	public void setMessengerMessage(MessengerMessage message) {
		// Run in pending runnable to avoid UI blocking.
		Platform.runLater(() -> {
			messageView.setDate(message.getDate());
			messageView.setHost(message.getRemoteAddress());
			messageView.setMessage(message.getMessage().getText());
		});
	}

	@Override
	public void setSelectedToolType(ToolType type) {
		FxUtils.invoke(() -> {
			boolean isTeXTool = type == ToolType.LATEX;

			latexTab.setDisable(!isTeXTool);

			if (isTeXTool && !latexTab.isSelected()) {
				bottomTabPane.getSelectionModel().select(latexTab);
			}
			if (!isTeXTool && latexTab.isSelected()) {
				bottomTabPane.getSelectionModel().clearSelection();
			}
		});
	}

	@Override
	public void setOnKeyEvent(ConsumerAction<org.lecturestudio.core.input.KeyEvent> action) {
		this.keyAction = action;
	}

	@Override
	public void setOnLaTeXText(ConsumerAction<String> action) {
		latexTextArea.textProperty().addListener(observable -> {
			executeAction(action, latexTextArea.getText());
		});
	}

	@Override
	public void setOnSelectDocument(ConsumerAction<Document> action) {
		this.selectDocumentAction = action;
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
	public void setOnNewPage(Action action) {
		this.newPageAction = action;
	}

	@Override
	public void setOnDeletePage(Action action) {
		this.deletePageAction = action;
	}

	@Override
	public void setOnOutlineItem(ConsumerAction<DocumentOutlineItem> action) {

	}

	@FXML
	private void initialize() {
		focusRegistry.add(latexTextArea);

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

		// Initially deselect the tabs.
		bottomTabPane.getSelectionModel().selectedItemProperty().addListener(new InvalidationListener() {

			@Override
			public void invalidated(Observable observable) {
				bottomTabPane.getSelectionModel().selectedItemProperty().removeListener(this);
				bottomTabPane.getSelectionModel().clearSelection();
			}
		});

		bottomTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (nonNull(bottomTabPane.getSelectionModel().getSelectedItem())) {
				// Expanded. Show tab content.
				notesSplitPane.setDividerPosition(0, notesDividerPosition);

				// Since the notes are shown, remove the highlight notification.
				notesTab.getStyleClass().remove("notes-highlight");
				notesTab.getGraphic().getStyleClass().remove("notes-highlight");
			}
			else {
				// Collapsed. Remember last split position.
				notesDividerPosition = notesSplitPane.getDividerPositions()[0];

				// Hide tab content.
				notesSplitPane.setDividerPosition(0, 1.0);

				slideView.requestFocus();
			}
		});
	}

	private void onSceneSet(Scene oldScene, Scene newScene) {
		if (nonNull(oldScene)) {
			// Clean up.
			Stage stage = (Stage) oldScene.getWindow();
			stage.fullScreenProperty().removeListener(fullscreenListener);

			oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, keyEventHandler);
			oldScene.removeEventFilter(KeyEvent.KEY_RELEASED, keyEventHandler);

			oldScene.focusOwnerProperty().removeListener(sceneFocusListener);
		}
		if (nonNull(newScene)) {
			// Attach listeners to the new scene.
			newScene.addEventFilter(KeyEvent.KEY_PRESSED, keyEventHandler);
			newScene.addEventFilter(KeyEvent.KEY_RELEASED, keyEventHandler);

			newScene.focusOwnerProperty().addListener(sceneFocusListener);

			if (nonNull(newScene.getWindow())) {
				Stage stage = (Stage) newScene.getWindow();

				showBottomTabPane(stage);

				stage.fullScreenProperty().addListener(fullscreenListener);
			}
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

			if (focusRegistry.contains(node)) {
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

		EditableThumbnailPanel thumbPanel = (EditableThumbnailPanel) tab.getContent();

		executeAction(selectDocumentAction, thumbPanel.getDocument());
	}

	private void showBottomTabPane(Stage stage) {
		boolean show = !stage.isFullScreen() || !extendedFullscreen;

		if (show) {
			if (!notesSplitPane.getItems().contains(bottomTabPane)) {
				notesSplitPane.getItems().add(bottomTabPane);
			}
		}
		else {
			notesSplitPane.getItems().remove(1);
		}
	}

}
