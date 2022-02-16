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

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.Objects;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Matrix;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentOutline;
import org.lecturestudio.core.model.DocumentOutlineItem;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.SlideNote;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.view.*;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.model.MessageBarPosition;
import org.lecturestudio.presenter.api.model.MessageTabData;
import org.lecturestudio.presenter.api.model.SlidesTabData;
import org.lecturestudio.presenter.api.model.TabData;
import org.lecturestudio.swing.model.ExternalWindowPosition;
import org.lecturestudio.presenter.api.stylus.StylusHandler;
import org.lecturestudio.presenter.api.view.SlidesView;
import org.lecturestudio.presenter.swing.input.StylusListener;
import org.lecturestudio.stylus.awt.AwtStylusManager;
import org.lecturestudio.swing.components.*;
import org.lecturestudio.swing.components.SlideView;
import org.lecturestudio.swing.converter.KeyEventConverter;
import org.lecturestudio.swing.converter.MatrixConverter;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;
import org.lecturestudio.web.api.event.PeerStateEvent;
import org.lecturestudio.web.api.event.VideoFrameEvent;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.SpeechCancelMessage;
import org.lecturestudio.web.api.message.SpeechRequestMessage;

@SwingView(name = "main-slides")
public class SwingSlidesView extends JPanel implements SlidesView {
	private static final String MESSAGE_LABEL_KEY = "slides.message";

	private static final String NO_MESSAGES_LABEL_KEY = "slides.no.messages";

	private static final String SLIDES_PREVIEW_LABEL_KEY = "slides.slide.preview";

	private static final String SPEECH_LABEL_KEY = "slides.speech";

	private static final String CURRENTLY_NO_SPEECH_LABEL_KEY = "slides.currently.no.speech";

	private static final int TAB_OFFSET = 3;

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

	private ConsumerAction<Long> stopPeerConnectionAction;

	private ConsumerAction<ExternalWindowPosition> externalMessagesPositionChangedAction;

	private ConsumerAction<Dimension> externalMessagesSizeChangedAction;

	private Action externalMessagesClosedAction;

	private ConsumerAction<ExternalWindowPosition> externalSlidePreviewPositionChangedAction;

	private ConsumerAction<Dimension> externalSlidePreviewSizeChangedAction;

	private Action externalSlidePreviewClosedAction;

	private ConsumerAction<ExternalWindowPosition> externalSpeechPositionChangedAction;

	private ConsumerAction<Dimension> externalSpeechSizeChangedAction;

	private Action externalSpeechClosedAction;

	private Action newPageAction;

	private Action deletePageAction;

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

	private Box rightVbox;

	private JTabbedPane rightTabPane;

	private Container peerViewContainer;

	private SettingsTab messageTab;

	private JScrollPane messagesPane;

	private Box messageViewContainer;

	private JTabbedPane bottomTabPane;

	private JPanel messagesPanel;

	private final JTabbedPane leftTabPane = new JTabbedPane(SwingConstants.LEFT);

	//	private JTextArea notesTextArea;

	//	private JTextArea latexTextArea;

	private ExternalFrame externalMessagesFrame;

	private ExternalFrame externalSlidePreviewFrame;

	private ExternalFrame externalSpeechFrame;

	private final JScrollPane externalMessagesPane = new JScrollPane();

	private int leftTabPrevSelectedIndex;

	private int rightTabPrevSelectedIndex;

	private int bottomTabPrevSelectedIndex;

	private double oldDocSplitPaneDividerRatio = 0.15;

	private double oldNotesSplitPaneDividerRatio = 0.75;

	private double oldTabSplitPaneDividerRatio = 0.9;

	private boolean currentSpeech = false;

	private JLabel messagesPlaceholder;

	private final JTabbedPane externalSlidePreviewTabPane = new JTabbedPane(SwingConstants.RIGHT);

	private final ArrayList<TabData> leftTabs = new ArrayList<>();

	private final ArrayList<TabData> bottomTabs = new ArrayList<>();

	private final ArrayList<TabData> rightTabs = new ArrayList<>();

	private final ArrayList<TabData> externalSlideTabs = new ArrayList<>();

	private MessageBarPosition messageBarPosition = MessageBarPosition.BOTTOM;

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
			final JTabbedPane slidesTabPane = getSlidesTabPane();

			// Select document tab.
			int tabCount = slidesTabPane.getTabCount();

			for (int i = 0; i < tabCount; i++) {
				final Component tabComponent = slidesTabPane.getComponentAt(i);
				if (!(tabComponent instanceof ThumbPanel)) {
					continue;
				}

				ThumbPanel thumbnailPanel = (ThumbPanel) tabComponent;

				if (thumbnailPanel.getDocument().getName().equals(doc.getName())) {
					// Reload if document has changed.
					if (!thumbnailPanel.getDocument().equals(doc)) {
						// Prevent tab switching for quiz reloading.
						thumbnailPanel.setDocument(doc, ppProvider);
					}
					return;
				}
			}

			// Create a ThumbnailPanel for each document.
			ThumbnailPanel thumbPanel;

			if (doc.isWhiteboard()) {
				WhiteboardThumbnailPanel wbThumbPanel = new WhiteboardThumbnailPanel(dict);
				wbThumbPanel.setOnAddPage(newPageAction);
				wbThumbPanel.setOnRemovePage(deletePageAction);

				thumbPanel = wbThumbPanel;
			} else if (doc.isQuiz()) {
				QuizThumbnailPanel quizThumbPanel = new QuizThumbnailPanel(dict);
				quizThumbPanel.setOnStopQuiz(stopQuizAction);

				thumbPanel = quizThumbPanel;
			} else {
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

			VerticalTab tab = VerticalTab.fromText(doc.getName(), slidesTabPane.getTabPlacement());
			addTabToSlidesPane(thumbPanel, tab, this::updateSlidesTabs);
		});
	}

	private void addTabToSlidesPane(Component component, JLabel tabComponent, Runnable updateTabsFunc) {
		final ArrayList<TabData> slidesTabs = getSlidesTabs();

		final int index = IntStream.range(0, slidesTabs.size()).filter(i -> slidesTabs.get(i) instanceof MessageTabData)
				.findFirst().orElse(slidesTabs.size());

		slidesTabs.add(index, new SlidesTabData(component, tabComponent, true, true));

		updateTabsFunc.run();
	}

	@Override
	public void removeDocument(Document doc) {
		final ArrayList<TabData> slidesTabs = getSlidesTabs();

		// Remove document tab.
		for (final TabData tab : slidesTabs) {
			if (!(tab.component instanceof ThumbPanel)) {
				continue;
			}

			ThumbPanel thumbnailPanel = (ThumbPanel) tab.component;
			if (thumbnailPanel.getDocument().equals(doc)) {
				slidesTabs.remove(tab);
				updateSlidesTabs();
				break;
			}
		}
	}

	private void checkIfThumbSelected() {
		final Component selectedComponent = getSlidesTabPane().getSelectedComponent();

		if (!(selectedComponent instanceof ThumbPanel)) {
			return;
		}

		ThumbPanel thumbPanel = (ThumbPanel) selectedComponent;

		executeAction(selectDocumentAction, thumbPanel.getDocument());
	}

	@Override
	public void selectDocument(Document doc, PresentationParameterProvider ppProvider) {
		SwingUtils.invoke(() -> {
			final JTabbedPane slidesTabPane = getSlidesTabPane();

			// Select document tab.
			int tabCount = slidesTabPane.getTabCount();

			for (int i = 0; i < tabCount; i++) {
				final Component tabComponent = slidesTabPane.getComponentAt(i);
				if (!(tabComponent instanceof ThumbPanel)) {
					continue;
				}

				ThumbPanel thumbnailPanel = (ThumbPanel) tabComponent;

				if (thumbnailPanel.getDocument().getName().equals(doc.getName())) {
					// Reload if document has changed.
					if (!thumbnailPanel.getDocument().equals(doc)) {
						// Prevent tab switching for quiz reloading.
						thumbnailPanel.setDocument(doc, ppProvider);
					}
					slidesTabPane.setSelectedIndex(i);
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
			final Component selectedComponent = getSlidesTabPane().getSelectedComponent();
			if (selectedComponent instanceof ThumbPanel) {
				ThumbPanel thumbPanel = (ThumbPanel) selectedComponent;
				thumbPanel.selectPage(page);

				selectOutlineItem(page);
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
		StringBuilder buffer = new StringBuilder();

		if (nonNull(notes)) {
			for (Iterator<SlideNote> notesIter = notes.iterator(); notesIter.hasNext(); ) {
				buffer.append(notesIter.next().getText());

				if (notesIter.hasNext()) {
					buffer.append("\n");
				}
			}

			// Show red highlight, if notes-view is hidden and page notes are available.
			final int notesIndex = getTabIndex(bottomTabPane, dict.get("slides.notes"));
			if (bottomTabPane.getSelectedIndex() != notesIndex && !notes.isEmpty()) {
				bottomTabPane.setBackgroundAt(notesIndex, new Color(255, 182, 193));
			}
		}

		//notesTextArea.setText(buffer.toString());
	}

	private TabData getTabByLabel(ArrayList<TabData> tabs, String labelText) {
		return tabs.stream().filter(tab -> tab.getTabComponent().getText().equals(labelText)).findFirst().orElse(null);
	}

	private int getTabIndex(JTabbedPane pane, String labelText) {
		int index = -1;

		for (int i = 0; i < pane.getTabCount(); i++) {
			JLabel label = (JLabel) pane.getTabComponentAt(i);
			if (label.getText().equals(labelText)) {
				index = i;
				break;
			}
		}

		return index;
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
		//SwingUtils.invoke(() -> latexTextArea.setText(text));
	}

	@Override
	public void setQuizState(ExecutableState state) {
		for (int i = 0; i < getSlidesTabPane().getTabCount(); i++) {
			final Component tabComponent = getSlidesTabPane().getComponentAt(i);
			if (!(tabComponent instanceof QuizThumbnailPanel)) {
				continue;
			}

			QuizThumbnailPanel quizPanel = (QuizThumbnailPanel) getSlidesTabPane().getComponentAt(i);
			quizPanel.setQuizState(state);
			break;

		}
	}

	@Override
	public void setStreamState(ExecutableState state) {
		streamState = state;

		boolean streamStarted = streamState == ExecutableState.Started;
		boolean messengerStarted = messengerState == ExecutableState.Started;

		SwingUtils.invoke(() -> {
			if (state == ExecutableState.Stopped && !messengerStarted) {
				minimizeBottomTabPane();
			}

			final boolean streamOrMessengerStarted = streamStarted || messengerStarted;
			setMessageBarTabEnabled(dict.get(MESSAGE_LABEL_KEY), streamOrMessengerStarted);
			setMessageBarTabSelected(dict.get(MESSAGE_LABEL_KEY), streamOrMessengerStarted);

			if (!streamStarted) {
				removeMessageViews(SpeechRequestView.class);
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
				minimizeBottomTabPane();
			}

			final boolean streamOrMessengerStarted = streamStarted || messengerStarted;
			setMessageBarTabEnabled(dict.get(MESSAGE_LABEL_KEY), streamOrMessengerStarted);
			setMessageBarTabSelected(dict.get(MESSAGE_LABEL_KEY), streamOrMessengerStarted);

			if (!streamStarted && !messengerStarted) {
				showMessagesPlaceholder();
			}

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
				executeAction(discardMessageAction, message);

				removeMessageView(messageView);
			});
			messageView.pack();

			messageViewContainer.add(messageView);
			messageViewContainer.revalidate();
			messageViewContainer.repaint();
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
				executeAction(acceptSpeechRequestAction, message);

				removeMessageView(requestView);
			});
			requestView.setOnReject(() -> {
				executeAction(rejectSpeechRequestAction, message);

				removeMessageView(requestView);
			});
			requestView.pack();

			messageViewContainer.add(requestView);
			messageViewContainer.revalidate();
			messageViewContainer.repaint();
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

			removePeerView(message.getRequestId());
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

			if (state == ExecutableState.Starting) {
				peerView = new PeerView(dict);
				peerView.setMinimumSize(new Dimension(100, 150));
				peerView.setPreferredSize(new Dimension(100, 150));
				peerViewContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
				peerView.setState(state);
				peerView.setRequestId(event.getRequestId());
				peerView.setPeerName(event.getPeerName());
				peerView.setOnMuteAudio(mutePeerAudioAction);
				peerView.setOnMuteVideo(mutePeerVideoAction);
				peerView.setOnStopPeerConnection(stopPeerConnectionAction);

				peerViewContainer.setVisible(true);
				peerViewContainer.removeAll();
				peerViewContainer.add(peerView);
				peerViewContainer.revalidate();
				peerViewContainer.repaint();

				currentSpeech = true;

				externalSpeechFrame.showBody();

				if (getNumberOfVisibleTabs(rightTabPane) == 0 && !externalSpeechFrame.isVisible()) {
					maximizeRightTabPane();
				}
			}
			else if (state == ExecutableState.Started) {
				for (var component : peerViewContainer.getComponents()) {
					if (component instanceof PeerView) {
						PeerView peerView = (PeerView) component;

						if (Objects.equals(peerView.getRequestId(), event.getRequestId())) {
							peerView.setState(state);
							peerView.setHasVideo(event.hasVideo());
						}
					}
				}
			}
			else if (state == ExecutableState.Stopped) {
				removePeerView(event.getRequestId());
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
	public void setOnStopPeerConnection(ConsumerAction<Long> action) {
		stopPeerConnectionAction = action;
	}

	@Override
	public void setVideoFrameEvent(VideoFrameEvent event) {
		if (isNull(peerView)) {
			return;
		}

		try {
			peerViewImage = convertVideoFrame(event.getFrame(), peerViewImage);
		} catch (Exception e) {
			return;
		}

		SwingUtils.invoke(() -> {
			if (nonNull(peerView)) {
				peerView.showImage(peerViewImage);
			}
		});
	}

	@Override
	public void setSelectedToolType(ToolType type) {
		//SwingUtils.invoke(() -> {
		//			boolean isTeXTool = type == ToolType.LATEX;
//
		//			setBottomTabEnabled(0, isTeXTool);
		//			setBottomTabSelected(0, isTeXTool);
		//});
	}

	@Override
	public void setOnKeyEvent(ConsumerAction<KeyEvent> action) {
		keyAction = action;
	}

	@Override
	public void setOnLaTeXText(ConsumerAction<String> action) {
		//		latexTextArea.getDocument().addDocumentListener(new DocumentListener() {
//
		//@Override
		//public void insertUpdate(DocumentEvent e) {
		//changedUpdate(e);
		//			}
//
		//@Override
		//public void removeUpdate(DocumentEvent e) {
		//changedUpdate(e);
		//			}
//
		//@Override
		//public void changedUpdate(DocumentEvent e) {
		//try {
		//executeAction(action, latexTextArea.getText());
		//latexTextArea.setBackground(Color.decode("#D1FAE5"));
		//} catch (ParseException exception) {
		//executeAction(action, "");
		//latexTextArea.setBackground(Color.decode("#FEE2E2"));
		//}
		//if (Objects.equals(latexTextArea.getText(), "")) {
		//latexTextArea.setBackground(Color.WHITE);
		//}
		//}
		//});
	}

	@Override
	public void setOnSelectDocument(ConsumerAction<Document> action) {
		selectDocumentAction = action;
	}

	@Override
	public void setOnSelectPage(ConsumerAction<Page> action) {
		selectPageAction = action;
	}

	@Override
	public void setOnViewTransform(ConsumerAction<Matrix> action) {
		viewTransformAction = action;
	}

	@Override
	public void setOnNewPage(Action action) {
		newPageAction = action;
	}

	@Override
	public void setOnDeletePage(Action action) {
		deletePageAction = action;
	}

	@Override
	public void setOnStopQuiz(Action action) {
		stopQuizAction = action;
	}

	@Override
	public void setOnOutlineItem(ConsumerAction<DocumentOutlineItem> action) {
		outlineAction = action;
	}

	@Override
	public void setOnExternalMessagesPositionChanged(ConsumerAction<ExternalWindowPosition> action) {
		this.externalMessagesPositionChangedAction = action;
	}

	@Override
	public void setOnExternalMessagesSizeChanged(ConsumerAction<Dimension> action) {
		this.externalMessagesSizeChangedAction = action;
	}

	@Override
	public void setOnExternalMessagesClosed(Action action) {
		externalMessagesClosedAction = action;
	}

	@Override
	public void setOnExternalSlidePreviewPositionChanged(ConsumerAction<ExternalWindowPosition> action) {
		this.externalSlidePreviewPositionChangedAction = action;
	}

	@Override
	public void setOnExternalSlidePreviewSizeChanged(ConsumerAction<Dimension> action) {
		this.externalSlidePreviewSizeChangedAction = action;
	}

	@Override
	public void setOnExternalSlidePreviewClosed(Action action) {
		externalSlidePreviewClosedAction = action;
	}

	@Override
	public void setOnExternalSpeechPositionChanged(ConsumerAction<ExternalWindowPosition> action) {
		this.externalSpeechPositionChangedAction = action;
	}

	@Override
	public void setOnExternalSpeechSizeChanged(ConsumerAction<Dimension> action) {
		this.externalSpeechSizeChangedAction = action;
	}

	@Override
	public void setOnExternalSpeechClosed(Action action) {
		externalSpeechClosedAction = action;
	}

	@Override
	public void showExternalMessages(Screen screen, Point position, Dimension size) {
		if (externalMessagesFrame.isVisible()) {
			return;
		}

		setMessageBarTabVisible(dict.get(MESSAGE_LABEL_KEY), false);

		messagesPane.getViewport().remove(messageViewContainer);

		externalMessagesPane.getViewport().add(messageViewContainer);

		externalMessagesFrame.updatePosition(screen, position, size);
		externalMessagesFrame.setVisible(true);
	}

	@Override
	public void hideExternalMessages() {
		if (!externalMessagesFrame.isVisible()) {
			return;
		}

		externalMessagesPane.getViewport().remove(messageViewContainer);

		externalMessagesFrame.setVisible(false);

		messagesPane.getViewport().add(messageViewContainer);

		setMessageBarTabVisible(dict.get(MESSAGE_LABEL_KEY), true);
	}

	@Override
	public void showExternalSlidePreview(Screen screen, Point position, Dimension size) {
		if (externalSlidePreviewFrame.isVisible()) {
			return;
		}

		externalSlidePreviewFrame.updatePosition(screen, position, size);
		externalSlidePreviewFrame.showBody();
		externalSlidePreviewFrame.setVisible(true);

		moveSlideTabsToExternalTabPane();
	}

	@Override
	public void hideExternalSlidePreview() {
		if (!externalSlidePreviewFrame.isVisible()) {
			return;
		}

		externalSlidePreviewFrame.hideBody();
		externalSlidePreviewFrame.setVisible(false);

		moveSlideTabsToRightTabPane();
	}

	private ArrayList<TabData> getSlidesTabs() {
		return externalSlidePreviewFrame.isVisible() ? externalSlideTabs : rightTabs;
	}

	private JTabbedPane getSlidesTabPane() {
		return externalSlidePreviewFrame.isVisible() ? externalSlidePreviewTabPane : rightTabPane;
	}

	private void moveSlideTabsToExternalTabPane() {
		moveSlidesTabsFromTo(rightTabs, externalSlideTabs);
		updateRightTabs();
		updateExternalSlideTabs();
	}

	private void moveSlideTabsToRightTabPane() {
		moveSlidesTabsFromTo(externalSlideTabs, rightTabs);
		updateExternalSlideTabs();
		updateRightTabs();
	}

	private void moveSlidesTabsFromTo(ArrayList<TabData> from, ArrayList<TabData> to) {
		final ArrayList<TabData> tabsToAdd = new ArrayList<>();

		final Iterator<TabData> iter = from.iterator();

		while (iter.hasNext()) {
			final TabData tab = iter.next();

			if (!(tab instanceof SlidesTabData)) {
				continue;
			}

			tabsToAdd.add(tab);
			iter.remove();
		}

		final int index =
				IntStream.range(0, to.size()).filter(i -> to.get(i) instanceof MessageTabData).findFirst()
						.orElse(to.size());

		to.addAll(index, tabsToAdd);
	}

	@Override
	public void showExternalSpeech(Screen screen, Point position, Dimension size) {
		if (externalSpeechFrame.isVisible()) {
			return;
		}

		removeComponentAndUpdate(rightVbox, peerViewContainer);

		peerViewContainer.setVisible(true);

		externalSpeechFrame.updatePosition(screen, position, size);
		externalSpeechFrame.setVisible(true);
	}

	@Override
	public void hideExternalSpeech() {
		if (!externalSpeechFrame.isVisible()) {
			return;
		}

		if (getNumberOfVisibleTabs(rightTabPane) == 0) {
			maximizeRightTabPane();
		}

		externalSpeechFrame.setVisible(false);


		addComponentAndUpdate(rightVbox, peerViewContainer, 0);

		if (!currentSpeech) {
			peerViewContainer.setVisible(false);
		}
	}

	@Override
	public void showMessagesPlaceholder() {
		externalMessagesFrame.hideBody();

		messagesPanel.remove(messagesPane);
		messagesPanel.add(messagesPlaceholder);
		bottomTabPane.revalidate();
		bottomTabPane.repaint();
	}

	@Override
	public void hideMessagesPlaceholder() {
		externalMessagesFrame.showBody();

		messagesPanel.remove(messagesPlaceholder);
		messagesPanel.add(messagesPane);
		bottomTabPane.revalidate();
		bottomTabPane.repaint();
	}

	@Override
	public void setMessageBarPosition(MessageBarPosition position) {
		switch (position) {
			case BOTTOM:
				showMessageBarBottom();
				break;
			case LEFT:
				showMessageBarLeft();
				break;
			case RIGHT:
				showMessageBarRight();
				break;
		}

		messageBarPosition = position;
	}

	private void showMessageBarBottom() {
		if (messageBarPosition == MessageBarPosition.BOTTOM) {
			return;
		}

		moveMessageBarTabsToBottom();
	}

	private void showMessageBarLeft() {
		if (messageBarPosition == MessageBarPosition.LEFT) {
			return;
		}

		moveMessageBarTabsToLeft();
	}

	private void showMessageBarRight() {
		if (messageBarPosition == MessageBarPosition.RIGHT) {
			return;
		}

		moveMessageBarTabsToRight();
	}

	private void removeComponentAndUpdate(JComponent component, Component componentToRemove) {
		component.remove(componentToRemove);
		component.revalidate();
		component.repaint();
	}

	private void addComponentAndUpdate(JComponent component, Component componentToAdd) {
		addComponentAndUpdate(component, componentToAdd, -1);
	}

	private void addComponentAndUpdate(JComponent component, Component componentToAdd, int index) {
		component.add(componentToAdd, index);
		component.revalidate();
		component.repaint();
	}

	private void removePeerView(long requestId) {
		for (var component : peerViewContainer.getComponents()) {
			if (!(component instanceof PeerView)) {
				continue;
			}

			PeerView peerView = (PeerView) component;

			if (Objects.equals(peerView.getRequestId(), requestId)) {
				this.peerView = null;

				peerViewContainer.setVisible(false);
				peerViewContainer.remove(peerView);
				peerViewContainer.revalidate();
				peerViewContainer.repaint();

				currentSpeech = false;

				externalSpeechFrame.hideBody();
			}
		}
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
		setLeftTabVisible(dict.get("menu.contents"), show);
	}

	private void selectOutlineItem(Page page) {
		DocumentOutlineItem outlineItem = page.getDocument().getDocumentOutline().getOutlineItem(page.getPageNumber());

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

	private void setMessageBarTabVisible(String labelText, boolean visible) {
		switch (messageBarPosition) {
			case BOTTOM:
				setBottomTabVisible(labelText, visible);
				break;
			case LEFT:
				setLeftTabVisible(labelText, visible);
				break;
			case RIGHT:
				setRightTabVisible(labelText, visible);
				break;
		}
	}

	private void setLeftTabVisible(String labelText, boolean visible) {
		setTabVisible(leftTabs, labelText, visible, this::updateLeftTabs);
	}

	private void setBottomTabVisible(String labelText, boolean visible) {
		setTabVisible(bottomTabs, labelText, visible, this::updateBottomTabs);
	}

	private void setRightTabVisible(String labelText, boolean visible) {
		setTabVisible(rightTabs, labelText, visible, this::updateRightTabs);
	}

	private void setTabVisible(ArrayList<TabData> tabs, String labelText, boolean visible, Runnable updateTabsFunc) {
		tabs.stream().filter(tabData1 -> tabData1.getTabComponent().getText().equals(labelText)).findFirst()
				.ifPresent(tabData -> {
					tabData.setVisible(visible);
					updateTabsFunc.run();
				});
	}

	private int getNumberOfVisibleTabs(JTabbedPane pane) {
		int numberOfVisible = 0;

		for (int i = 0; i < pane.getTabCount(); i++) {
			if (pane.getTabComponentAt(i).isVisible()) {
				numberOfVisible++;
			}
		}

		return numberOfVisible;
	}

	private void moveMessageBarTabsToLeft() {
		final LinkedList<TabData> tabs = removeMessageBarTabs();
		addMessageBarTabs(leftTabs, tabs, tab -> VerticalTab.fromJLabel(tab, SwingConstants.LEFT),
				this::updateLeftTabs);
	}

	private void moveMessageBarTabsToBottom() {
		final LinkedList<TabData> tabs = removeMessageBarTabs();
		addMessageBarTabs(bottomTabs, tabs, tab -> new JLabel(tab.getText(), tab.getIcon(), SwingConstants.LEFT),
				this::updateBottomTabs);
	}

	private void moveMessageBarTabsToRight() {
		final LinkedList<TabData> tabs = removeMessageBarTabs();
		addMessageBarTabs(rightTabs, tabs, tab -> VerticalTab.fromJLabel(tab, SwingConstants.RIGHT),
				this::updateRightTabs);
	}

	private void addMessageBarTabs(ArrayList<TabData> tabs, LinkedList<TabData> tabsToAdd,
								   UnaryOperator<JLabel> transformTabFunc, Runnable updateTabsFunc) {
		for (final TabData tabToAdd : tabsToAdd) {
			tabToAdd.setTabComponent(transformTabFunc.apply(tabToAdd.getTabComponent()));
			tabs.add(tabToAdd);
		}

		updateTabsFunc.run();
	}

	private LinkedList<TabData> removeMessageBarTabs() {
		switch (messageBarPosition) {
			case BOTTOM:
				return removeMessageBarTabs(bottomTabs, this::updateBottomTabs);
			case LEFT:
				return removeMessageBarTabs(leftTabs, this::updateLeftTabs);
			case RIGHT:
				return removeMessageBarTabs(rightTabs, this::updateRightTabs);
		}

		return new LinkedList<>();
	}

	private LinkedList<TabData> removeMessageBarTabs(ArrayList<TabData> tabs, Runnable updateTabsFunc) {
		final LinkedList<TabData> removedTabs = new LinkedList<>();

		final Iterator<TabData> iter = tabs.iterator();

		while (iter.hasNext()) {
			final TabData tab = iter.next();

			if (!(tab instanceof MessageTabData)) {
				continue;
			}

			removedTabs.add(tab);
			iter.remove();
		}

		updateTabsFunc.run();

		return removedTabs;
	}

	private void setMessageBarTabEnabled(String labelText, boolean enable) {
		switch (messageBarPosition) {
			case BOTTOM:
				setTabEnabled(bottomTabs, labelText, enable, this::updateBottomTabs);
				break;
			case LEFT:
				setTabEnabled(leftTabs, labelText, enable, this::updateLeftTabs);
				break;
			case RIGHT:
				setTabEnabled(rightTabs, labelText, enable, this::updateRightTabs);
				break;
		}
	}

	private void setTabEnabled(ArrayList<TabData> tabs, String labelText, boolean enabled, Runnable updateTabsFunc) {
		final TabData tab = getTabByLabel(tabs, labelText);

		if (tab != null) {
			tab.setEnabled(enabled);

			updateTabsFunc.run();
		}
	}

	private void setMessageBarTabSelected(String labelText, boolean select) {
		switch (messageBarPosition) {
			case BOTTOM:
				setTabSelected(bottomTabPane, labelText, select, this::updateBottomTabPaneIndex);
				break;
			case LEFT:
				setTabSelected(leftTabPane, labelText, select, this::updateLeftTabPaneIndex);
				break;
			case RIGHT:
				setTabSelected(rightTabPane, labelText, select, this::updateRightTabPaneIndex);
				break;
		}
	}

	private void setTabSelected(JTabbedPane pane, String labelText, boolean select, Runnable updateTabIndexFunc) {
		final int index = getTabIndex(pane, labelText);
		if (select) {
			pane.setSelectedIndex(index);
		}
		if (!select && pane.getSelectedIndex() == index) {
			pane.setSelectedIndex(-1);
		}
		updateTabIndexFunc.run();
	}

	private void updateLeftTabPaneIndex() {
		leftTabPrevSelectedIndex = leftTabPane.getSelectedIndex();
	}

	private void updateBottomTabPaneIndex() {
		bottomTabPrevSelectedIndex = bottomTabPane.getSelectedIndex();
	}

	private void updateRightTabPaneIndex() {
		rightTabPrevSelectedIndex = rightTabPane.getSelectedIndex();
	}

	private void updateLeftTabs() {
		updateTabs(leftTabPane, leftTabs, this::minimizeLeftTabPane, this::updateLeftTabPaneIndex,
				() -> new Dimension(getLeftTabWidth(), 0));
	}

	private void updateBottomTabs() {
		updateTabs(bottomTabPane, bottomTabs, this::minimizeBottomTabPane, this::updateBottomTabPaneIndex,
				() -> new Dimension(0, getBottomTabHeight()));
	}

	private void updateRightTabs() {
		updateTabs(rightTabPane, rightTabs, this::minimizeRightTabPane, this::updateRightTabPaneIndex,
				() -> new Dimension(getRightTabWidth(), 0));
	}

	private void updateExternalSlideTabs() {
		updateTabs(externalSlidePreviewTabPane, externalSlideTabs, () -> {
		}, () -> {
		}, () -> new Dimension(0, 0));
	}

	private void updateSlidesTabs() {
		if (externalSlidePreviewFrame.isVisible()) {
			updateExternalSlideTabs();
		} else {
			updateRightTabs();
		}
		checkIfThumbSelected();
	}

	private void updateTabs(JTabbedPane tabPane, ArrayList<TabData> tabs, Runnable minimizeFunc,
							Runnable updateIndexFunc, Supplier<Dimension> minimumDimensionFunc) {
		final int numberOfPrevVisible = getNumberOfVisibleTabs(tabPane);
		int numberOfVisible = 0;

		final int oldSelectedIndex = tabPane.getSelectedIndex();
		final String selectedLabel = oldSelectedIndex >= 0 && oldSelectedIndex < tabPane.getTabCount() ?
				((JLabel) tabPane.getTabComponentAt(oldSelectedIndex)).getText() : "";

		tabPane.removeAll();

		int selectedIndex = -1;

		for (final TabData tab : tabs) {
			if (!tab.isVisible()) {
				continue;
			}

			tabPane.addTab(null, tab.component);

			final int index = tabPane.getTabCount() - 1;

			tab.getTabComponent().setEnabled(tab.isEnabled());
			tabPane.setTabComponentAt(index, tab.getTabComponent());
			tabPane.setEnabledAt(index, tab.isEnabled());

			if (tab.getTabComponent().getText().equals(selectedLabel)) {
				selectedIndex = index;
			}

			numberOfVisible++;
		}

		tabPane.setSelectedIndex(selectedIndex >= 0 ? selectedIndex : tabPane.getTabCount() - 1);

		tabPane.setMinimumSize(minimumDimensionFunc.get());
		updateIndexFunc.run();

		if (numberOfVisible >= 1 && numberOfPrevVisible == 0) {
			tabPane.setVisible(true);
			minimizeFunc.run();
		}
		if (numberOfVisible == 0) {
			tabPane.setVisible(false);
			minimizeFunc.run();
		}
	}

	private void maximizeLeftTabPane() {
		maximizePane(docSplitPane, oldDocSplitPaneDividerRatio, docSplitPane::getWidth);
	}

	private void minimizeLeftTabPane() {
		minimizeLeftTabPane(false);
	}

	private void minimizeLeftTabPane(boolean saveOldRatio) {
		minimizePane(docSplitPane, this::getLeftTabWidth, leftTabPane::getWidth,
				(pane, tabSize, tabOffset) -> tabSize,
				() -> oldDocSplitPaneDividerRatio = getDocSplitPaneDividerRatio(), saveOldRatio);
	}

	private void minimizeBottomTabPane() {
		minimizeBottomTabPane(false);
	}

	private void minimizeBottomTabPane(boolean saveOldRatio) {
		minimizePane(notesSplitPane, this::getBottomTabHeight, bottomTabPane::getHeight,
				(pane, tabSize, tabOffset) -> pane.getHeight() - pane.getDividerSize() - tabSize - tabOffset,
				() -> oldNotesSplitPaneDividerRatio = getNotesSplitPaneDividerRatio(), saveOldRatio);
	}

	private void maximizeBottomTabPane() {
		maximizePane(notesSplitPane, oldNotesSplitPaneDividerRatio, notesSplitPane::getHeight);
	}

	private void minimizeRightTabPane() {
		minimizeRightTabPane(false);
	}

	private void minimizeRightTabPane(boolean saveOldRatio) {
		minimizePane(tabSplitPane, this::getRightTabWidth, tabSplitPane::getWidth,
				(pane, tabSize, tabOffset) -> pane.getWidth() - pane.getDividerSize() - tabSize - tabOffset,
				() -> oldTabSplitPaneDividerRatio = getTabSplitPaneDividerRatio(), saveOldRatio);
	}

	private void maximizeRightTabPane() {
		maximizePane(tabSplitPane, oldTabSplitPaneDividerRatio, tabSplitPane::getWidth);

		final int dividerLocation = (int) (tabSplitPane.getWidth() * oldTabSplitPaneDividerRatio);

		tabSplitPane.setDividerLocation(dividerLocation);
	}

	private void minimizePane(JSplitPane splitPane, IntSupplier tabSizeFunc, IntSupplier tabPaneSizeFunc,
							  SplitPaneDividerLocationAction locationFunc,
							  Runnable updateSplitPaneRatioFunc, boolean saveOldRatio) {
		final int tabSize = tabSizeFunc.getAsInt();

		if (tabPaneSizeFunc.getAsInt() <= tabSize && saveOldRatio) {
			return;
		}

		if (saveOldRatio) {
			updateSplitPaneRatioFunc.run();
		}

		splitPane.setDividerLocation(locationFunc.calculate(splitPane, tabSize, TAB_OFFSET));
	}

	private void maximizePane(JSplitPane splitPane, double oldSplitPaneRatio, IntSupplier splitPaneSizeFunc) {
		final int dividerLocation = (int) (oldSplitPaneRatio * splitPaneSizeFunc.getAsInt());

		splitPane.setDividerLocation(dividerLocation);
	}

	private double getTabSplitPaneDividerRatio() {
		return tabSplitPane.getDividerLocation() / (double) tabSplitPane.getWidth();
	}

	private double getNotesSplitPaneDividerRatio() {
		return notesSplitPane.getDividerLocation() / (double) notesSplitPane.getHeight();
	}

	private double getDocSplitPaneDividerRatio() {
		return docSplitPane.getDividerLocation() / (double) docSplitPane.getWidth();
	}

	private void toggleLeftTab(int index) {
		toggleTab(index, leftTabPane, leftTabPane::getWidth, this::getLeftTabWidth, this::minimizeLeftTabPane,
				this::maximizeLeftTabPane, leftTabPrevSelectedIndex, this::updateLeftTabPaneIndex);
	}

	private void toggleBottomTab(int index) {
		toggleTab(index, bottomTabPane, bottomTabPane::getHeight, this::getBottomTabHeight, this::minimizeBottomTabPane,
				this::maximizeBottomTabPane, bottomTabPrevSelectedIndex, this::updateBottomTabPaneIndex);
	}

	private void toggleRightTab(int index) {
		toggleTab(index, rightTabPane, rightTabPane::getWidth, this::getRightTabWidth, this::minimizeRightTabPane,
				this::maximizeRightTabPane, rightTabPrevSelectedIndex, this::updateRightTabPaneIndex);
	}

	private void toggleTab(int index, JTabbedPane tabPane, IntSupplier tabPaneSizeFunc, IntSupplier tabSizeFunc,
						   Consumer<Boolean> minimizeFunc, Runnable maximizeFunc, int tabIndex,
						   Runnable updateTabIndexFunc) {
		if (index < 0 || index >= tabPane.getTabCount() || !tabPane.isEnabledAt(index)) {
			return;
		}

		boolean isSameTab = index == tabIndex;

		final int tabPaneSize = tabPaneSizeFunc.getAsInt();
		final int tabSize = tabSizeFunc.getAsInt();

		if (isSameTab) {
			if (tabPaneSize > tabSize) {
				minimizeFunc.accept(true);
			} else {
				maximizeFunc.run();
			}
		}

		updateTabIndexFunc.run();
	}

	private int getLeftTabWidth() {
		try {
			return leftTabPane.getUI().getTabBounds(leftTabPane, 0).width + 1;
		} catch (Exception ignored) {
			return 0;
		}
	}

	private int getBottomTabHeight() {
		try {
			return bottomTabPane.getUI().getTabBounds(bottomTabPane, 0).height + 1;
		} catch (Exception ignored) {
			return 0;
		}
	}

	private int getRightTabWidth() {
		try {
			return rightTabPane.getUI().getTabBounds(rightTabPane, 0).width + 1;
		} catch (Exception ignored) {
			return 0;
		}
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
				TreePath selPath = outlineTree.getPathForLocation(e.getX(), e.getY());

				if (isNull(selPath)) {
					return;
				}

				DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();

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
				executeAction(viewTransformAction, MatrixConverter.INSTANCE.from(slideView.getPageTransform()));
			}
		});

		bottomTabPane.setMinimumSize(new Dimension(0, getBottomTabHeight()));
		bottomTabPane.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					final int clickedTabIndex =
							bottomTabPane.getUI().tabForCoordinate(bottomTabPane, e.getX(), e.getY());

					toggleBottomTab(clickedTabIndex);
				}
			}
		});

		leftTabPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					final int clickedTabIndex = leftTabPane.getUI().tabForCoordinate(leftTabPane, e.getX(), e.getY());

					toggleLeftTab(clickedTabIndex);
				}
			}
		});

		rightTabPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					final int clickedTabIndex = rightTabPane.getUI().tabForCoordinate(rightTabPane, e.getX(), e.getY());

					toggleRightTab(clickedTabIndex);

					checkIfThumbSelected();
				}
			}
		});

		externalSlidePreviewTabPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					checkIfThumbSelected();
				}
			}
		});

		messagesPlaceholder = new JLabel(dict.get(NO_MESSAGES_LABEL_KEY), SwingConstants.CENTER);

		externalMessagesFrame =
				new ExternalFrame.Builder().setName(dict.get(MESSAGE_LABEL_KEY)).setBody(externalMessagesPane)
						.setPlaceholderText(dict.get(NO_MESSAGES_LABEL_KEY)).setPositionChangedAction(
								position -> executeAction(externalMessagesPositionChangedAction, position))
						.setClosedAction(() -> executeAction(externalMessagesClosedAction))
						.setSizeChangedAction(size -> executeAction(externalMessagesSizeChangedAction, size))
						.setMinimumSize(new Dimension(500, 400)).build();

		externalSlidePreviewFrame =
				new ExternalFrame.Builder().setName(dict.get(SLIDES_PREVIEW_LABEL_KEY)).setBody(
								externalSlidePreviewTabPane)
						.setPositionChangedAction(
								position -> executeAction(externalSlidePreviewPositionChangedAction, position))
						.setClosedAction(() -> executeAction(externalSlidePreviewClosedAction))
						.setSizeChangedAction(size -> executeAction(externalSlidePreviewSizeChangedAction, size))
						.setMinimumSize(new Dimension(500, 700)).build();

		externalSpeechFrame = new ExternalFrame.Builder().setName(dict.get(SPEECH_LABEL_KEY)).setBody(peerViewContainer)
				.setPlaceholderText(dict.get(CURRENTLY_NO_SPEECH_LABEL_KEY))
				.setPositionChangedAction(position -> executeAction(externalSpeechPositionChangedAction, position))
				.setClosedAction(() -> executeAction(externalSpeechClosedAction))
				.setSizeChangedAction(size -> executeAction(externalSpeechSizeChangedAction, size))
				.setMinimumSize(new Dimension(1000, 500)).build();

		saveLeftTabBarTabs();
		saveBottomTabBarTabs();

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
			}
		});

		notesSplitPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				notesSplitPane.removeComponentListener(this);
				minimizeBottomTabPane();
			}
		});
	}

	private void saveLeftTabBarTabs() {
		for (int i = 0; i < leftTabPane.getTabCount(); i++) {
			final Component component = leftTabPane.getComponent(i);
			final JLabel tabComponent = (JLabel) leftTabPane.getTabComponentAt(i);

			leftTabs.add(new TabData(component, tabComponent, true, tabComponent.isEnabled()));
		}
	}

	private void saveBottomTabBarTabs() {
		for (int i = 0; i < bottomTabPane.getTabCount(); i++) {
			final Component component = bottomTabPane.getComponent(i);
			final JLabel tabComponent = (JLabel) bottomTabPane.getTabComponentAt(i);

			bottomTabs.add(new MessageTabData(component, tabComponent, true, tabComponent.isEnabled()));
		}

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

	private void removeMessageViews(Class<? extends MessagePanel> cls) {
		for (Component c : messageViewContainer.getComponents()) {
			if (cls.isAssignableFrom(c.getClass())) {
				messageViewContainer.remove(c);
			}
		}

		messageViewContainer.validate();
		messageViewContainer.repaint();
	}

	private BufferedImage convertVideoFrame(VideoFrame videoFrame, BufferedImage image) throws Exception {
		VideoFrameBuffer buffer = videoFrame.buffer;
		int width = buffer.getWidth();
		int height = buffer.getHeight();

		// Scale video frame down to the view size.
		double uiScale = getGraphicsConfiguration().getDefaultTransform().getScaleX();
		int viewHeight = (int) (peerView.getHeight() * uiScale);
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
}
