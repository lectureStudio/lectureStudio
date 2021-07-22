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

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Matrix;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.model.*;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.*;
import org.lecturestudio.presenter.api.stylus.StylusHandler;
import org.lecturestudio.presenter.api.view.SlidesView;
import org.lecturestudio.presenter.swing.input.StylusListener;
import org.lecturestudio.stylus.awt.AwtStylusManager;
import org.lecturestudio.swing.components.SlideView;
import org.lecturestudio.swing.components.*;
import org.lecturestudio.swing.converter.KeyEventConverter;
import org.lecturestudio.swing.converter.MatrixConverter;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;
import org.lecturestudio.web.api.message.MessengerMessage;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@SwingView(name = "main-slides")
public class SwingSlidesView extends JPanel implements SlidesView {

	private final static int MAX_TITLE_LENGTH = 20;

	private ConsumerAction<org.lecturestudio.core.input.KeyEvent> keyAction;

	private ConsumerAction<Document> selectDocumentAction;

	private ConsumerAction<DocumentOutlineItem> outlineAction;

	private ConsumerAction<Page> selectPageAction;

	private ConsumerAction<Matrix> viewTransformAction;

	private Action newPageAction;

	private Action deletePageAction;

	private Action startScreenCaptureAction;

	private Action stopScreenCaptureAction;

	private double notesDividerPosition;

	private boolean extendedFullscreen;

	private RenderController pageRenderer;

	private JSplitPane tabSplitPane;

	private JSplitPane notesSplitPane;

	private JSplitPane docSplitPane;

	private JScrollPane outlinePane;

	private JTree outlineTree;

	private SlideView slideView;

	private JTabbedPane tabPane;

	private MessageView messageView;

	private JTabbedPane bottomTabPane;

	private JTextArea notesTextArea;

	private JTextArea latexTextArea;

	private int bottomTabIndex;


	SwingSlidesView() {
		super();
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
		SwingUtils.invoke(() -> {

			// Create the ThumbnailPanel for the TabPane.
			ThumbPanel thumbPanel;
			switch (doc.getType()) {
				case WHITEBOARD:
					thumbPanel = new EditableThumbnailPanel();
					break;
				case SCREEN_CAPTURE:
					thumbPanel = new ScreenCaptureThumbnailPanel();
					break;
				default:
					thumbPanel = new ThumbPanel();
			}

			thumbPanel.setRenderController(pageRenderer);
			thumbPanel.setDocument(doc, ppProvider);
			thumbPanel.addSelectedSlideChangedListener(event -> {
				if (event.getNewValue() instanceof Page) {
					Page page = (Page) event.getNewValue();

					executeAction(selectPageAction, page);
				}
			});

			if (thumbPanel instanceof EditableThumbnailPanel) {
				EditableThumbnailPanel editableThumbPanel = (EditableThumbnailPanel) thumbPanel;

				editableThumbPanel.setOnNewPage(() -> executeAction(newPageAction));
				editableThumbPanel.setOnDeletePage(() -> executeAction(deletePageAction));
			}

			if (thumbPanel instanceof ScreenCaptureThumbnailPanel) {
				ScreenCaptureThumbnailPanel screenCaptureThumbnailPanel = (ScreenCaptureThumbnailPanel) thumbPanel;

				screenCaptureThumbnailPanel.setOnStartRecording(() -> executeAction(startScreenCaptureAction));
				screenCaptureThumbnailPanel.setOnStopRecording(() -> executeAction(stopScreenCaptureAction));
			}

			VerticalTab tab = new VerticalTab(tabPane.getTabPlacement());
			tab.setText(limitTitleLength(doc.getTitle()));

			tabPane.addTab(null, thumbPanel);
			tabPane.setTabComponentAt(tabPane.getTabCount() - 1, tab);
		});
	}

	private String limitTitleLength(String title) {
		if (title == null)
			return "";
		return title.length() > MAX_TITLE_LENGTH ? title.substring(0, MAX_TITLE_LENGTH) + "..." : title;
	}

	@Override
	public void removeDocument(Document doc) {
		// Remove document tab.
		int tabCount = tabPane.getTabCount();

		for (int i = 0; i < tabCount; i++) {
			ThumbPanel thumbnailPanel = (ThumbPanel) tabPane.getComponentAt(i);

			if (thumbnailPanel.getDocument().equals(doc)) {
				tabPane.remove(i);
				break;
			}
		}
	}

	@Override
	public void selectDocument(Document doc) {
		SwingUtils.invoke(() -> {
			// Select document tab.
			int tabCount = tabPane.getTabCount();

			for (int i = 0; i < tabCount; i++) {
				ThumbPanel thumbnailPanel = (ThumbPanel) tabPane.getComponentAt(i);

				if (thumbnailPanel.getDocument().getName().equals(doc.getName())) {
					// Reload if document has changed.
					if (!thumbnailPanel.getDocument().equals(doc)) {
						// Prevent tab switching for quiz reloading.
						thumbnailPanel.setDocument(doc, null);
					}

					tabPane.setSelectedIndex(i);
					break;
				}
			}

			// Set document outline.
			setOutline(doc.getDocumentOutline());
		});
	}

	@Override
	public Page getPage() {
		return slideView.getPage();
	}

	@Override
	public void setPage(Page page, PresentationParameter parameter) {
		SwingUtils.invoke(() -> {
			slideView.parameterChanged(page, parameter);
			slideView.setPage(page);

			// Select page on the thumbnail panel.
			ThumbPanel thumbPanel = (ThumbPanel) tabPane.getSelectedComponent();
			thumbPanel.selectPage(page);

			selectOutlineItem(page);
		});
	}

	@Override
	public void setPageRenderer(RenderController pageRenderer) {
		this.pageRenderer = pageRenderer;

		slideView.setPageRenderer(pageRenderer);
	}

	@Override
	public void setPageNotes(List<SlideNote> notes) {
		StringBuilder buffer = new StringBuilder();

		if (nonNull(notes)) {
			for (Iterator<SlideNote> notesIter = notes.iterator(); notesIter.hasNext();) {
				buffer.append(notesIter.next().getText());

				if (notesIter.hasNext()) {
					buffer.append("\n");
				}
			}

			// Show red highlight, if notes-view is hidden and page notes are available.
			if (bottomTabPane.getSelectedIndex() != 0 && !notes.isEmpty()) {
				bottomTabPane.setBackgroundAt(0, new Color(255, 182, 193));
			}
		}

		notesTextArea.setText(buffer.toString());
	}

	@Override
	public void setOutline(DocumentOutline outline) {
		SwingUtils.invoke(() -> {
			DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");

			for (var child : outline.getChildren()) {
				buildOutlineTree(child, root);
			}

			outlineTree.setModel(new DefaultTreeModel(root));
		});
	}

	@Override
	public void bindShowOutline(BooleanProperty showProperty) {
		SwingUtils.invoke(() -> showOutline(showProperty.get()));

		showProperty.addListener((observable, oldValue, newValue) -> {
			SwingUtils.invoke(() -> showOutline(showProperty.get()));
		});
	}

	@Override
	public void setExtendedFullscreen(boolean extended) {
		extendedFullscreen = extended;

		if (isNull(getParent())) {
			return;
		}

		JFrame window = (JFrame) SwingUtilities.getWindowAncestor(this);
		boolean show = !window.isUndecorated() || !extendedFullscreen;

		bottomTabPane.setVisible(show);
	}

	@Override
	public void setStylusHandler(StylusHandler handler) {
		StylusListener stylusListener = new StylusListener(handler, slideView);
		AwtStylusManager manager = AwtStylusManager.getInstance();
		manager.attachStylusListener(slideView, stylusListener);
	}

	@Override
	public void setLaTeXText(String text) {
		SwingUtils.invoke(() -> latexTextArea.setText(text));
	}

	@Override
	public void setMessengerState(ExecutableState state) {
		boolean started = state == ExecutableState.Started;

		SwingUtils.invoke(() -> {
			setBottomTabEnabled(2, started);
			setBottomTabSelected(2, started);
		});
	}

	@Override
	public void setMessengerMessage(MessengerMessage message) {
		SwingUtils.invoke(() -> {
			boolean nonNull = nonNull(message);

			messageView.setDate(nonNull ? message.getDate() : null);
			messageView.setHost(nonNull ? message.getRemoteAddress() : "");
			messageView.setMessage(nonNull ? message.getMessage().getText() : "");
		});
	}

	@Override
	public void setSelectedToolType(ToolType type) {
		SwingUtils.invoke(() -> {
			boolean isTeXTool = type == ToolType.LATEX;

			setBottomTabEnabled(1, isTeXTool);
			setBottomTabSelected(1, isTeXTool);
		});
	}

	@Override
	public void setOnKeyEvent(ConsumerAction<KeyEvent> action) {
		this.keyAction = action;
	}

	@Override
	public void setOnLaTeXText(ConsumerAction<String> action) {
		latexTextArea.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				executeAction(action, latexTextArea.getText());
			}
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
	public void setOnStartScreenCapture(Action action) {
		this.startScreenCaptureAction = action;
	}

	@Override
	public void setOnStopScreenCapture(Action action) {
		this.stopScreenCaptureAction = action;
	}

	@Override
	public void setOnOutlineItem(ConsumerAction<DocumentOutlineItem> action) {
		this.outlineAction = action;
	}

	@Override
	public void setScreenCaptureRecordingState(ExecutableState state) {
		SwingUtils.invoke(() -> {
			ThumbPanel thumbPanel = (ThumbPanel) tabPane.getSelectedComponent();
			if (thumbPanel instanceof ScreenCaptureThumbnailPanel) {
				ScreenCaptureThumbnailPanel screenCaptureThumbnailPanel = (ScreenCaptureThumbnailPanel) thumbPanel;
				screenCaptureThumbnailPanel.setRecordingState(state);
			}
		});
	}


	private void buildOutlineTree(DocumentOutlineItem item, DefaultMutableTreeNode root) {
		DefaultMutableTreeNode parent = new DefaultMutableTreeNode(item.getTitle());
		parent.setUserObject(item);

		root.add(parent);

		for (var child : item.getChildren()) {
			buildOutlineTree(child, parent);
		}
	}

	private void showOutline(boolean show) {
		outlinePane.setVisible(show);
		docSplitPane.setDividerLocation(0.2);
	}

	private void selectOutlineItem(Page page) {
		DocumentOutlineItem outlineItem = page.getDocument()
				.getDocumentOutline().getOutlineItem(page.getPageNumber());

		DefaultTreeModel model = (DefaultTreeModel) outlineTree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		DefaultMutableTreeNode node;

		Enumeration<TreeNode> e = root.breadthFirstEnumeration();

		while (e.hasMoreElements()) {
			node = (DefaultMutableTreeNode) e.nextElement();
			Object userObject = node.getUserObject();

			if (nonNull(userObject) && userObject.equals(outlineItem)) {
				TreePath path = new TreePath(node.getPath());
				Rectangle bounds = outlineTree.getPathBounds(path);

				outlineTree.setSelectionPath(path);

				if (nonNull(bounds)) {
					bounds.x = 0;
					//bounds.height = outlineTree.getVisibleRect().height / 2;

					outlineTree.scrollRectToVisible(bounds);
				}
				break;
			}
		}
	}

	private void setBottomTabEnabled(int index, boolean enable) {
		JLabel label = (JLabel) bottomTabPane.getTabComponentAt(index);
		label.setEnabled(enable);

		bottomTabPane.setEnabledAt(index, enable);
	}

	private void setBottomTabSelected(int index, boolean select) {
		if (select) {
			bottomTabPane.setSelectedIndex(index);
		}
		if (!select && bottomTabPane.getSelectedIndex() == index) {
			bottomTabPane.setSelectedIndex(-1);
		}
	}

	private void minimizeBottomPane() {
		int tabHeight = getBottomTabHeight();
		int location = notesSplitPane.getHeight() - notesSplitPane.getDividerSize() - tabHeight;

		notesSplitPane.setDividerLocation(location);
	}

	private void toggleBottomTab(int index) {
		if (index < 0 || index > bottomTabPane.getTabCount() - 1) {
			return;
		}
		if (!bottomTabPane.isEnabledAt(index)) {
			return;
		}

		boolean isSameTab = index == bottomTabIndex;

		if (isSameTab && bottomTabPane.getHeight() > getBottomTabHeight()) {
			minimizeBottomPane();
		}
		else if (isSameTab || bottomTabPane.getHeight() <= getBottomTabHeight()) {
			notesSplitPane.setDividerLocation(0.85);
		}

		bottomTabIndex = index;
	}

	private int getBottomTabHeight() {
		return bottomTabPane.getUI().getTabBounds(bottomTabPane, 0).height + 1;
	}

	@ViewPostConstruct
	private void initialize() {
		KeyboardFocusManager keyboardManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		keyboardManager.addKeyEventDispatcher(event -> {
			Component focusOwner = event.getComponent();

			if (nonNull(focusOwner) && focusOwner.isShowing()) {
				if (focusOwner instanceof JTextComponent) {
					return false;
				}
			}
			if (event.getID() == java.awt.event.KeyEvent.KEY_PRESSED) {
				executeAction(keyAction, KeyEventConverter.INSTANCE.from(event));
			}
			return false;
		});

		ToolTipManager.sharedInstance().registerComponent(outlineTree);

		// Use one-way tree selection.
		outlineTree.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				TreePath selPath = outlineTree
						.getPathForLocation(e.getX(), e.getY());

				if (isNull(selPath)) {
					return;
				}

				DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath
						.getLastPathComponent();

				if (isNull(node)) {
					// Nothing is selected.
					return;
				}

				Object userObject = node.getUserObject();

				if (nonNull(userObject)) {
					DocumentOutlineItem item = (DocumentOutlineItem) userObject;

					executeAction(outlineAction, item);
				}
			}
		});

		slideView.addPropertyChangeListener("transform", e -> {
			if (nonNull(viewTransformAction)) {
				viewTransformAction.execute(MatrixConverter.INSTANCE
						.from(slideView.getPageTransform()));
			}
		});

		tabPane.getModel().addChangeListener(e -> {
			ThumbPanel thumbPanel = (ThumbPanel) tabPane.getSelectedComponent();

			if (nonNull(thumbPanel)) {
				executeAction(selectDocumentAction, thumbPanel.getDocument());
			}
		});

		bottomTabPane.setMinimumSize(new Dimension(0, getBottomTabHeight()));
		bottomTabPane.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				int tabIndex = bottomTabPane.getUI().tabForCoordinate(
						bottomTabPane, e.getX(), e.getY());

				toggleBottomTab(tabIndex);
			}
		});

		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
				removeAncestorListener(this);

				minimizeBottomPane();
			}
		});
	}
}
