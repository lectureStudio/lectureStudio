package org.lecturestudio.swing.list;

import static java.util.Objects.nonNull;

import java.awt.Component;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.lecturestudio.swing.AwtResourceLoader;
import org.lecturestudio.web.api.stream.model.CourseParticipant;
import org.lecturestudio.web.api.stream.model.CourseParticipantType;
import org.lecturestudio.web.api.stream.model.CoursePresenceType;

public class ParticipantCellRenderer extends Box implements ListCellRenderer<CourseParticipant> {

	private static final Border BORDER = new EmptyBorder(5, 5, 5, 5);

	private static final Icon ICON_ORGANISATOR = AwtResourceLoader.getIcon("host-indicator.svg", 20);
	private static final Icon ICON_CO_ORGANISATOR = AwtResourceLoader.getIcon("co-host-indicator.svg", 20);
	private static final Icon ICON_STREAM = AwtResourceLoader.getIcon("stream-indicator.svg", 20);
	private static final Icon ICON_CLASSROOM = AwtResourceLoader.getIcon("classroom-indicator.svg", 20);

	private final ResourceBundle bundle;

	private final JLabel nameLabel;
	private final JLabel typeLabel;
	private final JLabel streamLabel;
	private final JLabel classroomLabel;


	public ParticipantCellRenderer(ResourceBundle bundle) {
		super(BoxLayout.X_AXIS);

		this.bundle = bundle;
		this.nameLabel = new JLabel();
		this.typeLabel = new JLabel();
		this.streamLabel = new JLabel(ICON_STREAM);
		this.classroomLabel = new JLabel(ICON_CLASSROOM);

		streamLabel.setToolTipText(bundle.getString("presence.type.stream"));
		classroomLabel.setToolTipText(bundle.getString("presence.type.classroom"));

		setBorder(BORDER);
		setOpaque(true);

		add(nameLabel);
		add(Box.createHorizontalGlue());
		add(typeLabel);
		add(Box.createHorizontalStrut(3));
	}

	@Override
	public Component getListCellRendererComponent(
			JList<? extends CourseParticipant> list,
			CourseParticipant participant, int index, boolean isSelected,
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

		remove(streamLabel);
		remove(classroomLabel);

		if (nonNull(participant)) {
			CourseParticipantType partType = participant.getParticipantType();
			CoursePresenceType presenceType = participant.getPresenceType();

			switch (partType) {
				case ORGANISATOR -> {
					typeLabel.setIcon(ICON_ORGANISATOR);
					typeLabel.setToolTipText(bundle.getString("role.organisator"));
				}
				case CO_ORGANISATOR -> {
					typeLabel.setIcon(ICON_CO_ORGANISATOR);
					typeLabel.setToolTipText(bundle.getString("role.co-organisator"));
				}
				case PARTICIPANT -> {
					typeLabel.setIcon(null);
					typeLabel.setToolTipText(bundle.getString("role.participant"));
				}
			}

			nameLabel.setText(String.format("%s %s",
					participant.getFirstName(),	participant.getFamilyName()));

			if (presenceType == CoursePresenceType.STREAM) {
				add(streamLabel);
			}
			else if (presenceType == CoursePresenceType.CLASSROOM) {
				add(classroomLabel);
			}
		}
		else {
			nameLabel.setText("");
		}

		return this;
	}
}
