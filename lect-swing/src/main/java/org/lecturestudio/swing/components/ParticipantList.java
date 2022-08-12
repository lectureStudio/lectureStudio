package org.lecturestudio.swing.components;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;

import org.lecturestudio.swing.list.ParticipantCellRenderer;
import org.lecturestudio.web.api.stream.model.CourseParticipant;

public class ParticipantList extends JPanel {

	private final SortedListModel listModel;


	@Inject
	public ParticipantList(ResourceBundle bundle) {
		super();

		setLayout(new BorderLayout());
		setFocusable(false);
		setIgnoreRepaint(true);

		listModel = new SortedListModel();

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
		listModel.add(participant);
	}

	public void removeParticipant(CourseParticipant participant) {
		listModel.remove(participant);
	}



	private static class SortedListModel extends AbstractListModel<CourseParticipant> {

		private final TreeSet<CourseParticipant> model;


		public SortedListModel() {
			model = new TreeSet<>(this::compare);
		}

		@Override
		public int getSize() {
			return model.size();
		}

		@Override
		public CourseParticipant getElementAt(int index) {
			return model.toArray(new CourseParticipant[0])[index];
		}

		public void add(CourseParticipant participant) {
			if (model.contains(participant)) {
				CourseParticipant ceil = model.ceiling(participant);
				CourseParticipant floor = model.floor(participant);

				System.out.println(ceil + " -> " + floor);
			}
			else if (model.add(participant)) {
				fireContentsChanged(this, 0, getSize());
			}
		}

		public void addAll(CourseParticipant[] participants) {
			model.addAll(Arrays.asList(participants));

			fireContentsChanged(this, 0, getSize());
		}

		public boolean remove(CourseParticipant participant) {
			boolean removed = model.remove(participant);

			if (removed) {
				fireContentsChanged(this, 0, getSize());
			}

			return removed;
		}

		public void clear() {
			model.clear();

			fireContentsChanged(this, 0, getSize());
		}

		public boolean contains(CourseParticipant element) {
			return model.contains(element);
		}

		private int compare(CourseParticipant lhs, CourseParticipant rhs) {
			int result = lhs.getParticipantType().compareTo(rhs.getParticipantType());
			if (result != 0) {
				return result;
			}

			result = lhs.getFirstName().compareToIgnoreCase(rhs.getFirstName());
			if (result != 0) {
				return result;
			}

			result = lhs.getFamilyName().compareToIgnoreCase(rhs.getFamilyName());
			if (result != 0) {
				return result;
			}

			return lhs.getPresenceType().compareTo(rhs.getPresenceType());
		}
	}
}
