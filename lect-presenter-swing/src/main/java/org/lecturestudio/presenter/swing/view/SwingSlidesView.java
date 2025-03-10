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

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import org.apache.commons.lang3.StringUtils;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.PageMetrics;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.geometry.Matrix;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.input.ScrollHandler;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentOutline;
import org.lecturestudio.core.model.DocumentOutlineItem;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.stylus.StylusHandler;
import org.lecturestudio.core.view.*;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.config.SlideViewConfiguration;
import org.lecturestudio.presenter.api.model.*;
import org.lecturestudio.presenter.api.service.UserPrivilegeService;
import org.lecturestudio.presenter.api.view.SlidesView;
import org.lecturestudio.presenter.swing.input.MouseListener;
import org.lecturestudio.presenter.swing.input.StylusListener;
import org.lecturestudio.presenter.swing.utils.ViewUtil;
import org.lecturestudio.stylus.awt.AwtStylusManager;
import org.lecturestudio.swing.components.AdaptiveTabbedPane;
import org.lecturestudio.swing.components.ExternalFrame;
import org.lecturestudio.swing.components.MessageAsReplyView;
import org.lecturestudio.swing.components.MessagePanel;
import org.lecturestudio.swing.components.MessageView;
import org.lecturestudio.swing.components.NotesView;
import org.lecturestudio.swing.components.ParticipantList;
import org.lecturestudio.swing.components.QuizThumbnailPanel;
import org.lecturestudio.swing.components.ScreenThumbnailPanel;
import org.lecturestudio.swing.components.SettingsTab;
import org.lecturestudio.swing.components.SlideView;
import org.lecturestudio.swing.components.SpeechRequestView;
import org.lecturestudio.swing.components.ThumbPanel;
import org.lecturestudio.swing.components.ThumbnailPanel;
import org.lecturestudio.swing.components.VerticalTab;
import org.lecturestudio.swing.components.WhiteboardThumbnailPanel;
import org.lecturestudio.swing.converter.KeyEventConverter;
import org.lecturestudio.swing.converter.MatrixConverter;
import org.lecturestudio.swing.layout.VideoTileLayout;
import org.lecturestudio.swing.model.AdaptiveTab;
import org.lecturestudio.swing.model.AdaptiveTabType;
import org.lecturestudio.swing.model.ExternalWindowPosition;
import org.lecturestudio.swing.util.AdaptiveTabbedPaneChangeListener;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.PeerView;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.SpeechBaseMessage;
import org.lecturestudio.web.api.message.util.MessageUtil;
import org.lecturestudio.web.api.model.UserInfo;
import org.lecturestudio.web.api.stream.model.CourseParticipant;

@SwingView(name = "main-slides")
public class SwingSlidesView extends JPanel implements SlidesView {

	private static final String MESSAGE_LABEL_KEY = "slides.messages";

	private static final String NO_MESSAGES_LABEL_KEY = "slides.no.messages";

	private static final String PARTICIPANTS_LABEL_KEY = "slides.participants";

	private static final String PARTICIPANT_VIDEO_LABEL_KEY = "slides.participant.video";

	private static final String NO_PARTICIPANTS_LABEL_KEY = "slides.no.participants";

	private static final String SLIDES_PREVIEW_LABEL_KEY = "slides.slide.preview";

	private static final String NOTES_LABEL_KEY = "slides.notes";

	private static final String SLIDE_NOTES_LABEL_KEY = "slides.slide.notes";

	private static final String MENU_LABEL_KEY = "menu.contents";

	private final Dictionary dict;

	private final UserPrivilegeService userPrivilegeService;

	private MouseListener mouseListener;

	private ConsumerAction<org.lecturestudio.core.input.KeyEvent> keyAction;

	private ConsumerAction<Document> selectDocumentAction;

	private ConsumerAction<DocumentOutlineItem> outlineAction;

	private ConsumerAction<Page> selectPageAction;

	private ConsumerAction<Matrix> viewTransformAction;

	private ConsumerAction<MessengerMessage> discardMessageAction;

	private ConsumerAction<MessengerMessage> createMessageSlideAction;

	private ConsumerAction<SpeechBaseMessage> acceptSpeechRequestAction;

	private ConsumerAction<SpeechBaseMessage> rejectSpeechRequestAction;

	private ConsumerAction<ExternalWindowPosition> externalMessagesPositionChangedAction;

	private ConsumerAction<Dimension> externalMessagesSizeChangedAction;

	private ConsumerAction<Dimension> externalNotesSizeChangedAction;

	private ConsumerAction<Dimension> externalSlideNotesSizeChangedAction;

	private Action externalMessagesClosedAction;

	private ConsumerAction<ExternalWindowPosition> externalParticipantsPositionChangedAction;

	private ConsumerAction<Dimension> externalParticipantsSizeChangedAction;

	private Action externalParticipantsClosedAction;

	private ConsumerAction<ExternalWindowPosition> externalParticipantVideoPositionChangedAction;

	private ConsumerAction<Dimension> externalParticipantVideoSizeChangedAction;

	private Action externalParticipantVideoClosedAction;

	private ConsumerAction<ExternalWindowPosition> externalSlidePreviewPositionChangedAction;

	private ConsumerAction<Dimension> externalSlidePreviewSizeChangedAction;

	private Action externalSlidePreviewClosedAction;

	private ConsumerAction<ExternalWindowPosition> externalNotesPositionChangedAction;

	private Action externalNotesClosedAction;

	private ConsumerAction<ExternalWindowPosition> externalSlideNotesPositionChangedAction;

	private Action externalSlideNotesClosedAction;

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

	private SlideView slideNotesView;

	private MouseWheelListener mouseWheelListener;

	private StylusListener stylusListener;

	private BufferedImage peerViewImage;

	private Container peerViewContainer;

	private Box leftVbox;
	private Box rightVbox;

	private AdaptiveTabbedPane rightTabPane;

	private AdaptiveTabbedPane noneTabPane;

	private Container leftNoteSlideViewContainer;
	private Container rightNoteSlideViewContainer;

	private ParticipantList participantList;

	private SettingsTab messageTab;

	private JScrollPane messagesPane;

	private JScrollPane notesPane;

	private Box messageViewContainer;

	private Box notesViewContainer;

	private AdaptiveTabbedPane bottomTabPane;

	private JPanel participantsPanel;

	private JPanel participantVideoPanel;

	private JPanel messagesPanel;

	private Box messageSendPanel;

	private JTextField messageTextField;

	private JButton sendMessageButton;

	private AdaptiveTabbedPane leftTabPane;

	private ExternalFrame externalMessagesFrame;

	private ExternalFrame externalParticipantsFrame;

	private ExternalFrame externalParticipantVideoFrame;

	private ExternalFrame externalSlidePreviewFrame;

	private ExternalFrame externalNotesFrame;

	private ExternalFrame externalSlideNotesFrame;

	private final JScrollPane externalMessagesPane = new JScrollPane();

	private final JScrollPane externalNotesPane = new JScrollPane();

	private final JScrollPane externalParticipantsPane = new JScrollPane();

	private final JPanel externalParticipantVideoPane = new JPanel(new BorderLayout());

	private final AdaptiveTabbedPane externalSlidePreviewTabPane = new AdaptiveTabbedPane(SwingConstants.RIGHT);

	private Box externalPreviewBox;
	private JPanel externalNoteSlideViewContainer;

	private double oldDocSplitPaneDividerRatio = 0.15;

	private double oldNotesSplitPaneDividerRatio = 0.75;

	private double oldTabSplitPaneDividerRatio = 0.9;

	private JLabel messagesPlaceholder;

	private SlideViewConfiguration viewConfig;

	private MessageBarPosition messageBarPosition = MessageBarPosition.BOTTOM;

	private SlideNotesPosition slideNotesPosition = SlideNotesPosition.BOTTOM;

	private NoteSlidePosition noteSlidePosition = NoteSlidePosition.NONE;

	private ParticipantsPosition participantsPosition = ParticipantsPosition.LEFT;

	private ParticipantVideoPosition participantVideoPosition = ParticipantVideoPosition.LEFT;

	private SlidePreviewPosition previewPosition = SlidePreviewPosition.RIGHT;

	private String selectedSlideLabelText = "";


	@Inject
	SwingSlidesView(Dictionary dictionary,
					UserPrivilegeService userPrivilegeService) {
		super();

		this.dict = dictionary;
		this.userPrivilegeService = userPrivilegeService;
	}

	@Override
	public void setSlideViewConfig(SlideViewConfiguration viewConfig) {
		this.viewConfig = viewConfig;

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
					// Reload if a document has changed.
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
					// Reload if a document has changed.
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

			updateNoteSlideView(doc);
		});

		// Show embedded document notes, if available.
		if (doc.hasTextNotes()) {
			expandTextNotes();
		}
		else {
			collapseTextNotes();
		}
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
		if (nonNull(mouseListener)) {
			mouseListener.setPresentationParameter(parameter);
		}

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
	public void setSlideNotes(Page page, PresentationParameter parameter) {
		SwingUtils.invoke(() -> {
			slideNotesView.parameterChanged(page, parameter);
			slideNotesView.setPage(page);
		});
	}

	@Override
	public void setPageRenderer(RenderController pageRenderer) {
		this.pageRenderer = pageRenderer;

		slideView.setPageRenderer(pageRenderer);
		slideNotesView.setPageRenderer(pageRenderer);
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
	public void setScrollHandler(ScrollHandler handler) {
		if (nonNull(mouseWheelListener)) {
			removeMouseWheelListener(mouseWheelListener);
		}

		mouseWheelListener = e -> {
			handler.onScrollEvent(new ScrollHandler.ScrollEvent(e.getX(), e.getY(),
					e.getPreciseWheelRotation(), e.getPreciseWheelRotation()));
		};

		addMouseWheelListener(mouseWheelListener);
	}

	@Override
	public void createStylusInput(StylusHandler handler) {
		if (nonNull(mouseListener)) {
			// Detach mouse to avoid double input.
			slideView.removeMouseListener(mouseListener);
			slideView.removeMouseMotionListener(mouseListener);
		}

		if (isNull(stylusListener)) {
			stylusListener = new StylusListener(handler, slideView);
		}

		AwtStylusManager manager = AwtStylusManager.getInstance();
		manager.attachStylusListener(slideView, stylusListener);
	}

	@Override
	public void createMouseInput(ToolController toolController) {
		if (nonNull(stylusListener)) {
			// Detach stylus to avoid double input.
			AwtStylusManager manager = AwtStylusManager.getInstance();
			manager.detachStylusListener(slideView);
		}

		if (isNull(mouseListener)) {
			mouseListener = new MouseListener(slideView, toolController);
		}

		slideView.addMouseListener(mouseListener);
		slideView.addMouseMotionListener(mouseListener);
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
				setParticipantsTabEnabled(dict.get(PARTICIPANTS_LABEL_KEY), true);

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

				if (!messageBarPosition.name().equals(participantsPosition.name())) {
					setMessageBarTabEnabled(dict.get(MESSAGE_LABEL_KEY), true);
				}

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

			switch (slideNotesPosition){
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

			MessageView messageView = ViewUtil.createMessageView(MessageView.class, userInfo, message, dict);
			messageView.setMessage(message, userInfo);
			messageView.setOnDiscard(() -> {
				executeAction(discardMessageAction, messageView.getMessage());

				removeMessageView(messageView);
			});
			messageView.setOnCreateSlide(() -> {
				createMessageSlideAction.execute(messageView.getMessage());

				removeMessageView(messageView);
			});

			messageView.pack();

			addMessageView(messageView);
		});
	}

	@Override
	public void setMessengerMessageAsReply(MessengerMessage message, MessengerMessage messageToReplyTo) {
		SwingUtils.invoke(() -> {
			UserInfo userInfo = userPrivilegeService.getUserInfo();

			MessageAsReplyView messageView = ViewUtil.createMessageView(MessageAsReplyView.class, userInfo, message, dict);
			messageView.setMessage(message, userInfo);

			final String userToReplyTo = MessageUtil.evaluateSenderOfMessageToReplyTo(messageToReplyTo, userInfo, dict);
			messageView.setUserToReplyTo(userToReplyTo);

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
	public void setModifiedMessengerMessage(MessengerMessage modifiedMessage) {
		SwingUtils.invoke(() -> {
			final Optional<MessageView> toModify = findCorrespondingMessageView(modifiedMessage.getMessageId());

			if (toModify.isEmpty()){
				return;
			}

			UserInfo userInfo = userPrivilegeService.getUserInfo();

			toModify.get().setMessage(modifiedMessage, userInfo);
			toModify.get().setIsEdited();
		});
	}

	@Override
	public void removeMessengerMessage(String messageId) {
		final Optional<MessageView> toRemove = findCorrespondingMessageView(messageId);

		if(toRemove.isEmpty()) return;

		removeMessageView(toRemove.get());
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
	public void acceptSpeechRequest(SpeechBaseMessage message) {
		SwingUtils.invoke(() -> {
			removeSpeechRequestMessage(message);
		});
	}

	@Override
	public void removeSpeechRequest(SpeechBaseMessage message) {
		SwingUtils.invoke(() -> {
			removeSpeechRequestMessage(message);
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
	public void setOnBan(ConsumerAction<CourseParticipant> action) {
		participantList.setOnBan(action);
	}

	@Override
	public void addPeerView(PeerView peerView) {
		if (peerView instanceof JComponent swingPeerView) {
			peerViewContainer.add(swingPeerView);

			if (rightTabPane.getPaneTabCount() == 0 && !externalParticipantVideoFrame.isVisible()) {
				maximizeRightTabPane();
			}
		}
	}

	@Override
	public void removePeerView(PeerView peerView) {
		if (peerView instanceof JComponent swingPeerView) {
			peerViewContainer.remove(swingPeerView);
			peerViewContainer.revalidate();
			peerViewContainer.repaint();
		}
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
	public void setOnExternalParticipantVideoPositionChanged(ConsumerAction<ExternalWindowPosition> action) {
		this.externalParticipantVideoPositionChangedAction = action;
	}

	@Override
	public void setOnExternalParticipantVideoSizeChanged(ConsumerAction<Dimension> action) {
		this.externalParticipantVideoSizeChangedAction = action;
	}

	@Override
	public void setOnExternalParticipantVideoClosed(Action action) {
		externalParticipantVideoClosedAction = action;
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
	public void setOnExternalSlideNotesPositionChanged(ConsumerAction<ExternalWindowPosition> action) {
		this.externalSlideNotesPositionChangedAction = action;
	}

	@Override
	public void setOnExternalSlideNotesSizeChanged(ConsumerAction<Dimension> action) {
		this.externalSlideNotesSizeChangedAction = action;
	}

	@Override
	public void setOnExternalSlideNotesClosed(Action action) {
		externalSlideNotesClosedAction = action;
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
		if (externalParticipantsFrame.isVisible()) {
			return;
		}

		setParticipantsTabVisible(dict.get(PARTICIPANTS_LABEL_KEY), false);

		participantsPanel.remove(participantList);

		externalParticipantsPane.getViewport().add(participantList);

		externalParticipantsFrame.updatePosition(screen, position, size);
		externalParticipantsFrame.showBody();
		externalParticipantsFrame.setVisible(true);
	}

	@Override
	public void hideExternalParticipants() {
		if (!externalParticipantsFrame.isVisible()) {
			return;
		}

		externalParticipantsPane.getViewport().remove(participantList);

		externalParticipantsFrame.setVisible(false);

		participantsPanel.add(participantList);

		setParticipantsTabVisible(dict.get(PARTICIPANTS_LABEL_KEY), true);
	}

	@Override
	public void showExternalParticipantVideo(Screen screen, Point position, Dimension size) {
		if (externalParticipantVideoFrame.isVisible()) {
			return;
		}

		setParticipantVideoTabVisible(dict.get(PARTICIPANT_VIDEO_LABEL_KEY), false);

		participantVideoPanel.remove(peerViewContainer);

		externalParticipantVideoPane.add(peerViewContainer, BorderLayout.CENTER);

		externalParticipantVideoFrame.updatePosition(screen, position, size);
		externalParticipantVideoFrame.showBody();
		externalParticipantVideoFrame.setVisible(true);
	}

	@Override
	public void hideExternalParticipantVideo() {
		if (!externalParticipantVideoFrame.isVisible()) {
			return;
		}

		externalParticipantVideoPane.remove(peerViewContainer);

		externalParticipantVideoFrame.setVisible(false);

		participantVideoPanel.add(peerViewContainer);

		setParticipantVideoTabVisible(dict.get(PARTICIPANT_VIDEO_LABEL_KEY), true);
	}

	@Override
	public void showExternalSlidePreview(Screen screen, Point position, Dimension size) {
		if (externalSlidePreviewFrame.isVisible()) {
			return;
		}

		externalPreviewBox.removeAll();

		hideNoteSlide();

		final String prevSelected = selectedSlideLabelText;

		if (previewPosition == SlidePreviewPosition.LEFT) {
			externalSlidePreviewTabPane.addTabs(leftTabPane.removeTabsByType(AdaptiveTabType.SLIDE));

			externalPreviewBox.add(externalSlidePreviewTabPane);
		}
		else if (previewPosition == SlidePreviewPosition.RIGHT) {
			externalSlidePreviewTabPane.addTabs(rightTabPane.removeTabsByType(AdaptiveTabType.SLIDE));

			externalPreviewBox.add(externalSlidePreviewTabPane);
		}

		externalSlidePreviewTabPane.setPaneTabSelected(prevSelected);

		if (!externalSlideNotesFrame.isVisible() && noteSlidePosition == NoteSlidePosition.BELOW_SLIDE_PREVIEW) {
			externalPreviewBox.add(externalNoteSlideViewContainer);

			externalNoteSlideViewContainer.add(slideNotesView);
		}

		updateSlideNoteContainer(externalNoteSlideViewContainer);

		externalSlidePreviewFrame.updatePosition(screen, position, size);
		externalSlidePreviewFrame.showBody();
		externalSlidePreviewFrame.setVisible(true);
	}

	@Override
	public void hideExternalSlidePreview() {
		if (!externalSlidePreviewFrame.isVisible()) {
			return;
		}

		SwingUtilities.invokeLater(() -> {
			externalSlidePreviewFrame.hideBody();
			externalSlidePreviewFrame.setVisible(false);

			dockSlidePreview(previewPosition);

			if (externalSlideNotesFrame.isVisible()) {
				hideNoteSlide();
				return;
			}
			if (noteSlidePosition == NoteSlidePosition.BELOW_SLIDE_PREVIEW) {
				showNoteSlide(previewPosition);
			}
		});
	}

	private void dockSlidePreview(SlidePreviewPosition position) {
		final String prevSelected = selectedSlideLabelText;

		externalPreviewBox.removeAll();

		if (position == SlidePreviewPosition.LEFT) {
			leftTabPane.addTabsBefore(externalSlidePreviewTabPane.removeTabsByType(AdaptiveTabType.SLIDE),
					AdaptiveTabType.MESSAGE);
			leftTabPane.setPaneTabSelected(prevSelected);
		}
		else if (position == SlidePreviewPosition.RIGHT) {
			rightTabPane.addTabsBefore(externalSlidePreviewTabPane.removeTabsByType(AdaptiveTabType.SLIDE),
					AdaptiveTabType.MESSAGE);
			rightTabPane.setPaneTabSelected(prevSelected);
		}
	}

	private AdaptiveTabbedPane getSlidesTabPane() {
		if (externalSlidePreviewFrame.isVisible()) {
			return externalSlidePreviewTabPane;
		}

		if (Objects.requireNonNull(previewPosition) == SlidePreviewPosition.LEFT) {
			return leftTabPane;
		}
		return rightTabPane;
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
	public void showExternalSlideNotes(Screen screen, Point position, Dimension size) {
		if (externalSlideNotesFrame.isVisible()) {
			return;
		}

		hideNoteSlide();

		externalSlideNotesFrame.updatePosition(screen, position, size);
		externalSlideNotesFrame.showBody();
		externalSlideNotesFrame.setVisible(true);
		externalSlideNotesFrame.repaint(0, externalSlideNotesFrame.getX(), externalSlideNotesFrame.getY(),
				externalSlideNotesFrame.getWidth() + 1, externalSlideNotesFrame.getHeight());
	}

	@Override
	public void hideExternalSlideNotes() {
		if (!externalSlideNotesFrame.isVisible()) {
			return;
		}

		externalSlideNotesFrame.setVisible(false);

		if (noteSlidePosition == NoteSlidePosition.BELOW_SLIDE_PREVIEW) {
			showNoteSlide(previewPosition);
		}
	}

	@Override
	public void setMessageBarPosition(MessageBarPosition position) {
		if (externalMessagesFrame.isVisible()) {
			hideExternalMessages();
		}

		switch (position) {
			case BOTTOM -> showMessageBarBottom();
			case LEFT -> showMessageBarLeft();
			case RIGHT -> showMessageBarRight();
		}

		messageBarPosition = position;
	}

	@Override
	public void setParticipantsPosition(ParticipantsPosition position) {
		if (externalParticipantsFrame.isVisible()) {
			hideExternalParticipants();
		}

		switch (position) {
			case LEFT -> showParticipantsLeft();
			case RIGHT -> showParticipantsRight();
		}

		participantsPosition = position;
	}

	@Override
	public void setParticipantVideoPosition(ParticipantVideoPosition position) {
		if (externalParticipantVideoFrame.isVisible()) {
			hideExternalParticipantVideo();
		}

		switch (position) {
			case LEFT -> showParticipantVideoLeft();
			case RIGHT -> showParticipantVideoRight();
		}

		participantVideoPosition = position;
	}

	@Override
	public void setPreviewPosition(SlidePreviewPosition position) {
		SwingUtilities.invokeLater(() -> {
			if (externalSlidePreviewFrame.isVisible()) {
				externalSlidePreviewFrame.hideBody();
				externalSlidePreviewFrame.setVisible(false);

				dockSlidePreview(position);
			}

			switch (position) {
				case LEFT -> showPreviewLeft();
				case RIGHT -> showPreviewRight();
			}

			// The note slide position is dependent on the slide preview position.
			hideNoteSlide();

			previewPosition = position;

			if (!externalSlideNotesFrame.isVisible()) {
				showNoteSlide(position);
			}
		});
	}

	@Override
	public void setNotesPosition(SlideNotesPosition position) {
		if (externalNotesFrame.isVisible()) {
			hideExternalNotes();
		}

		switch (position) {
			case LEFT -> showNoteLeft();
			case BOTTOM -> showNoteBottom();
		}

		slideNotesPosition = position;
	}

	private void expandTabPane(AdaptiveTabbedPane tabbedPane, String tabLabel, Runnable maximizeFunc) {
		if (tabbedPane.getPaneSelectedIndex() != tabbedPane.getPaneTabIndex(tabLabel)) {
			return;
		}

		tabbedPane.setTabEnabled(tabLabel, true);

		maximizeFunc.run();
	}

	private void expandTextNotes() {
		switch (slideNotesPosition) {
			case LEFT -> {
				expandTabPane(leftTabPane, dict.get(NOTES_LABEL_KEY), this::maximizeLeftTabPane);
			}
			case RIGHT -> {
				expandTabPane(rightTabPane, dict.get(NOTES_LABEL_KEY), this::maximizeRightTabPane);
			}
			case BOTTOM -> {
				expandTabPane(bottomTabPane, dict.get(NOTES_LABEL_KEY), this::maximizeBottomTabPane);
			}
		}
	}

	private void collapseTextNotes() {
		switch (slideNotesPosition) {
			case LEFT -> {
				minimizeLeftTabPane();
			}
			case RIGHT -> {
				minimizeRightTabPane();
			}
			case BOTTOM -> {
				minimizeBottomTabPane();
			}
		}
	}

	private void showNoteBottom() {
		if (slideNotesPosition == SlideNotesPosition.BOTTOM) {
			return;
		}
		final boolean prevMinimized = isBottomTabPaneMinimized();

		bottomTabPane.addTabs(removeNotesBarTabs());

		if (prevMinimized) {
			minimizeBottomTabPane();
		}
	}

	private void showNoteLeft() {
		if (slideNotesPosition == SlideNotesPosition.LEFT) {
			return;
		}

		final boolean prevMinimized = isLeftTabPaneMinimized();
		leftTabPane.addTabs(removeNotesBarTabs());

		if (prevMinimized) {
			minimizeLeftTabPane();
		}
	}

	@Override
	public void setNoteSlidePosition(NoteSlidePosition position) {
		if (externalSlideNotesFrame.isVisible()) {
			externalSlideNotesFrame.hideBody();
			externalSlideNotesFrame.setVisible(false);
		}

		switch (position) {
			case BELOW_SLIDE_PREVIEW -> showNoteSlide(previewPosition);
			case NONE -> hideNoteSlide();
		}

		noteSlidePosition = position;
	}

	private void updateNoteSlideView(Document doc) {
		if (doc.hasNoteSlide()) {
			showNoteSlide(previewPosition);
		}
		else {
			hideNoteSlide();
		}
	}

	private void showNoteSlide(SlidePreviewPosition position) {
		if (externalSlidePreviewFrame.isVisible()) {
			externalNoteSlideViewContainer.add(slideNotesView);

			externalPreviewBox.add(externalNoteSlideViewContainer);
			externalPreviewBox.revalidate();
			externalPreviewBox.repaint();

			updateSlideNoteContainer(externalNoteSlideViewContainer);
			return;
		}

		if (position == SlidePreviewPosition.LEFT) {
			leftNoteSlideViewContainer.setVisible(true);
			leftNoteSlideViewContainer.removeAll();
			leftNoteSlideViewContainer.add(slideNotesView);

			updateSlideNoteContainer(leftNoteSlideViewContainer);
		}
		else if (position == SlidePreviewPosition.RIGHT) {
			rightNoteSlideViewContainer.setVisible(true);
			rightNoteSlideViewContainer.removeAll();
			rightNoteSlideViewContainer.add(slideNotesView);

			updateSlideNoteContainer(rightNoteSlideViewContainer);
		}
	}

	private void hideNoteSlide() {
		if (externalSlidePreviewFrame.isVisible()) {
			externalNoteSlideViewContainer.removeAll();

			externalPreviewBox.remove(externalNoteSlideViewContainer);
			externalPreviewBox.revalidate();
			externalPreviewBox.repaint();
			return;
		}

		if (previewPosition == SlidePreviewPosition.LEFT) {
			leftNoteSlideViewContainer.setVisible(false);
			leftNoteSlideViewContainer.removeAll();
		}
		else if (previewPosition == SlidePreviewPosition.RIGHT) {
			rightNoteSlideViewContainer.setVisible(false);
			rightNoteSlideViewContainer.removeAll();
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
		if (participantsPosition == ParticipantsPosition.LEFT) {
			return;
		}

		final boolean prevMinimized = isLeftTabPaneMinimized();
		leftTabPane.addTabs(removeParticipantsTabs());

		if (prevMinimized) {
			minimizeLeftTabPane();
		}
	}

	private void showParticipantsRight() {
		if (participantsPosition == ParticipantsPosition.RIGHT) {
			return;
		}

		final boolean prevMinimized = isRightTabPaneMinimized();
		rightTabPane.addTabs(removeParticipantsTabs());

		if (prevMinimized) {
			minimizeRightTabPane();
		}
	}

	private void showParticipantVideoLeft() {
		final boolean prevMinimized = isLeftTabPaneMinimized();
		leftTabPane.addTabs(removeParticipantVideoTabs());

		if (prevMinimized) {
			minimizeLeftTabPane();
		}
	}

	private void showParticipantVideoRight() {
		if (participantVideoPosition == ParticipantVideoPosition.RIGHT) {
			return;
		}

		final boolean prevMinimized = isRightTabPaneMinimized();
		rightTabPane.addTabs(removeParticipantVideoTabs());

		if (prevMinimized) {
			minimizeRightTabPane();
		}
	}

	private void showPreviewLeft() {
		if (previewPosition == SlidePreviewPosition.LEFT) {
			return;
		}

		leftTabPane.addTabs(removePreviewTabs(), 0);

		SwingUtilities.invokeLater(this::maximizeLeftTabPane);
	}

	private void showPreviewRight() {
		if (previewPosition == SlidePreviewPosition.RIGHT) {
			return;
		}

		rightTabPane.addTabs(removePreviewTabs(), 0);

		maximizeRightTabPane();
	}

	private void removeSpeechRequestMessage(SpeechBaseMessage message) {
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

	private void setParticipantsTabVisible(String labelText, boolean visible) {
		switch (participantsPosition) {
			case RIGHT -> setRightTabVisible(labelText, visible);
			case LEFT -> setLeftTabVisible(labelText, visible);
		}
	}

	private void setParticipantVideoTabVisible(String labelText, boolean visible) {
		switch (participantVideoPosition) {
			case RIGHT -> setRightTabVisible(labelText, visible);
			case LEFT -> setLeftTabVisible(labelText, visible);
		}
	}

	private void setNotesBarTabVisible(String labelText, boolean visible) {
		switch (slideNotesPosition) {
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

	private void setNoneTabVisible(String labelText, boolean visible) {
		setTabVisible(noneTabPane, labelText, visible, null);
	}

	private void setTabVisible(AdaptiveTabbedPane tabbedPane, String labelText,
							   boolean visible, Runnable minimizeFunc) {
		final int prevVisibleTabCount = tabbedPane.getPaneTabCount();

		tabbedPane.setTabVisible(labelText, visible);

		final int visibleTabCount = tabbedPane.getPaneTabCount();

		if (nonNull(minimizeFunc) && (visibleTabCount == 0 || (prevVisibleTabCount == 0 && visibleTabCount >= 1))) {
			minimizeFunc.run();
		}
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

	private void setParticipantsTabEnabled(String labelText, boolean enable) {
		switch (participantsPosition) {
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

	private List<AdaptiveTab> removeMessageBarTabs() {
		return removeTabs(AdaptiveTabType.MESSAGE, bottomTabPane, leftTabPane, rightTabPane);
	}

	private List<AdaptiveTab> removeNotesBarTabs() {
		return removeTabs(AdaptiveTabType.NOTES, bottomTabPane, leftTabPane);
	}

	private List<AdaptiveTab> removeParticipantsTabs() {
		return removeTabs(AdaptiveTabType.PARTICIPANTS, leftTabPane, rightTabPane);
	}

	private List<AdaptiveTab> removeParticipantVideoTabs() {
		return removeTabs(AdaptiveTabType.PARTICIPANT_VIDEO, leftTabPane, rightTabPane);
	}

	private List<AdaptiveTab> removePreviewTabs() {
		return removeTabs(AdaptiveTabType.SLIDE, leftTabPane, rightTabPane);
	}

	private List<AdaptiveTab> removeTabs(AdaptiveTabType tabType, AdaptiveTabbedPane... panes) {
		final ArrayList<AdaptiveTab> removedTabs = new ArrayList<>();

		for (AdaptiveTabbedPane pane : panes) {
			removedTabs.addAll(pane.removeTabsByType(tabType));
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
		// TODO: check video position
		if (!externalParticipantVideoFrame.isVisible()) {
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

	private void updateSlideNoteContainer(Container container) {
		SwingUtilities.invokeLater(() -> {
			Page page = getPage();
			if (isNull(page)) {
				return;
			}

			PageMetrics pageMetrics = page.getPageMetrics();
			Dimension size = container.getSize();

			int height = (int) pageMetrics.getHeight(size.width);
			var newSize = pageMetrics.convert(size.width, height);
			int width = (int) newSize.getWidth();
			height = (int) newSize.getHeight();

			container.setPreferredSize(new Dimension(width, height));
			container.setMinimumSize(new Dimension(0, height));
			container.revalidate();
			container.repaint();
		});
	}

	private void scrollPreview(int step) {
		final AdaptiveTabbedPane slidesTabPane = getSlidesTabPane();
		final Component selectedTab = slidesTabPane.getSelectedComponent();

		if (selectedTab instanceof ThumbPanel thumbnailPanel) {
			thumbnailPanel.scroll(step);
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

		slideNotesView.setViewType(ViewType.Slide_Notes);

		rightNoteSlideViewContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

		leftVbox.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateSlideNoteContainer(leftNoteSlideViewContainer);
			}
		});
		rightVbox.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateSlideNoteContainer(rightNoteSlideViewContainer);
			}
		});

		noneTabPane = new AdaptiveTabbedPane();

		peerViewContainer.setLayout(new VideoTileLayout(5));

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

		externalPreviewBox = Box.createVerticalBox();
		externalNoteSlideViewContainer = new JPanel(new BorderLayout());

		JComboBox<String> participantLayoutComboBox = new JComboBox<>(new String[] {
				"Gallery",
				"Speaker"
		});

		externalParticipantVideoPane.add(participantLayoutComboBox, BorderLayout.NORTH);

		externalMessagesFrame = createExternalFrame(dict.get(MESSAGE_LABEL_KEY), externalMessagesPane,
				dict.get(NO_MESSAGES_LABEL_KEY),
				new Dimension(500, 400),
				() -> executeAction(externalMessagesClosedAction),
				position -> executeAction(externalMessagesPositionChangedAction, position),
				size -> executeAction(externalMessagesSizeChangedAction, size));

		externalParticipantsFrame = createExternalFrame(dict.get(PARTICIPANTS_LABEL_KEY), externalParticipantsPane,
				dict.get(NO_PARTICIPANTS_LABEL_KEY),
				new Dimension(280, 600),
				() -> executeAction(externalParticipantsClosedAction),
				position -> executeAction(externalParticipantsPositionChangedAction, position),
				size -> executeAction(externalParticipantsSizeChangedAction, size));

		externalParticipantVideoFrame = createExternalFrame(dict.get(PARTICIPANT_VIDEO_LABEL_KEY), externalParticipantVideoPane,
				"", new Dimension(280, 600),
				() -> executeAction(externalParticipantVideoClosedAction),
				position -> executeAction(externalParticipantVideoPositionChangedAction, position),
				size -> executeAction(externalParticipantVideoSizeChangedAction, size));

		externalSlidePreviewFrame = createExternalFrame(dict.get(SLIDES_PREVIEW_LABEL_KEY), externalPreviewBox,
				"", new Dimension(300, 700),
				() -> executeAction(externalSlidePreviewClosedAction),
				position -> executeAction(externalSlidePreviewPositionChangedAction, position),
				size -> executeAction(externalSlidePreviewSizeChangedAction, size));

		externalNotesFrame = createExternalFrame(dict.get(NOTES_LABEL_KEY), externalNotesPane, "",
				new Dimension(500, 400),
				() -> executeAction(externalNotesClosedAction),
				position -> executeAction(externalNotesPositionChangedAction, position),
				size -> executeAction(externalNotesSizeChangedAction, size));

		externalSlideNotesFrame = createExternalFrame(dict.get(SLIDE_NOTES_LABEL_KEY), slideNotesView, "",
				new Dimension(500, 400),
				() -> executeAction(externalSlideNotesClosedAction),
				position -> executeAction(externalSlideNotesPositionChangedAction, position),
				size -> executeAction(externalSlideNotesSizeChangedAction, size));

		slideNotesView.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				centerSlideNotesView();
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
			}
		});

		tabSplitPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				tabSplitPane.removeComponentListener(this);

				Page page = getPage();
				if (nonNull(page)) {
					tabSplitPane.setDividerLocation(viewConfig.getRightSliderPosition());
				}
			}
		});

		notesSplitPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				notesSplitPane.removeComponentListener(this);

				// Show embedded document notes, if available.
				Page page = getPage();
				if (nonNull(page)) {
					if (page.getDocument().hasTextNotes()) {
						expandTextNotes();
					}
					else {
						collapseTextNotes();
					}
				}
			}
		});
		showMessagesPlaceholder();
	}

	private ExternalFrame createExternalFrame(String name, Component body, String placeholderText,
											  Dimension minimumSize, Runnable closedAction,
											  Consumer<ExternalWindowPosition> positionChangedAction,
											  Consumer<Dimension> sizeChangedAction) {
		ExternalFrame frame = new ExternalFrame(name, body, placeholderText);
//		frame.setSize(minimumSize);
		frame.setClosedAction(closedAction);
		frame.setPositionChangedAction(positionChangedAction);
		frame.setSizeChangedAction(sizeChangedAction);

		return frame;
	}

	private void centerSlideNotesView() {
		SwingUtilities.invokeLater(() -> {
			if (externalSlideNotesFrame.isVisible()){
				Dimension size = slideNotesView.getSize();
				Dimension viewSize = slideNotesView.getCanvasBounds().getSize();

				int x = (size.width - viewSize.width) / 2;
				int y = (size.height - viewSize.height) / 2;

				slideNotesView.setSlideLocation(x, y);
				slideNotesView.repaint();
			}
			else {
				slideNotesView.setSlideLocation(0, 0);
				slideNotesView.repaint();
			}
		});
	}

	private void observeDividerLocation(final JSplitPane pane,
										final DoubleProperty property) {
		BasicSplitPaneUI ui = (BasicSplitPaneUI) pane.getUI();
		BasicSplitPaneDivider divider = ui.getDivider();

		divider.addMouseMotionListener(new MouseAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (pane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
					property.set(pane.getDividerLocation() / (double) (pane.getWidth() - pane.getDividerSize()));
				}
				else {
					property.set(pane.getDividerLocation() / (double) pane.getHeight());
				}
			}
		});
	}

	private void addNoteView(Component view){
//		setNotesBarTabVisible(dict.get(NOTES_LABEL_KEY), true);

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

	private Optional<MessageView> findCorrespondingMessageView(final String messageId) {
		for(Component component : messageViewContainer.getComponents()) {
			if((component instanceof MessageView messageView) &&
					messageView.getMessageId().equals(messageId)) {
				return Optional.of(messageView);
			}
		}
		return Optional.empty();
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
