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

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.text.MessageFormat;
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

import org.apache.commons.lang3.StringUtils;

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
import org.lecturestudio.presenter.api.model.NoteBarPosition;
import org.lecturestudio.presenter.api.model.MessageBarPosition;
import org.lecturestudio.presenter.api.config.SlideViewConfiguration;
import org.lecturestudio.presenter.api.service.UserPrivilegeService;
import org.lecturestudio.presenter.swing.utils.ViewUtil;
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
import org.lecturestudio.swing.util.VideoFrameConverter;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;
import org.lecturestudio.web.api.event.PeerStateEvent;
import org.lecturestudio.web.api.event.RemoteVideoFrameEvent;
import org.lecturestudio.web.api.message.MessengerDirectMessage;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.SpeechBaseMessage;
import org.lecturestudio.web.api.model.UserInfo;
import org.lecturestudio.web.api.stream.model.CourseParticipant;

@SwingView(name = "main-slides")
public class SwingSlidesView extends JPanel implements SlidesView {

	private static final String MESSAGE_LABEL_KEY = "slides.messages";

	private static final String NO_MESSAGES_LABEL_KEY = "slides.no.messages";

	private static final String PARTICIPANTS_LABEL_KEY = "slides.participants";

	private static final String NO_PARTICIPANTS_LABEL_KEY = "slides.no.participants";

	private static final String SLIDES_PREVIEW_LABEL_KEY = "slides.slide.preview";

	private static final String SPEECH_LABEL_KEY = "slides.speech";

	private static final String NOTES_LABEL_KEY = "slides.notes";

	private static final String CURRENTLY_NO_SPEECH_LABEL_KEY = "slides.currently.no.speech";

	private static final String MENU_LABEL_KEY = "menu.contents";

	private final Dictionary dict;

	private final UserPrivilegeService userPrivilegeService;

	private ConsumerAction<org.lecturestudio.core.input.KeyEvent> keyAction;

	private ConsumerAction<Document> selectDocumentAction;

	private ConsumerAction<DocumentOutlineItem> outlineAction;

	private ConsumerAction<Page> selectPageAction;

	private ConsumerAction<Matrix> viewTransformAction;

	private ConsumerAction<MessengerMessage> discardMessageAction;

	private ConsumerAction<MessengerMessage> createMessageSlideAction;

	private ConsumerAction<SpeechBaseMessage> acceptSpeechRequestAction;

	private ConsumerAction<SpeechBaseMessage> rejectSpeechRequestAction;

	private ConsumerAction<Boolean> mutePeerAudioAction;

	private ConsumerAction<Boolean> mutePeerVideoAction;

	private ConsumerAction<UUID> stopPeerConnectionAction;

	private ConsumerAction<ExternalWindowPosition> externalMessagesPositionChangedAction;

	private ConsumerAction<Dimension> externalMessagesSizeChangedAction;

	private ConsumerAction<Dimension> externalNotesSizeChangedAction;

	private Action externalMessagesClosedAction;

	private ConsumerAction<ExternalWindowPosition> externalParticipantsPositionChangedAction;

	private ConsumerAction<Dimension> externalParticipantsSizeChangedAction;

	private Action externalParticipantsClosedAction;

	private ConsumerAction<ExternalWindowPosition> externalSlidePreviewPositionChangedAction;

	private ConsumerAction<Dimension> externalSlidePreviewSizeChangedAction;

	private Action externalSlidePreviewClosedAction;

	private ConsumerAction<ExternalWindowPosition> externalSpeechPositionChangedAction;

	private ConsumerAction<Dimension> externalSpeechSizeChangedAction;

	private Action externalSpeechClosedAction;

	private ConsumerAction<ExternalWindowPosition> externalNotesPositionChangedAction;

	private Action externalNotesClosedAction;

	private Action newPageAction;

	private Action deletePageAction;

	private Action stopQuizAction;

	private BooleanProperty toggleScreenShareAction;

	private Action stopScreenShareAction;

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

	private ParticipantList participantList;

	private SettingsTab messageTab;

	private JScrollPane messagesPane;

	private JScrollPane notesPane;

	private Box messageViewContainer;

	private Box notesViewContainer;

	private AdaptiveTabbedPane bottomTabPane;

	private JPanel messagesPanel;

	private JPanel notesPanel;

	private Box messageSendPanel;

	private JTextField messageTextField;

	private JButton sendMessageButton;

	private AdaptiveTabbedPane leftTabPane;

	private ExternalFrame externalMessagesFrame;

	private ExternalFrame externalParticipantsFrame;

	private ExternalFrame externalSlidePreviewFrame;

	private ExternalFrame externalSpeechFrame;

	private ExternalFrame externalNotesFrame;

	private final JScrollPane externalMessagesPane = new JScrollPane();

	private final JScrollPane externalNotesPane = new JScrollPane();

	private double oldDocSplitPaneDividerRatio = 0.15;

	private double oldNotesSplitPaneDividerRatio = 0.75;

	private double oldTabSplitPaneDividerRatio = 0.9;

	private boolean currentSpeech = false;

	private JLabel messagesPlaceholder;

	private final AdaptiveTabbedPane externalSlidePreviewTabPane = new AdaptiveTabbedPane(SwingConstants.RIGHT);

	private MessageBarPosition messageBarPosition = MessageBarPosition.BOTTOM;
	private NoteBarPosition notesBarPosition = NoteBarPosition.BOTTOM;
	private MessageBarPosition participantsPosition = MessageBarPosition.LEFT;
	private MessageBarPosition previewPosition = MessageBarPosition.RIGHT;

	private String selectedSlideLabelText = "";


	@Inject
	SwingSlidesView(Dictionary dictionary, UserPrivilegeService userPrivilegeService) {
		super();

		this.dict = dictionary;
		this.userPrivilegeService = userPrivilegeService;
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
				if (!(tabComponent instanceof ThumbPanel thumbnailPanel)) {
					continue;
				}

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
			else if (doc.isScreen()) {
				ScreenThumbnailPanel screenThumbPanel = new ScreenThumbnailPanel(dict);
				screenThumbPanel.setOnToggleScreenShare(toggleScreenShareAction);
				screenThumbPanel.setOnStopScreenShare(stopScreenShareAction);

				thumbPanel = screenThumbPanel;
			}
			else {
				thumbPanel = new ThumbnailPanel();
			}

			thumbPanel.setEnabled(slidesTabPane.isEnabled());
			thumbPanel.setRenderController(pageRenderer);
			thumbPanel.setDocument(doc, ppProvider);
			thumbPanel.addSelectedSlideChangedListener(event -> {
				if (event.getNewValue() instanceof Page page) {
					executeAction(selectPageAction, page);
				}
			});

			String tabName = StringUtils.abbreviate(doc.getName(), 32);

			VerticalTab tab = VerticalTab.fromText(tabName, getSlidesTabPane().getTabPlacement());
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
			if (!(tab.getComponent() instanceof ThumbPanel thumbnailPanel)) {
				continue;
			}

			if (thumbnailPanel.getDocument().equals(doc)) {
				slidesTabPane.removeTab(tab.getLabelText());
				break;
			}
		}
	}

	private void checkIfThumbSelected() {
		final Component selectedComponent = getSlidesTabPane().getSelectedComponent();

		if (!(selectedComponent instanceof ThumbPanel thumbPanel)) {
			return;
		}

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

				if (!(tabComponent instanceof ThumbPanel thumbnailPanel)) {
					continue;
				}

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
	public void setParticipants(Collection<CourseParticipant> participants) {
		SwingUtils.invoke(() -> {
			participantList.setParticipants(participants);
		});
	}

	@Override
	public void addParticipant(CourseParticipant participant) {
		SwingUtils.invoke(() -> {
			participantList.addParticipant(participant);
		});
	}

	@Override
	public void removeParticipant(CourseParticipant participant) {
		SwingUtils.invoke(() -> {
			participantList.removeParticipant(participant);
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
			if (selectedComponent instanceof ThumbPanel thumbPanel) {
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

				participantList.clear();
			}
			else {
				if (state == ExecutableState.Stopped) {
					participantList.clear();
				}

				removeMessageViews(SpeechRequestView.class);
			}
		});
	}

	@Override
	public void setScreenShareState(ExecutableState state, Document document) {
		final AdaptiveTabbedPane slidesTabPane = getSlidesTabPane();

		for (int i = 0; i < slidesTabPane.getPaneTabCount(); i++) {
			final Component tabComponent = slidesTabPane.getPaneComponentAt(i);
			if (!(tabComponent instanceof ScreenThumbnailPanel screenPanel)) {
				continue;
			}

			if (screenPanel.getDocument().getName().equals(document.getName())) {
				screenPanel.setScreenShareState(state);
				break;
			}
		}
	}

	@Override
	public void setMessengerState(ExecutableState state) {
		SwingUtils.invoke(() -> {
			boolean started = state == ExecutableState.Started;

			if (started) {
				removeMessageViews(MessageView.class);
				setMessageBarTabEnabled(dict.get(MESSAGE_LABEL_KEY), true);

				if (userPrivilegeService.canWriteMessages()) {
					messageSendPanel.setVisible(true);
				}
			}
			else {
				messageSendPanel.setVisible(false);
			}
		});
	}

	@Override
	public void setNotesText(String notesText){
		SwingUtils.invoke(() -> {
			AdaptiveTabbedPane currentPane;

			switch (notesBarPosition){
				case BOTTOM -> currentPane = bottomTabPane;
				case LEFT -> currentPane = leftTabPane;
				default -> currentPane = bottomTabPane;
			}

			int tabPos = currentPane.getPaneSelectedIndex();
			NotesView notesView = ViewUtil.createNotesView(NotesView.class, dict);
			notesView.setNote(notesText);
			notesView.doLayout();

			addNoteView(notesView);
			currentPane.setSelectedIndex(tabPos);
		});
	}

	@Override
	public void setMessengerMessage(MessengerMessage message) {
		SwingUtils.invoke(() -> {
			UserInfo userInfo = userPrivilegeService.getUserInfo();
			String myId = userInfo.getUserId();

			MessageView messageView = ViewUtil.createMessageView(MessageView.class, userInfo, message, dict);
			messageView.setMessage(message.getMessage().getText());
			messageView.setOnDiscard(() -> {
				executeAction(discardMessageAction, message);

				removeMessageView(messageView);
			});
			messageView.setOnCreateSlide(() -> {
				createMessageSlideAction.execute(message);

				removeMessageView(messageView);
			});

			if (message instanceof MessengerDirectMessage directMessage) {
				String recipientId = directMessage.getRecipientId();
				boolean byMe = Objects.equals(message.getUserId(), myId);
				boolean toMe = Objects.equals(recipientId, myId);
				boolean toOrganisers = Objects.equals(recipientId, "organisers");

				String sender = byMe
						? dict.get("text.message.me")
						: String.format("%s %s", message.getFirstName(), message.getFamilyName());

				String recipient = toMe
						? dict.get("text.message.to.me")
						: toOrganisers
							? dict.get("text.message.to.organisators.short")
							: String.format("%s %s", directMessage.getRecipientFirstName(), directMessage.getRecipientFamilyName());

				messageView.setUserName(MessageFormat.format(dict.get("text.message.recipient"), sender, ""));
				messageView.setPrivateText(recipient);
			}

			messageView.pack();

			addMessageView(messageView);
		});
	}

	@Override
	public void addSpeechRequest(SpeechBaseMessage message) {
		SwingUtils.invoke(() -> {
			participantList.addSpeechRequest(message);

			UserInfo userInfo = userPrivilegeService.getUserInfo();

			SpeechRequestView requestView = ViewUtil.createMessageView(SpeechRequestView.class, userInfo, message, dict);
			requestView.setRequestId(message.getRequestId());
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
	public void removeSpeechRequest(SpeechBaseMessage message) {
		SwingUtils.invoke(() -> {
			participantList.removeSpeechRequest(message);

			for (Component c : messageViewContainer.getComponents()) {
				if (c instanceof SpeechRequestView view) {
					if (Objects.equals(view.getRequestId(), message.getRequestId())) {
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
	public void setOnAcceptSpeech(ConsumerAction<SpeechBaseMessage> action) {
		acceptSpeechRequestAction = action;

		participantList.setOnAcceptSpeech(action);
	}

	@Override
	public void setOnRejectSpeech(ConsumerAction<SpeechBaseMessage> action) {
		rejectSpeechRequestAction = action;

		participantList.setOnRejectSpeech(action);
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
					if (component instanceof PeerView peerView) {
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
	public void setOnStopPeerConnection(ConsumerAction<UUID> action) {
		stopPeerConnectionAction = action;
	}

	@Override
	public void setVideoFrameEvent(RemoteVideoFrameEvent event) {
		if (isNull(peerView)) {
			return;
		}

		try {
			peerViewImage = VideoFrameConverter.convertVideoFrameToComponentSize(
					event.getFrame(), peerViewImage, peerView);
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
	public void setOnToggleScreenShare(BooleanProperty property) {
		toggleScreenShareAction = property;
	}

	@Override
	public void setOnStopScreenShare(Action action) {
		stopScreenShareAction = action;
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
	public void setOnExternalParticipantsPositionChanged(ConsumerAction<ExternalWindowPosition> action) {
		this.externalParticipantsPositionChangedAction = action;
	}

	@Override
	public void setOnExternalParticipantsSizeChanged(ConsumerAction<Dimension> action) {
		this.externalParticipantsSizeChangedAction = action;
	}

	@Override
	public void setOnExternalParticipantsClosed(Action action) {
		externalParticipantsClosedAction = action;
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
	public void setOnExternalNotesPositionChanged(ConsumerAction<ExternalWindowPosition> action) {
		this.externalNotesPositionChangedAction = action;
	}

	@Override
	public void setOnExternalNotesSizeChanged(ConsumerAction<Dimension> action) {
		this.externalNotesSizeChangedAction = action;
	}

	@Override
	public void setOnExternalNotesClosed(Action action) {
		externalNotesClosedAction = action;
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
	public void showExternalParticipants(Screen screen, Point position, Dimension size) {
//		if (externalParticipantsFrame.isVisible()) {
//			return;
//		}

//		setParticipantsTabVisible(dict.get(PARTICIPANTS_LABEL_KEY), false);
//
//		participantsPane.getViewport().remove(participantsViewContainer);
//
//		externalParticipantsPane.getViewport().add(participantsViewContainer);
//
//		externalParticipantsFrame.updatePosition(screen, position, size);
//		externalParticipantsFrame.setVisible(true);
	}

	@Override
	public void hideExternalParticipants() {
//		if (!externalParticipantsFrame.isVisible()) {
//			return;
//		}

//		externalParticipantsPane.getViewport().remove(participantsViewContainer);
//
//		externalParticipantsFrame.setVisible(false);
//
//		participantsPane.getViewport().add(participantsViewContainer);
//
//		setParticipantsTabVisible(dict.get(PARTICIPANTS_LABEL_KEY), true);
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
		if (externalSlidePreviewFrame.isVisible()) {
			return externalSlidePreviewTabPane;
		}

		if (Objects.requireNonNull(previewPosition) == MessageBarPosition.LEFT) {
			return leftTabPane;
		}
		return rightTabPane;
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
	public void showExternalNotes(Screen screen, Point position, Dimension size) {
		if (externalNotesFrame.isVisible()) {
			return;
		}

		setNotesBarTabVisible(dict.get(NOTES_LABEL_KEY), false);

		notesPane.getViewport().remove(notesViewContainer);

		externalNotesPane.getViewport().add(notesViewContainer);

		externalNotesFrame.updatePosition(screen, position, size);
		externalNotesFrame.showBody();
		externalNotesFrame.setVisible(true);
	}

	@Override
	public void hideExternalNotes() {
		if (!externalNotesFrame.isVisible()) {
			return;
		}

		externalNotesPane.getViewport().remove(notesViewContainer);

		externalNotesFrame.setVisible(false);

		notesPane.getViewport().add(notesViewContainer);

		setNotesBarTabVisible(dict.get(NOTES_LABEL_KEY), true);
	}

	@Override
	public void setMessageBarPosition(MessageBarPosition position) {
		switch (position) {
			case BOTTOM -> showMessageBarBottom();
			case LEFT -> showMessageBarLeft();
			case RIGHT -> showMessageBarRight();
		}

		messageBarPosition = position;
	}

	@Override
	public void setParticipantsPosition(MessageBarPosition position) {
		switch (position) {
			case LEFT -> showParticipantsLeft();
			case RIGHT -> showParticipantsRight();
		}

		participantsPosition = position;
	}

	@Override
	public void setPreviewPosition(MessageBarPosition position) {
		switch (position) {
			case LEFT -> showPreviewLeft();
			case RIGHT -> showPreviewRight();
		}

		previewPosition = position;
	}

	@Override
	public void setNotesBarPosition(NoteBarPosition position) {
		switch (position) {
			case LEFT -> showNoteLeft();
			case BOTTOM -> showNoteBottom();
		}

		notesBarPosition = position;
	}

	private void showNoteBottom() {
		if (notesBarPosition == NoteBarPosition.BOTTOM) {
			return;
		}
		final boolean prevMinimized = isBottomTabPaneMinimized();

		bottomTabPane.addTabs(removeNotesBarTabs());

		if (prevMinimized) {
			minimizeBottomTabPane();
		}
	}

	private void showNoteLeft() {
		if (notesBarPosition == NoteBarPosition.LEFT) {
			return;
		}

		final boolean prevMinimized = isLeftTabPaneMinimized();
		leftTabPane.addTabs(removeNotesBarTabs());

		if (prevMinimized) {
			minimizeLeftTabPane();
		}
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

	private void showParticipantsLeft() {
		if (participantsPosition == MessageBarPosition.LEFT) {
			return;
		}

		final boolean prevMinimized = isLeftTabPaneMinimized();
		leftTabPane.addTabs(removeParticipantsTabs());

		if (prevMinimized) {
			minimizeLeftTabPane();
		}
	}

	private void showParticipantsRight() {
		if (participantsPosition == MessageBarPosition.RIGHT) {
			return;
		}

		final boolean prevMinimized = isRightTabPaneMinimized();
		rightTabPane.addTabs(removeParticipantsTabs());

		if (prevMinimized) {
			minimizeRightTabPane();
		}
	}

	private void showPreviewLeft() {
		if (previewPosition == MessageBarPosition.LEFT) {
			return;
		}

		leftTabPane.addTabs(removePreviewTabs(), 0);

		maximizeLeftTabPane();
	}

	private void showPreviewRight() {
		if (previewPosition == MessageBarPosition.RIGHT) {
			return;
		}

		rightTabPane.addTabs(removePreviewTabs(), 0);

		maximizeRightTabPane();
	}

	private void removePeerView(UUID requestId) {
		for (var component : peerViewContainer.getComponents()) {
			if (!(component instanceof PeerView peerView)) {
				continue;
			}

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
			case BOTTOM -> setBottomTabVisible(labelText, visible);
			case LEFT -> setLeftTabVisible(labelText, visible);
			case RIGHT -> setRightTabVisible(labelText, visible);
		}
	}

	private void setNotesBarTabVisible(String labelText, boolean visible) {
		switch (notesBarPosition) {
			case BOTTOM -> setBottomTabVisible(labelText, visible);
			case LEFT -> setLeftTabVisible(labelText, visible);
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

		switch (messageBarPosition) {
			case BOTTOM -> removedTabs.addAll(bottomTabPane.removeTabsByType(AdaptiveTabType.MESSAGE));
			case LEFT -> removedTabs.addAll(leftTabPane.removeTabsByType(AdaptiveTabType.MESSAGE));
			case RIGHT -> removedTabs.addAll(rightTabPane.removeTabsByType(AdaptiveTabType.MESSAGE));
		}

		return removedTabs;
	}

	private void setMessageBarTabEnabled(String labelText, boolean enable) {
		switch (messageBarPosition) {
			case BOTTOM -> {
				bottomTabPane.setTabEnabled(labelText, enable);
				if (enable) {
					maximizeBottomTabPane();
				}
			}
			case LEFT -> {
				leftTabPane.setTabEnabled(labelText, enable);
				if (enable) {
					maximizeLeftTabPane();
				}
			}
			case RIGHT -> {
				rightTabPane.setTabEnabled(labelText, enable);
				if (enable) {
					maximizeRightTabPane();
				}
			}
		}
	}

	private List<AdaptiveTab> removeNotesBarTabs() {
		final ArrayList<AdaptiveTab> removedTabs = new ArrayList<>();

		switch (notesBarPosition) {
			case BOTTOM -> removedTabs.addAll(bottomTabPane.removeTabsByType(AdaptiveTabType.NOTES));
			case LEFT -> removedTabs.addAll(leftTabPane.removeTabsByType(AdaptiveTabType.NOTES));
		}

		return removedTabs;
	}

	private List<AdaptiveTab> removeParticipantsTabs() {
		final ArrayList<AdaptiveTab> removedTabs = new ArrayList<>();

		switch (participantsPosition) {
			case LEFT -> removedTabs.addAll(leftTabPane.removeTabsByType(AdaptiveTabType.PARTICIPANTS));
			case RIGHT -> removedTabs.addAll(rightTabPane.removeTabsByType(AdaptiveTabType.PARTICIPANTS));
		}

		return removedTabs;
	}

	private List<AdaptiveTab> removePreviewTabs() {
		final ArrayList<AdaptiveTab> removedTabs = new ArrayList<>();

		switch (previewPosition) {
			case LEFT -> removedTabs.addAll(leftTabPane.removeTabsByType(AdaptiveTabType.SLIDE));
			case RIGHT -> removedTabs.addAll(rightTabPane.removeTabsByType(AdaptiveTabType.SLIDE));
		}

		return removedTabs;
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
//		final int dividerLocation = (int) (oldSplitPaneRatio * splitPaneSize);

//		splitPane.setDividerLocation(dividerLocation);
		splitPane.setDividerLocation(oldSplitPaneRatio);
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
		toggleTab(sameTab, leftTabPane::getWidth,
				leftTabPane::getPaneMainAxisSize, this::minimizeLeftTabPane,
				this::maximizeLeftTabPane);
	}

	private void toggleBottomTab(boolean sameTab) {
		toggleTab(sameTab, bottomTabPane::getHeight,
				bottomTabPane::getPaneMainAxisSize, this::minimizeBottomTabPane,
				this::maximizeBottomTabPane);
	}

	private void toggleRightTab(boolean sameTab) {
		toggleTab(sameTab, rightTabPane::getWidth,
				rightTabPane::getPaneMainAxisSize, this::minimizeRightTabPane,
				this::maximizeRightTabPane);
	}

	private void toggleTab(boolean sameTab, IntSupplier tabPaneSizeFunc,
			IntSupplier tabSizeFunc, Consumer<Boolean> minimizeFunc,
			Runnable maximizeFunc) {
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
				if (clickedTab.type == AdaptiveTabType.SLIDE) {
					selectedSlideLabelText = clickedTab.getLabelText();
				}
				else {
					toggleLeftTab(sameTab);
				}
			}

			@Override
			public void onVisibilityChanged(boolean visible) {
				if (visible) {
					maximizeLeftTabPane();
				}
				else {
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
				}
				else {
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
				//toggleRightTab(sameTab);
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

//		externalParticipantsFrame =
//				new ExternalFrame.Builder().setName(dict.get(PARTICIPANTS_LABEL_KEY)).setBody(externalParticipantsPane)
//						.setPlaceholderText(dict.get(NO_PARTICIPANTS_LABEL_KEY)).setPositionChangedAction(
//								position -> executeAction(externalParticipantsPositionChangedAction, position))
//						.setClosedAction(() -> executeAction(externalParticipantsClosedAction))
//						.setSizeChangedAction(size -> executeAction(externalParticipantsSizeChangedAction, size))
//						.setMinimumSize(new Dimension(200, 600)).build();

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

		externalNotesFrame =
				new ExternalFrame.Builder().setName(dict.get(NOTES_LABEL_KEY)).setBody(externalNotesPane)
						.setClosedAction(() -> executeAction(externalNotesClosedAction))
						.setPositionChangedAction(position -> executeAction(externalNotesPositionChangedAction, position))
						.setSizeChangedAction(size -> executeAction(externalNotesSizeChangedAction, size))
						.setMinimumSize(new Dimension(500, 400)).build();

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


	private void addNoteView(Component view){
		setNotesBarTabVisible(dict.get(NOTES_LABEL_KEY), true);
		notesViewContainer.add(view);
		notesViewContainer.invalidate();
		notesViewContainer.doLayout();
		notesViewContainer.repaint();
	}

	private void addMessageView(Component view) {
		setMessageBarTabVisible(dict.get(MESSAGE_LABEL_KEY), true);

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

	/**
	 * Removes all notes from the View
	 */
	@Override
	public void clearNotesViewContainer(){
		SwingUtils.invoke(() -> {
			notesViewContainer.removeAll();
			notesViewContainer.invalidate();
			notesViewContainer.doLayout();
			notesViewContainer.repaint();
		});
	}
}
