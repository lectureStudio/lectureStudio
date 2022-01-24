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

import dev.onvoid.webrtc.media.FourCC;
import dev.onvoid.webrtc.media.video.VideoBufferConverter;
import dev.onvoid.webrtc.media.video.VideoFrame;
import dev.onvoid.webrtc.media.video.VideoFrameBuffer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.math.BigInteger;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
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

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Matrix;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentOutline;
import org.lecturestudio.core.model.DocumentOutlineItem;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.SlideNote;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PageObjectView;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.presenter.api.stylus.StylusHandler;
import org.lecturestudio.presenter.api.view.SlidesView;
import org.lecturestudio.presenter.swing.input.StylusListener;
import org.lecturestudio.stylus.awt.AwtStylusManager;
import org.lecturestudio.swing.components.*;
import org.lecturestudio.swing.converter.KeyEventConverter;
import org.lecturestudio.swing.converter.MatrixConverter;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;
import org.lecturestudio.web.api.event.PeerStateEvent;
import org.lecturestudio.web.api.event.VideoFrameEvent;
import org.lecturestudio.web.api.message.CourseParticipantMessage;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.SpeechCancelMessage;
import org.lecturestudio.web.api.message.SpeechRequestMessage;
import org.scilab.forge.jlatexmath.ParseException;

@SwingView(name = "main-slides")
public class SwingSlidesView extends JPanel implements SlidesView {

	private final Dictionary dict;

	private ExecutableState streamState = ExecutableState.Stopped;

	private ExecutableState messengerState = ExecutableState.Stopped;

	private ConsumerAction<org.lecturestudio.core.input.KeyEvent> keyAction;

	private ConsumerAction<Document> selectDocumentAction;

	private ConsumerAction<DocumentOutlineItem> outlineAction;

	private ConsumerAction<Page> selectPageAction;

	private ConsumerAction<Matrix> viewTransformAction;

	private ConsumerAction<MessengerMessage> discardMessageAction;

	private ConsumerAction<SpeechRequestMessage> acceptSpeechRequestAction;

	private ConsumerAction<SpeechRequestMessage> rejectSpeechRequestAction;

	private ConsumerAction<Boolean> mutePeerAudioAction;

	private ConsumerAction<Boolean> mutePeerVideoAction;

	private ConsumerAction<BigInteger> stopPeerConnectionAction;

	private Action newPageAction;

	private Action deletePageAction;

	private Action shareQuizAction;

	private Action stopQuizAction;

	private double notesDividerPosition;

	private boolean extendedFullscreen;

	private RenderController pageRenderer;

	private JSplitPane tabSplitPane;

	private JSplitPane notesSplitPane;

	private JSplitPane docSplitPane;

	private JScrollPane outlinePane;

	private JTree outlineTree;

	private SlideView slideView;

	private StylusListener stylusListener;

	private BufferedImage peerViewImage;

	private PeerView peerView;

	private JTabbedPane tabPane;

	private Container peerViewContainer;

	private Container messageViewContainer;

	private Container messageSendContainer;

	private Container messageContainer;

	private Container participantViewContainer;

	private JTabbedPane bottomTabPane;

	private JTextArea notesTextArea;

	private JTextArea latexTextArea;

	private JTextField sendTextField;

	private JButton sendMessengerMessageButton;

	private int bottomTabIndex;


	@Inject
	SwingSlidesView(Dictionary dictionary) {
		super();

		this.dict = dictionary;
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
			// Create a ThumbnailPanel for each document.
			ThumbnailPanel thumbPanel;

			if (doc.isWhiteboard()) {
				WhiteboardThumbnailPanel wbThumbPanel = new WhiteboardThumbnailPanel(dict);
				wbThumbPanel.setOnAddPage(newPageAction);
				wbThumbPanel.setOnRemovePage(deletePageAction);

				thumbPanel = wbThumbPanel;
			}
			else if (doc.isQuiz()) {
				QuizThumbnailPanel quizThumbPanel = new QuizThumbnailPanel(dict);
				quizThumbPanel.setStreamState(streamState);
				quizThumbPanel.setOnShareQuiz(shareQuizAction);
				quizThumbPanel.setOnStopQuiz(stopQuizAction);

				thumbPanel = quizThumbPanel;
			}
			else {
				thumbPanel = new ThumbnailPanel();
			}

			thumbPanel.setRenderController(pageRenderer);
			thumbPanel.setDocument(doc, ppProvider);
			thumbPanel.addSelectedSlideChangedListener(event -> {
				if (event.getNewValue() instanceof Page) {
					Page page = (Page) event.getNewValue();

					executeAction(selectPageAction, page);
				}
			});

			if (doc.isWhiteboard()) {
				JButton addPageButton = new JButton("+");
				JButton deletePageButton = new JButton("-");

				SwingUtils.bindAction(addPageButton, () -> {
					executeAction(newPageAction);
				});
				SwingUtils.bindAction(deletePageButton, () -> {
					executeAction(deletePageAction);
				});

				thumbPanel.addButton(addPageButton);
				thumbPanel.addButton(deletePageButton);
			}
			if (doc.isQuiz()) {
				JButton shareQuizButton = new JButton(dict.get("slides.share.quiz"));

				SwingUtils.bindAction(shareQuizButton, () -> {
					executeAction(shareQuizAction);
				});

				thumbPanel.addButton(shareQuizButton);
			}

			VerticalTab tab = new VerticalTab(tabPane.getTabPlacement());
			tab.setText(doc.getName());

			tabPane.addTab(null, thumbPanel);
			tabPane.setTabComponentAt(tabPane.getTabCount() - 1, tab);
		});
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
	public void selectDocument(Document doc, PresentationParameterProvider ppProvider) {
		SwingUtils.invoke(() -> {
			// Select document tab.
			int tabCount = tabPane.getTabCount();

			for (int i = 0; i < tabCount; i++) {
				ThumbPanel thumbnailPanel = (ThumbPanel) tabPane.getComponentAt(i);

				if (thumbnailPanel.getDocument().getName().equals(doc.getName())) {
					// Reload if document has changed.
					if (!thumbnailPanel.getDocument().equals(doc)) {
						// Prevent tab switching for quiz reloading.
						thumbnailPanel.setDocument(doc, ppProvider);
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
		SwingUtils.invoke(() -> {
			showOutline(showProperty.get());
		});

		showProperty.addListener((observable, oldValue, newValue) -> {
			SwingUtils.invoke(() -> {
				showOutline(showProperty.get());
			});
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
		stylusListener = new StylusListener(handler, slideView);

		AwtStylusManager manager = AwtStylusManager.getInstance();
		manager.attachStylusListener(slideView, stylusListener);
	}

	@Override
	public void setLaTeXText(String text) {
		SwingUtils.invoke(() -> latexTextArea.setText(text));
	}

	@Override
	public void setQuizState(ExecutableState state) {
		for (int i = 0; i < tabPane.getTabCount(); i++) {
			ThumbPanel thumbnailPanel = (ThumbPanel) tabPane.getComponentAt(i);

			if (thumbnailPanel instanceof QuizThumbnailPanel) {
				QuizThumbnailPanel quizPanel = (QuizThumbnailPanel) thumbnailPanel;
				quizPanel.setQuizState(state);
				break;
			}
		}
	}

	@Override
	public void setStreamState(ExecutableState state) {
		streamState = state;

		boolean streamStarted = streamState == ExecutableState.Started;
		boolean messengerStarted = messengerState == ExecutableState.Started;

		SwingUtils.invoke(() -> {
			if (state == ExecutableState.Stopped && !messengerStarted) {
				minimizeBottomPane();
			}

			setBottomTabEnabled(2, streamStarted || messengerStarted);
			setBottomTabSelected(2, streamStarted || messengerStarted);

			if (!streamStarted) {
				removeMessageViews(SpeechRequestView.class);
				removeParticipantsView(ParticipantsView.class);
			}

			for (int i = 0; i < tabPane.getTabCount(); i++) {
				ThumbPanel thumbnailPanel = (ThumbPanel) tabPane.getComponentAt(i);

				if (thumbnailPanel instanceof QuizThumbnailPanel) {
					QuizThumbnailPanel quizPanel = (QuizThumbnailPanel) thumbnailPanel;
					quizPanel.setStreamState(state);
					break;
				}
			}
		});
	}

	@Override
	public void setMessengerState(ExecutableState state) {
		messengerState = state;

		boolean streamStarted = streamState == ExecutableState.Started;
		boolean messengerStarted = messengerState == ExecutableState.Started;

		SwingUtils.invoke(() -> {
			if (state == ExecutableState.Stopped && !streamStarted) {
				minimizeBottomPane();
			}

			setBottomTabEnabled(2, streamStarted || messengerStarted);
			setBottomTabSelected(2, streamStarted || messengerStarted);
			setSendMessengerMessageButtonEnabled(messengerStarted);

			if (!messengerStarted) {
				removeMessageViews(MessageView.class);
			}
		});
	}

	@Override
	public void setMessengerMessage(MessengerMessage message) {
		SwingUtils.invoke(() -> {
			MessageView messageView = new MessageView(this.dict);
			messageView.setUserName(String.format("%s %s", message.getFirstName(), message.getFamilyName()));
			messageView.setDate(message.getDate());
			messageView.setMessage(message.getMessage().getText());
			messageView.setOnDiscard(() -> {
				discardMessageAction.execute(message);

				removeMessageView(messageView);
			});
			messageView.pack();

			messageViewContainer.add(messageView);
			messageViewContainer.revalidate();
		});
	}

	@Override
	public void setParticipantMessage(CourseParticipantMessage message) {
			SwingUtils.invoke(() -> {
				ParticipantsView participantsView = new ParticipantsView(this.dict, message.getUsername());
				participantsView.setParticipantNameLabel(String.format("%s %s", message.getFirstName(), message.getFamilyName()));
				participantsView.pack();
				if (message.getConnected()) {
					participantViewContainer.add(participantsView);
					participantViewContainer.revalidate();
				}
				else {
					removeParticipantMessageView(participantsView);
				}
			});
	}

	@Override
	public void setSpeechRequestMessage(SpeechRequestMessage message) {
		SwingUtils.invoke(() -> {
			SpeechRequestView requestView = new SpeechRequestView(this.dict);
			requestView.setRequestId(message.getRequestId());
			requestView.setUserName(String.format("%s %s", message.getFirstName(), message.getFamilyName()));
			requestView.setDate(message.getDate());
			requestView.setOnAccept(() -> {
				acceptSpeechRequestAction.execute(message);

				removeMessageView(requestView);
			});
			requestView.setOnReject(() -> {
				rejectSpeechRequestAction.execute(message);

				removeMessageView(requestView);
			});
			requestView.pack();

			messageViewContainer.add(requestView);
			messageViewContainer.revalidate();
		});
	}

	@Override
	public void setSpeechCancelMessage(SpeechCancelMessage message) {
		SwingUtils.invoke(() -> {
			for (Component c : messageViewContainer.getComponents()) {
				if (c instanceof SpeechRequestView) {
					SpeechRequestView view = (SpeechRequestView) c;

					if (view.getRequestId() == message.getRequestId()) {
						view.setCanceled();

						removeMessageView(view);
						break;
					}
				}
			}
		});
	}

	@Override
	public void setOnDiscardMessage(ConsumerAction<MessengerMessage> action) {
		discardMessageAction = action;
	}

	@Override
	public void setOnAcceptSpeech(ConsumerAction<SpeechRequestMessage> action) {
		acceptSpeechRequestAction = action;
	}

	@Override
	public void setOnRejectSpeech(ConsumerAction<SpeechRequestMessage> action) {
		rejectSpeechRequestAction = action;
	}

	@Override
	public void setPeerStateEvent(PeerStateEvent event) {
		SwingUtils.invoke(() -> {
			ExecutableState state = event.getState();

			if (state == ExecutableState.Started) {
				if (peerViewContainer.getComponentCount() > 0) {
					return;
				}

				peerView = new PeerView();
				peerView.setMinimumSize(new Dimension(100, 150));
				peerView.setPreferredSize(new Dimension(100, 150));
				peerView.setPeerId(event.getPeerId());
				peerView.setPeerName(event.getPeerName());
				peerView.setOnMuteAudio(mutePeerAudioAction);
				peerView.setOnMuteVideo(mutePeerVideoAction);
				peerView.setOnStopPeerConnection(stopPeerConnectionAction);

				peerViewContainer.add(peerView);
				peerViewContainer.revalidate();
				peerViewContainer.repaint();
			}
			else if (state == ExecutableState.Stopped) {
				peerView = null;

				peerViewContainer.removeAll();
				peerViewContainer.revalidate();
				peerViewContainer.repaint();
			}
		});
	}

	@Override
	public void setOnMutePeerAudio(ConsumerAction<Boolean> action) {
		mutePeerAudioAction = action;
	}

	@Override
	public void setOnMutePeerVideo(ConsumerAction<Boolean> action) {
		mutePeerVideoAction = action;
	}

	@Override
	public void setOnStopPeerConnection(ConsumerAction<BigInteger> action) {
		stopPeerConnectionAction = action;
	}

	@Override
	public void setVideoFrameEvent(VideoFrameEvent event) {
		if (isNull(peerView)) {
			return;
		}

		try {
			peerViewImage = convertVideoFrame(event.getFrame(), peerViewImage);
		}
		catch (Exception e) {
			return;
		}

		SwingUtils.invoke(() -> {
			peerView.showImage(peerViewImage);
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
				try {
					executeAction(action, latexTextArea.getText());
					latexTextArea.setBackground(Color.decode("#D1FAE5"));
				} catch (ParseException exception) {
					executeAction(action, "");
					latexTextArea.setBackground(Color.decode("#FEE2E2"));
				}
				if (Objects.equals(latexTextArea.getText(), "")) {
					latexTextArea.setBackground(Color.WHITE);
				}
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
	public void setOnShareQuiz(Action action) {
		this.shareQuizAction = action;
	}

	@Override
	public void setOnStopQuiz(Action action) {
		this.stopQuizAction = action;
	}

	@Override
	public void setOnOutlineItem(ConsumerAction<DocumentOutlineItem> action) {
		this.outlineAction = action;
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
			notesSplitPane.setDividerLocation(0.7);
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
				JFrame window = (JFrame) SwingUtilities.getWindowAncestor(SwingSlidesView.this);
				window.addComponentListener(new ComponentAdapter() {

					@Override
					public void componentShown(ComponentEvent e) {
						AwtStylusManager manager = AwtStylusManager.getInstance();
						manager.attachStylusListener(slideView, stylusListener);
					}
				});
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

	private void removeMessageView(Component view) {
		for (Component c : messageViewContainer.getComponents()) {
			if (c == view) {
				messageViewContainer.remove(view);
				messageViewContainer.validate();
				messageViewContainer.repaint();
				break;
			}
		}
	}


	private void removeParticipantMessageView(Component view) {
		for (Component c : participantViewContainer.getComponents()) {
			ParticipantsView consideredView = null;
			if (c instanceof ParticipantsView) {
				consideredView = (ParticipantsView) c;
			}
			if (consideredView != null && consideredView.equals(view)) {
				participantViewContainer.remove(c);
				participantViewContainer.validate();
				participantViewContainer.repaint();
				break;
			}
		}
	}

	private void removeMessageViews(Class<? extends MessagePanel> cls) {
		for (Component c : messageViewContainer.getComponents()) {
			if (cls.isAssignableFrom(c.getClass())) {
				messageViewContainer.remove(c);
			}
		}

		messageViewContainer.validate();
		messageViewContainer.repaint();
	}

	private void removeParticipantsView(Class<? extends ParticipantsPanel> cls) {
		for (Component c : participantViewContainer.getComponents()) {
			if (cls.isAssignableFrom(cls)) {
				participantViewContainer.remove(c);
			}
		}

		participantViewContainer.validate();
		participantViewContainer.repaint();
	}

	private BufferedImage convertVideoFrame(VideoFrame videoFrame, BufferedImage image) throws Exception {
		VideoFrameBuffer buffer = videoFrame.buffer;
		int width = buffer.getWidth();
		int height = buffer.getHeight();

		// Scale video frame down to the view size.
		int viewHeight = peerView.getHeight();
		double scale = viewHeight / (double) height;

		buffer = buffer.cropAndScale(0, 0, width, height, (int) (width * scale), viewHeight);
		width = buffer.getWidth();
		height = buffer.getHeight();

		if (isNull(image) || image.getWidth() != width || image.getHeight() != height) {
			image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		}

		byte[] imageBuffer = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

		VideoBufferConverter.convertFromI420(buffer, imageBuffer, FourCC.RGBA);

		// Release resources.
		buffer.release();

		return image;
	}

	@Override
	public void setMessageSendContainerMaxHeight(int height) {
		this.messageSendContainer.setMaximumSize(new Dimension(this.messageSendContainer.getPreferredSize().width, height));
	}

	@Override
	public void setMessageToSend(StringProperty messageValue) {
		SwingUtils.bindBidirectional(this.sendTextField, messageValue);
	}

	@Override
	public void setOnSend(Action action) {
		SwingUtils.bindAction(this.sendMessengerMessageButton, action);
	}

	@Override
	public void setSendMessengerMessageButtonEnabled(boolean enable) {
		this.sendMessengerMessageButton.setEnabled(enable);
	}
}
