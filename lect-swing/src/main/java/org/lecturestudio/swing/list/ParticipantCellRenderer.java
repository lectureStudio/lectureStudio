package org.lecturestudio.swing.list;

import static java.util.Objects.nonNull;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import org.lecturestudio.core.model.Participant;

public class ParticipantCellRenderer extends DefaultListCellRenderer {

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
				index, isSelected, cellHasFocus);

		Participant participant = (Participant) value;

		if (nonNull(participant)) {
			label.setText(String.format("%s %s", participant.getFirstName(),
					participant.getFamilyName()));
		}
		else {
			label.setText("");
		}

		return label;
	}

}
