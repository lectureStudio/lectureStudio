package org.lecturestudio.swing.list;

import static java.util.Objects.nonNull;

import java.awt.Component;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.lecturestudio.swing.AwtResourceLoader;
import org.lecturestudio.swing.components.ParticipantList.CourseParticipantItem;
import org.lecturestudio.web.api.message.SpeechBaseMessage;
import org.lecturestudio.web.api.stream.model.CourseParticipantType;
import org.lecturestudio.web.api.stream.model.CoursePresenceType;

public class ParticipantCellRenderer extends Box implements ListCellRenderer<CourseParticipantItem> {

	private static final Border BORDER = new EmptyBorder(5, 5, 5, 5);

	private static final Icon ICON_ORGANISATOR = AwtResourceLoader.getIcon("host-indicator.svg", 20);
	private static final Icon ICON_CO_ORGANISATOR = AwtResourceLoader.getIcon("co-host-indicator.svg", 20);
	private static final Icon ICON_STREAM = AwtResourceLoader.getIcon("stream-indicator.svg", 20);
	private static final Icon ICON_CLASSROOM = AwtResourceLoader.getIcon("classroom-indicator.svg", 20);

	private static final Icon ICON_GUEST_SPEAKER = AwtResourceLoader.getIcon("guest-lecturer.svg", 15);

	private final ResourceBundle bundle;

	private final JLabel nameLabel;
	private final JLabel typeLabel;
	private final JLabel presenceTypeLabel;

	private final JButton acceptButton;
	private final JButton rejectButton;


	public ParticipantCellRenderer(ResourceBundle bundle) {
		super(BoxLayout.X_AXIS);

		this.bundle = bundle;
		this.nameLabel = new JLabel();
		this.typeLabel = new JLabel();
		this.presenceTypeLabel = new JLabel();

		acceptButton = new JButton(AwtResourceLoader.getIcon("speech-accept.svg", 14));
		rejectButton = new JButton(AwtResourceLoader.getIcon("speech-decline.svg", 14));

		acceptButton.setName("speech-accept");
		rejectButton.setName("speech-reject");

		setBorder(BORDER);
		setOpaque(true);

		add(nameLabel);
		add(Box.createHorizontalGlue());
		add(typeLabel);
		add(presenceTypeLabel);
		add(Box.createHorizontalStrut(3));
		add(acceptButton);
		add(Box.createHorizontalStrut(3));
		add(rejectButton);
	}

	@Override
	public Component getListCellRendererComponent(
			JList<? extends CourseParticipantItem> list,
			CourseParticipantItem participant, int index, boolean isSelected,
			boolean cellHasFocus) {
		setEnabled(list.isEnabled());
		setFont(list.getFont());

		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		}
		else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		if (nonNull(participant)) {
			final CourseParticipantType partType = participant.getParticipantType();
			final CoursePresenceType presenceType = participant.getPresenceType();
			final SpeechBaseMessage speechRequest = participant.speechRequest.get();

			String partTypeStr = "";
			String presenceTypeStr = "";

			acceptButton.setVisible(nonNull(speechRequest));
			rejectButton.setVisible(nonNull(speechRequest));

			switch (partType) {
				case ORGANISATOR -> {
					typeLabel.setIcon(ICON_ORGANISATOR);

					partTypeStr = bundle.getString("role.organisator");
				}
				case CO_ORGANISATOR -> {
					typeLabel.setIcon(ICON_CO_ORGANISATOR);

					partTypeStr = bundle.getString("role.co-organisator");
				}
				case PARTICIPANT -> {
					typeLabel.setIcon(null);

					partTypeStr = bundle.getString("role.participant");
				}
				case GUEST_LECTURER -> {
					typeLabel.setIcon(ICON_GUEST_SPEAKER);

					partTypeStr = bundle.getString("role.guest-lecturer");
				}
			}

			nameLabel.setText(String.format("%s %s",
					participant.getFirstName(),	participant.getFamilyName()));

			if (presenceType == CoursePresenceType.STREAM) {
				presenceTypeStr = bundle.getString("presence.type.stream");

				presenceTypeLabel.setIcon(ICON_STREAM);
				presenceTypeLabel.setVisible(true);
			}
			else if (presenceType == CoursePresenceType.CLASSROOM) {
				presenceTypeStr = bundle.getString("presence.type.classroom");

				presenceTypeLabel.setIcon(ICON_CLASSROOM);
				presenceTypeLabel.setVisible(true);
			}
			else {
				presenceTypeLabel.setVisible(false);
			}

			list.setToolTipText(String.format("%s (%s)",
					partTypeStr, presenceTypeStr));
		}
		else {
			nameLabel.setText("");

			list.setToolTipText(null);
		}

		return this;
	}
}
