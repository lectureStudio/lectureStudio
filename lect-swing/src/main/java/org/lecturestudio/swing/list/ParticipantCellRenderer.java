package org.lecturestudio.swing.list;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.Component;
import java.util.ResourceBundle;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.lecturestudio.web.api.stream.model.CourseParticipant;
import org.lecturestudio.web.api.stream.model.CourseParticipantType;

public class ParticipantCellRenderer extends DefaultListCellRenderer {

	private static final Border BORDER = new EmptyBorder(5, 5, 5, 5);

	private final ResourceBundle bundle;


	public ParticipantCellRenderer(ResourceBundle bundle) {
		this.bundle = bundle;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
				index, isSelected, cellHasFocus);

		CourseParticipant participant = (CourseParticipant) value;

		if (nonNull(participant)) {
			CourseParticipantType pType = participant.getParticipantType();
			String pTypeStr = null;

			switch (pType) {
				case ORGANISATOR -> pTypeStr = bundle.getString("role.organisator");
				case CO_ORGANISATOR -> pTypeStr = bundle.getString("role.co-organisator");
			}

			if (isNull(pTypeStr)) {
				label.setText(String.format("%s %s",
						participant.getFirstName(),	participant.getFamilyName()));
			}
			else {
				label.setText(String.format("%s %s (%s)",
						participant.getFirstName(),	participant.getFamilyName(),
						pTypeStr));
			}
		}
		else {
			label.setText("");
		}

		label.setBorder(BORDER);

		return label;
	}

}
