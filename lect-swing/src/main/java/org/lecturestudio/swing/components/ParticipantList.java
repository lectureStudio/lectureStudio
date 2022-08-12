package org.lecturestudio.swing.components;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;

import org.lecturestudio.core.model.Participant;
import org.lecturestudio.swing.list.ParticipantCellRenderer;

public class ParticipantList extends JPanel {

	private final DefaultListModel<Participant> listModel;


	public ParticipantList() {
		super();

		setLayout(new BorderLayout());
		setFocusable(false);
		setIgnoreRepaint(true);

		listModel = new DefaultListModel<>();

		JList<Participant> list = new JList<>(listModel);
		list.setCellRenderer(new ParticipantCellRenderer());
		list.setLayoutOrientation(JList.VERTICAL);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setFocusable(false);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getViewport().add(list);

		add(scrollPane, BorderLayout.CENTER);
	}

	public void addParticipant(Participant participant) {
		listModel.addElement(participant);
	}

	public void removeParticipant(Participant participant) {
		listModel.removeElement(participant);
	}
}
