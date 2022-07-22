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
import java.util.Objects;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Matrix;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentOutline;
import org.lecturestudio.core.model.DocumentOutlineItem;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.*;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.model.MessageBarPosition;
import org.lecturestudio.presenter.api.config.SlideViewConfiguration;
import org.lecturestudio.swing.model.AdaptiveTab;
import org.lecturestudio.swing.model.AdaptiveTabType;
import org.lecturestudio.swing.model.ExternalWindowPosition;
import org.lecturestudio.presenter.api.stylus.StylusHandler;
import org.lecturestudio.presenter.api.view.SlidesView;
import org.lecturestudio.presenter.swing.input.StylusListener;
import org.lecturestudio.stylus.awt.AwtStylusManager;
import org.lecturestudio.swing.components.*;
import org.lecturestudio.swing.components.SlideView;
import org.lecturestudio.swing.converter.KeyEventConverter;
import org.lecturestudio.swing.converter.MatrixConverter;
import org.lecturestudio.swing.util.AdaptiveTabbedPaneChangeListener;
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

	private static final String MENU_LABEL_KEY = "menu.contents";

	private final Dictionary dict;

	private ConsumerAction<org.lecturestudio.core.input.KeyEvent> keyAction;

	private ConsumerAction<Document> selectDocumentAction;

	private ConsumerAction<DocumentOutlineItem> outlineAction;

	private ConsumerAction<Page> selectPageAction;

	private ConsumerAction<Matrix> viewTransformAction;

	private ConsumerAction<MessengerMessage> discardMessageAction;

	private ConsumerAction<MessengerMessage> createMessageSlideAction;

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

	private AdaptiveTabbedPane rightTabPane;

	private Container peerViewContainer;

	private SettingsTab messageTab;

	private JScrollPane messagesPane;

	private Box messageViewContainer;

	private AdaptiveTabbedPane bottomTabPane;

	private JPanel messagesPanel;

	private Box messageSendPanel;

	private JTextField messageTextField;

	private JButton sendMessageButton;

	private AdaptiveTabbedPane leftTabPane;

	private ExternalFrame externalMessagesFrame;

	private ExternalFrame externalSlidePreviewFrame;

	private ExternalFrame externalSpeechFrame;

	private final JScrollPane externalMessagesPane = new JScrollPane();

	private double oldDocSplitPaneDividerRatio = 0.15;

	private double oldNotesSplitPaneDividerRatio = 0.75;

	private double oldTabSplitPaneDividerRatio = 0.9;

	private boolean currentSpeech = false;

	private JLabel messagesPlaceholder;

	private final AdaptiveTabbedPane externalSlidePreviewTabPane = new AdaptiveTabbedPane(SwingConstants.RIGHT);

	private MessageBarPosition messageBarPosition = MessageBarPosition.BOTTOM;

	private String selectedSlideLabelText = "";


	@Inject
	SwingSlidesView(Dictionary dictionary) {
		super();

		this.dict = dictionary;
	}

	@Override
	public void setSlideViewConfig(SlideViewConfiguration viewConfig) {
		docSplitPane.setDividerLocation(viewConfig.getLeftSliderPosition());
		tabSplitPane.setDividerLocation(viewConfig.getRightSliderPosition());
		notesSplitPane.setDividerLocation(viewConfig.getBottomSliderPosition());

		oldNotesSplitPaneDividerRatio = viewConfig.getBottomSliderPosition();
		oldDocSplitPaneDividerRatio = viewConfig.getLeftSliderPosition();
		oldTabSplitPaneDividerRatio = viewConfig.getRightSliderPosition();

		observeDividerLocation(docSplitPane, viewConfig.leftSliderPositionProperty());
		observeDividerLocation(tabSplitPane, viewConfig.rightSliderPositionProperty());
		observeDividerLocation(notesSplitPane, viewConfig.bottomSliderPositionProperty());
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
			final AdaptiveTabbedPane slidesTabPane = getSlidesTabPane();

			// Select document tab.
			int tabCount = slidesTabPane.getPaneTabCount();

			for (int i = 0; i < tabCount; i++) {
				final Component tabComponent = slidesTabPane.getPaneComponentAt(i);
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
			}
			else if (doc.isQuiz()) {
				QuizThumbnailPanel quizThumbPanel = new QuizThumbnailPanel(dict);
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

			VerticalTab tab = VerticalTab.fromText(doc.getName(), getSlidesTabPane().getTabPlacement());
			getSlidesTabPane().addTabBefore(new AdaptiveTab(AdaptiveTabType.SLIDE, tab, thumbPanel),
					AdaptiveTabType.MESSAGE);
			getSlidesTabPane().setPaneTabSelected(tab.getText());
			selectedSlideLabelText = tab.getText();
		});
	}

	@Override
	public void removeDocument(Document doc) {
		final AdaptiveTabbedPane slidesTabPane = getSlidesTabPane();

		// Remove document tab.
		for (final AdaptiveTab tab : slidesTabPane.getTabs()) {
			if (!(tab.getComponent() instanceof ThumbPanel)) {
				continue;
			}

			ThumbPanel thumbnailPanel = (ThumbPanel) tab.getComponent();
			if (thumbnailPanel.getDocument().equals(doc)) {
				slidesTabPane.removeTab(tab.getLabelText());
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
			final AdaptiveTabbedPane slidesTabPane = getSlidesTabPane();

			// Select document tab.
			int tabCount = slidesTabPane.getPaneTabCount();

			for (int i = 0; i < tabCount; i++) {
				final Component tabComponent = slidesTabPane.getPaneComponentAt(i);

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
					else {
						slidesTabPane.setPaneTabSelected(doc.getName());
						selectedSlideLabelText = doc.getName();
					}
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
		final AdaptiveTabbedPane slidesTabPane = getSlidesTabPane();

		for (int i = 0; i < slidesTabPane.getPaneTabCount(); i++) {
			final Component tabComponent = slidesTabPane.getPaneComponentAt(i);
			if (!(tabComponent instanceof QuizThumbnailPanel)) {
				continue;
			}

			QuizThumbnailPanel quizPanel = (QuizThumbnailPanel) slidesTabPane.getPaneComponentAt(i);
			quizPanel.setQuizState(state);
			break;

		}
	}

	@Override
	public void setStreamState(ExecutableState state) {
		SwingUtils.invoke(() -> {
			if (state == ExecutableState.Started) {
				setMessageBarTabEnabled(dict.get(MESSAGE_LABEL_KEY), true);
			}
			else {
				removeMessageViews(SpeechRequestView.class);
			}
		});
	}

	@Override
	public void setMessengerState(ExecutableState state) {
		SwingUtils.invoke(() -> {
			boolean started = state == ExecutableState.Started;

			if (started) {
				removeMessageViews(MessageView.class);
				setMessageBarTabEnabled(dict.get(MESSAGE_LABEL_KEY), true);
			}

			messageSendPanel.setVisible(started);
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
			messageView.setOnCreateSlide(() -> {
				createMessageSlideAction.execute(message);

				removeMessageView(messageView);
			});
			messageView.pack();

			addMessageView(messageView);
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

			addMessageView(requestView);
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
	public void setOnCreateMessageSlide(ConsumerAction<MessengerMessage> action) {
		createMessageSlideAction = action;
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

				if (rightTabPane.getPaneTabCount() == 0 && !externalSpeechFrame.isVisible()) {
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
		}
		catch (Exception e) {
			return;
		}

		SwingUtils.invoke(() -> {
			if (nonNull(peerView)) {
				peerView.showImage(peerViewImage);
			}
		});
	}

	@Override
	public void setOnKeyEvent(ConsumerAction<KeyEvent> action) {
		keyAction = action;
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
	public void setOnSendMessage(ConsumerAction<String> action) {
		SwingUtils.bindAction(sendMessageButton, () -> {
			action.execute(messageTextField.getText());

			messageTextField.setText("");
		});
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

		final String prevSelected = selectedSlideLabelText;
		externalSlidePreviewTabPane.addTabs(rightTabPane.removeTabsByType(AdaptiveTabType.SLIDE));
		externalSlidePreviewTabPane.setPaneTabSelected(prevSelected);
	}

	@Override
	public void hideExternalSlidePreview() {
		if (!externalSlidePreviewFrame.isVisible()) {
			return;
		}

		externalSlidePreviewFrame.hideBody();
		externalSlidePreviewFrame.setVisible(false);

		final String prevSelected = selectedSlideLabelText;
		rightTabPane.addTabsBefore(externalSlidePreviewTabPane.removeTabsByType(AdaptiveTabType.SLIDE),
				AdaptiveTabType.MESSAGE);
		rightTabPane.setPaneTabSelected(prevSelected);
	}

	private AdaptiveTabbedPane getSlidesTabPane() {
		return externalSlidePreviewFrame.isVisible() ? externalSlidePreviewTabPane : rightTabPane;
	}

	@Override
	public void showExternalSpeech(Screen screen, Point position, Dimension size) {
		if (externalSpeechFrame.isVisible()) {
			return;
		}

		rightVbox.remove(peerViewContainer);
		rightVbox.revalidate();
		rightVbox.repaint();

		peerViewContainer.setVisible(true);

		externalSpeechFrame.updatePosition(screen, position, size);
		externalSpeechFrame.setVisible(true);
	}

	@Override
	public void hideExternalSpeech() {
		if (!externalSpeechFrame.isVisible()) {
			return;
		}

		if (rightTabPane.getPaneTabCount() == 0) {
			maximizeRightTabPane();
		}

		externalSpeechFrame.setVisible(false);

		rightVbox.add(peerViewContainer, 0);
		rightVbox.revalidate();
		rightVbox.repaint();

		if (!currentSpeech) {
			peerViewContainer.setVisible(false);
		}
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

	private void showMessagesPlaceholder() {
		externalMessagesFrame.hideBody();

		messagesPanel.removeAll();
		messagesPanel.add(messagesPlaceholder);
		messagesPanel.revalidate();
		messagesPanel.repaint();
	}

	private void hideMessagesPlaceholder() {
		externalMessagesFrame.showBody();

		messagesPanel.removeAll();
		messagesPanel.add(messagesPane);
		messagesPanel.revalidate();
		messagesPanel.repaint();
	}

	private void showMessageBarBottom() {
		if (messageBarPosition == MessageBarPosition.BOTTOM) {
			return;
		}

		final boolean prevMinimized = isBottomTabPaneMinimized();
		bottomTabPane.addTabs(removeMessageBarTabs());

		if (prevMinimized) {
			minimizeBottomTabPane();
		}
	}

	private void showMessageBarLeft() {
		if (messageBarPosition == MessageBarPosition.LEFT) {
			return;
		}

		final boolean prevMinimized = isLeftTabPaneMinimized();
		leftTabPane.addTabs(removeMessageBarTabs());

		if (prevMinimized) {
			minimizeLeftTabPane();
		}
	}

	private void showMessageBarRight() {
		if (messageBarPosition == MessageBarPosition.RIGHT) {
			return;
		}

		final boolean prevMinimized = isRightTabPaneMinimized();
		rightTabPane.addTabs(removeMessageBarTabs());

		if (prevMinimized) {
			minimizeRightTabPane();
		}
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
		setLeftTabVisible(dict.get(MENU_LABEL_KEY), show);
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
		final boolean prevMinimized = isLeftTabPaneMinimized();
		setTabVisible(leftTabPane, labelText, visible, this::minimizeLeftTabPane);

		if (prevMinimized) {
			minimizeLeftTabPane();
		}
	}

	private void setBottomTabVisible(String labelText, boolean visible) {
		final boolean prevMinimized = isBottomTabPaneMinimized();
		setTabVisible(bottomTabPane, labelText, visible, this::minimizeBottomTabPane);

		if (prevMinimized) {
			minimizeBottomTabPane();
		}
	}

	private void setRightTabVisible(String labelText, boolean visible) {
		final boolean prevMinimized = isRightTabPaneMinimized();
		setTabVisible(rightTabPane, labelText, visible, this::minimizeRightTabPane);

		if (prevMinimized) {
			minimizeRightTabPane();
		}
	}

	private void setTabVisible(AdaptiveTabbedPane tabbedPane, String labelText,
			boolean visible, Runnable minimizeFunc) {
		final int prevVisibleTabCount = tabbedPane.getPaneTabCount();

		tabbedPane.setTabVisible(labelText, visible);

		final int visibleTabCount = tabbedPane.getPaneTabCount();

		if (visibleTabCount == 0 || (prevVisibleTabCount == 0 && visibleTabCount >= 1)) {
			minimizeFunc.run();
		}
	}

	private List<AdaptiveTab> removeMessageBarTabs() {
		final ArrayList<AdaptiveTab> removedTabs = new ArrayList<>();
		final boolean prevMinimized;

		switch (messageBarPosition) {
			case BOTTOM:
				prevMinimized = isBottomTabPaneMinimized();
				removedTabs.addAll(bottomTabPane.removeTabsByType(AdaptiveTabType.MESSAGE));
				if (prevMinimized) {
					minimizeBottomTabPane();
				}
				break;
			case LEFT:
				prevMinimized = isLeftTabPaneMinimized();
				removedTabs.addAll(leftTabPane.removeTabsByType(AdaptiveTabType.MESSAGE));
				if (prevMinimized) {
					minimizeLeftTabPane();
				}
				break;
			case RIGHT:
				prevMinimized = isRightTabPaneMinimized();
				removedTabs.addAll(rightTabPane.removeTabsByType(AdaptiveTabType.MESSAGE));
				if (prevMinimized) {
					minimizeRightTabPane();
				}
				break;
		}

		return removedTabs;
	}

	private void setMessageBarTabEnabled(String labelText, boolean enable) {
		switch (messageBarPosition) {
			case BOTTOM:
				bottomTabPane.setTabEnabled(labelText, enable);
				if (enable) {
					maximizeBottomTabPane();
				}
				break;
			case LEFT:
				leftTabPane.setTabEnabled(labelText, enable);
				if (enable) {
					maximizeLeftTabPane();
				}
				break;
			case RIGHT:
				rightTabPane.setTabEnabled(labelText, enable);
				if (enable) {
					maximizeRightTabPane();
				}
				break;
		}
	}

	private void maximizeLeftTabPane() {
		maximizePane(docSplitPane, oldDocSplitPaneDividerRatio, docSplitPane.getWidth());
	}

	private void minimizeLeftTabPane() {
		minimizeLeftTabPane(false);
	}

	private void minimizeLeftTabPane(boolean saveOldRatio) {
		minimizePane(docSplitPane, isLeftTabPaneMinimized(), leftTabPane.getPaneMainAxisSize(),
				() -> oldDocSplitPaneDividerRatio = getDocSplitPaneDividerRatio(), saveOldRatio);
	}

	private void minimizeBottomTabPane() {
		minimizeBottomTabPane(false);
	}

	private void minimizeBottomTabPane(boolean saveOldRatio) {
		minimizePane(notesSplitPane, isBottomTabPaneMinimized(),
				notesSplitPane.getHeight() - notesSplitPane.getDividerSize() - bottomTabPane.getPaneMainAxisSize(),
				() -> oldNotesSplitPaneDividerRatio = getNotesSplitPaneDividerRatio(), saveOldRatio);
	}

	private void maximizeBottomTabPane() {
		maximizePane(notesSplitPane, oldNotesSplitPaneDividerRatio, notesSplitPane.getHeight());
	}

	private void minimizeRightTabPane() {
		minimizeRightTabPane(false);
	}

	private void minimizeRightTabPane(boolean saveOldRatio) {
		if (currentSpeech && !externalSpeechFrame.isVisible()) {
			return;
		}

		minimizePane(tabSplitPane, isRightTabPaneMinimized(),
				tabSplitPane.getWidth() - tabSplitPane.getDividerSize() - rightTabPane.getPaneMainAxisSize(),
				() -> oldTabSplitPaneDividerRatio = getTabSplitPaneDividerRatio(), saveOldRatio);
	}

	private void maximizeRightTabPane() {
		maximizePane(tabSplitPane, oldTabSplitPaneDividerRatio, tabSplitPane.getWidth());
	}

	private boolean isRightTabPaneMinimized() {
		return rightTabPane.getWidth() <= rightTabPane.getPaneMainAxisSize();
	}

	private boolean isLeftTabPaneMinimized() {
		return leftTabPane.getWidth() <= leftTabPane.getPaneMainAxisSize();
	}

	private boolean isBottomTabPaneMinimized() {
		return bottomTabPane.getHeight() <= bottomTabPane.getPaneMainAxisSize();
	}

	private void minimizePane(JSplitPane splitPane, boolean isMinimized, int minimizedDividerLocation,
							  Runnable updateSplitPaneRatioFunc, boolean saveOldRatio) {
		if (isMinimized && saveOldRatio) {
			return;
		}

		if (saveOldRatio) {
			updateSplitPaneRatioFunc.run();
		}

		splitPane.setDividerLocation(minimizedDividerLocation);
	}

	private void maximizePane(JSplitPane splitPane, double oldSplitPaneRatio, int splitPaneSize) {
		final int dividerLocation = (int) (oldSplitPaneRatio * splitPaneSize);

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

	private void toggleLeftTab(boolean sameTab) {
		toggleTab(sameTab, leftTabPane::getWidth, leftTabPane::getPaneMainAxisSize, this::minimizeLeftTabPane,
				this::maximizeLeftTabPane);
	}

	private void toggleBottomTab(boolean sameTab) {
		toggleTab(sameTab, bottomTabPane::getHeight, bottomTabPane::getPaneMainAxisSize, this::minimizeBottomTabPane,
				this::maximizeBottomTabPane);
	}

	private void toggleRightTab(boolean sameTab) {
		toggleTab(sameTab, rightTabPane::getWidth, rightTabPane::getPaneMainAxisSize, this::minimizeRightTabPane,
				this::maximizeRightTabPane);
	}

	private void toggleTab(boolean sameTab, IntSupplier tabPaneSizeFunc, IntSupplier tabSizeFunc,
						   Consumer<Boolean> minimizeFunc, Runnable maximizeFunc) {
		final int tabPaneSize = tabPaneSizeFunc.getAsInt();
		final int tabSize = tabSizeFunc.getAsInt();

		if (sameTab) {
			if (tabPaneSize > tabSize) {
				minimizeFunc.accept(true);
			}
			else {
				maximizeFunc.run();
			}
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

		leftTabPane.addChangeListener(new AdaptiveTabbedPaneChangeListener() {
			@Override
			public void onTabClicked(AdaptiveTab clickedTab, boolean sameTab) {
				toggleLeftTab(sameTab);
			}

			@Override
			public void onVisibilityChanged(boolean visible) {
				if (visible) {
					maximizeLeftTabPane();
				} else {
					minimizeLeftTabPane();
				}
			}

			@Override
			public void onNoTabsEnabled() {
				minimizeLeftTabPane();
			}
		});

		bottomTabPane.addChangeListener(new AdaptiveTabbedPaneChangeListener() {
			@Override
			public void onTabClicked(AdaptiveTab clickedTab, boolean sameTab) {
				toggleBottomTab(sameTab);
			}

			@Override
			public void onVisibilityChanged(boolean visible) {
				if (visible) {
					maximizeBottomTabPane();
				} else {
					minimizeBottomTabPane();
				}
			}

			@Override
			public void onNoTabsEnabled() {
				minimizeBottomTabPane();
			}
		});

		rightTabPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					checkIfThumbSelected();
				}
			}
		});

		rightTabPane.addChangeListener(new AdaptiveTabbedPaneChangeListener() {
			@Override
			public void onTabAdded(boolean visibleOrEnabled) {
				if (!visibleOrEnabled) {
					rightTabPane.setPaneTabSelected(selectedSlideLabelText);
				}
			}

			@Override
			public void onTabRemoved() {
				rightTabPane.setPaneTabSelected(selectedSlideLabelText);
			}

			@Override
			public void onTabClicked(AdaptiveTab clickedTab, boolean sameTab) {
				if (clickedTab.type == AdaptiveTabType.SLIDE) {
					selectedSlideLabelText = clickedTab.getLabelText();
				}
				toggleRightTab(sameTab);
			}

			@Override
			public void onVisibilityChanged(boolean visible) {
				if (visible) {
					maximizeRightTabPane();
				}
				else {
					minimizeRightTabPane();
				}
			}

			@Override
			public void onNoTabsEnabled() {
				minimizeRightTabPane();
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
		externalSlidePreviewTabPane.addChangeListener(new AdaptiveTabbedPaneChangeListener() {
			@Override
			public void onTabClicked(AdaptiveTab clickedTab, boolean sameTab) {
				selectedSlideLabelText = clickedTab.getLabelText();
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

		showMessagesPlaceholder();
	}

	private void observeDividerLocation(final JSplitPane pane,
			final DoubleProperty property) {
		BasicSplitPaneUI ui = (BasicSplitPaneUI) pane.getUI();
		BasicSplitPaneDivider divider = ui.getDivider();

		divider.addMouseMotionListener(new MouseAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (pane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
					property.set(pane.getDividerLocation() / (double) pane.getWidth());
				}
				else {
					property.set(pane.getDividerLocation() / (double) pane.getHeight());
				}
			}
		});
	}

	private void addMessageView(Component view) {
		messageViewContainer.add(view);
		messageViewContainer.revalidate();
		messageViewContainer.repaint();

		if (messageViewContainer.getComponentCount() == 1) {
			hideMessagesPlaceholder();
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

		if (messageViewContainer.getComponentCount() == 0) {
			showMessagesPlaceholder();
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

		if (messageViewContainer.getComponentCount() == 0) {
			showMessagesPlaceholder();
		}
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
