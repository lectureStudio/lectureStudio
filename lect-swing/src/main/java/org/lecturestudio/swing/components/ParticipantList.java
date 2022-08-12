package org.lecturestudio.swing.components;

import java.awt.BorderLayout;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;

import org.lecturestudio.swing.list.ParticipantCellRenderer;
import org.lecturestudio.web.api.stream.model.CourseParticipant;

public class ParticipantList extends JPanel {

	private final DefaultListModel<CourseParticipant> listModel;


	@Inject
	public ParticipantList(ResourceBundle bundle) {
		super();

		setLayout(new BorderLayout());
		setFocusable(false);
		setIgnoreRepaint(true);

		listModel = new DefaultListModel<>();

		JList<CourseParticipant> list = new JList<>(listModel);
		list.setCellRenderer(new ParticipantCellRenderer(bundle));
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

	public void addParticipant(CourseParticipant participant) {
		listModel.addElement(participant);
	}

	public void removeParticipant(CourseParticipant participant) {
		listModel.removeElement(participant);
	}
}
