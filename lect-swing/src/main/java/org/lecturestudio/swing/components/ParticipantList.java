/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.swing.components;

import static java.util.Objects.nonNull;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.swing.*;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.swing.list.ParticipantCellRenderer;
import org.lecturestudio.web.api.janus.JanusParticipantContext;
import org.lecturestudio.web.api.message.SpeechBaseMessage;
import org.lecturestudio.web.api.stream.model.CourseParticipant;
import org.lecturestudio.web.api.stream.model.CourseParticipantType;
import org.lecturestudio.web.api.stream.model.CoursePresenceType;

public class ParticipantList extends JPanel {

	private final SortedListModel listModel;

	private final Map<String, ConsumerAction<?>> actionMap;

	private final JPopupMenu popupMenu;

	private final JMenuItem popupMenuBanItem;

	private CourseParticipantItem popupMenuParticipant;


	@Inject
	public ParticipantList(ResourceBundle bundle) {
		super();

		setLayout(new BorderLayout());
		setFocusable(false);
		setIgnoreRepaint(true);

		listModel = new SortedListModel();
		actionMap = new HashMap<>();

		popupMenuBanItem = new JMenuItem("Ban");
		popupMenuBanItem.addActionListener(e -> {
			if (nonNull(popupMenuParticipant)) {
				var action = (ConsumerAction<CourseParticipant>) actionMap.get("ban-user");
				if (action == null) {
					return;
				}
				action.execute(popupMenuParticipant);
			}
		});

		popupMenu = new JPopupMenu();
		popupMenu.add(popupMenuBanItem);

		JList<CourseParticipantItem> list = new JList<>(listModel) {

			@Override
			public int locationToIndex(Point location) {
				int index = super.locationToIndex(location);
				return (index != -1 && !getCellBounds(index, index).contains(location)) ? -1 : index;
			}
		};
		list.setCellRenderer(new ParticipantCellRenderer(bundle));
		list.setLayoutOrientation(JList.VERTICAL);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setFocusable(false);

		MouseHandler mouseHandler = new MouseHandler(list);

		list.addMouseListener(mouseHandler);
		list.addMouseMotionListener(mouseHandler);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getViewport().add(list);

		add(scrollPane, BorderLayout.CENTER);
	}

	public void clear() {
		listModel.clear();
	}

	public void setParticipants(Collection<CourseParticipant> participants) {
		listModel.clear();
		listModel.addAll(participants.stream()
				.map(CourseParticipantItem::new)
				.toArray(CourseParticipantItem[]::new));
	}

	public void addParticipant(CourseParticipant participant) {
		listModel.add(new CourseParticipantItem(participant));
	}

	public void removeParticipant(CourseParticipant participant) {
		listModel.remove(new CourseParticipantItem(participant));
	}

	public void addSpeechRequest(SpeechBaseMessage request) {
		CourseParticipantItem found = listModel.getParticipantById(request.getUserId());

		if (nonNull(found)) {
			found.speechRequest.set(request);

			listModel.fireParticipantChanged(found);
		}
	}

	public void setSpeechRequestContext(JanusParticipantContext context) {
		CourseParticipantItem found = listModel.getParticipantById(context.getUserId());

		if (nonNull(found)) {
			found.participantContext.set(context);
			found.speechRequest.set(null);

			listModel.fireParticipantChanged(found);
		}
	}

	public void removeSpeechRequestContext(JanusParticipantContext context) {
		CourseParticipantItem found = listModel.getParticipantById(context.getUserId());

		if (nonNull(found)) {
			found.participantContext.set(null);

			listModel.fireParticipantChanged(found);
		}
	}

	public void removeSpeechRequest(SpeechBaseMessage request) {
		CourseParticipantItem found = listModel.getParticipantById(request.getUserId());

		if (nonNull(found)) {
			found.speechRequest.set(null);

			listModel.fireParticipantChanged(found);
		}
	}

	public void setOnAcceptSpeech(ConsumerAction<? extends SpeechBaseMessage> action) {
		actionMap.put("speech-accept", action);
	}

	public void setOnRejectSpeech(ConsumerAction<? extends SpeechBaseMessage> action) {
		actionMap.put("speech-reject", action);
	}

	public void setOnBan(ConsumerAction<CourseParticipant> action) {
		actionMap.put("ban-user", action);
	}



	private class MouseHandler extends MouseAdapter {

		private final JList<CourseParticipantItem> list;


		MouseHandler(JList<CourseParticipantItem> list) {
			this.list = list;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			JList<?> list = (JList<?>) e.getSource();
			if (list.locationToIndex(e.getPoint()) == -1) {
				list.clearSelection();
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			JList<?> list = (JList<?>) e.getSource();
			int index = list.locationToIndex(e.getPoint());

			if (index == -1 && !e.isShiftDown() && !e.isControlDown()) {
				list.clearSelection();
			}

			// Ensure the user is always reset
			popupMenuParticipant = null;

			if (index > -1) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					handleRightClick(e, index);
				}
				else if (e.getButton() == MouseEvent.BUTTON1) {
					handleButton(e.getPoint(), index);
				}
			}
		}

		private void handleRightClick(MouseEvent e, int index) {
			CourseParticipantItem value = listModel.getElementAt(index);
			if (value == null) {
				return;
			}
			if (value.getParticipantType() != CourseParticipantType.PARTICIPANT) {
				return;
			}

			popupMenuParticipant = value;
			popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}

		@SuppressWarnings("unchecked")
		private void handleButton(Point pt, int index) {
			CourseParticipantItem value = listModel.getElementAt(index);

			Component cell = list.getCellRenderer()
					.getListCellRendererComponent(list, value, index, false, false);
			cell.setBounds(list.getCellBounds(index, index));

			Rectangle r = cell.getBounds();
			pt.translate(-r.x, -r.y);

			Component child = cell.getComponentAt(pt);

			if (child instanceof JButton) {
				String actionName = child.getName();
				var action = actionMap.get(actionName);

				if (nonNull(action) && actionName.startsWith("speech-")) {
					ConsumerAction<SpeechBaseMessage> speechAction = (ConsumerAction<SpeechBaseMessage>) action;
					speechAction.execute(value.speechRequest.get());

					value.speechRequest.set(null);

					listModel.fireParticipantChanged(index);
				}
			}
		}
	}



	public static class CourseParticipantItem extends CourseParticipant {

		public ObjectProperty<JanusParticipantContext> participantContext = new ObjectProperty<>();

		public ObjectProperty<SpeechBaseMessage> speechRequest = new ObjectProperty<>();

		public CoursePresenceType secondaryPresenceType;


		public CourseParticipantItem(CourseParticipant participant) {
			super(participant.getUserId(), participant.getFirstName(),
					participant.getFamilyName(), participant.getPresenceType(),
					participant.getParticipantType());
		}
	}



	private static class SortedListModel extends AbstractListModel<CourseParticipantItem> {

		private final TreeSet<CourseParticipantItem> model;


		public SortedListModel() {
			model = new TreeSet<>(this::compare);
		}

		@Override
		public int getSize() {
			return model.size();
		}

		@Override
		public CourseParticipantItem getElementAt(int index) {
			return model.toArray(new CourseParticipantItem[0])[index];
		}

		public void fireParticipantChanged(int index) {
			if (index > -1) {
				fireContentsChanged(this, index, index);
			}
		}

		public void fireParticipantChanged(CourseParticipantItem participant) {
			int index = indexOf(participant);

			if (index > -1) {
				fireContentsChanged(this, index, index);
			}
		}

		public int indexOf(CourseParticipantItem participant) {
			return model.contains(participant)
					? model.headSet(participant).size()
					: -1;
		}

		public CourseParticipantItem getParticipantById(String id) {
			return model.stream().filter(p -> p.getUserId().equals(id))
					.findFirst().orElse(null);
		}

		public void add(CourseParticipantItem participant) {
			CourseParticipantItem found = getParticipantById(participant.getUserId());

			if (nonNull(found)) {
				// Stream presence takes precedence.
				if (CoursePresenceType.isStream(participant.getPresenceType())) {
					participant.secondaryPresenceType = found.getPresenceType();
					// Remove found participant and replace it with new presence.
					model.remove(found);
				}
			}

			if (model.add(participant)) {
				fireContentsChanged(this, 0, getSize());
			}
		}

		public void addAll(CourseParticipantItem[] participants) {
			model.addAll(Arrays.asList(participants));

			fireContentsChanged(this, 0, getSize());
		}

		public boolean remove(CourseParticipantItem participant) {
			CourseParticipantItem found = getParticipantById(participant.getUserId());

			if (nonNull(found)) {
				if (nonNull(found.secondaryPresenceType)) {
					// Multi-presence found.
					if (found.getPresenceType().equals(participant.getPresenceType())) {
						found.setPresenceType(found.secondaryPresenceType);
						found.setParticipantType(participant.getParticipantType());
					}

					found.secondaryPresenceType = null;

					fireContentsChanged(this, 0, getSize());

					return false;
				}

				return removeParticipant(found);
			}

			return removeParticipant(participant);
		}

		public void clear() {
			model.clear();

			fireContentsChanged(this, 0, getSize());
		}

		public boolean contains(CourseParticipantItem element) {
			return model.contains(element);
		}

		private boolean removeParticipant(CourseParticipantItem participant) {
			boolean removed = model.remove(participant);

			if (removed) {
				fireContentsChanged(this, 0, getSize());
			}

			return removed;
		}

		private int compare(CourseParticipantItem lhs, CourseParticipantItem rhs) {
			int result = lhs.getParticipantType().compareTo(rhs.getParticipantType());
			if (result != 0) {
				return result;
			}

			boolean lhsRequest = nonNull(lhs.speechRequest.get());
			boolean rhsRequest = nonNull(rhs.speechRequest.get());

			if (lhsRequest || rhsRequest) {
				if (lhsRequest && rhsRequest) {
					ZonedDateTime lhsDate = lhs.speechRequest.get().getDate();
					ZonedDateTime rhsDate = rhs.speechRequest.get().getDate();

					result = Objects.compare(lhsDate, rhsDate, ZonedDateTime::compareTo);
					if (result != 0) {
						return result;
					}
				}
				else if (lhsRequest) {
					return -1;
				}
				else {
					return 1;
				}
			}

			result = lhs.getFirstName().compareToIgnoreCase(rhs.getFirstName());
			if (result != 0) {
				return result;
			}

			result = lhs.getFamilyName().compareToIgnoreCase(rhs.getFamilyName());
			if (result != 0) {
				return result;
			}

			return Objects.compare(lhs.getPresenceType(), rhs.getPresenceType(),
					CoursePresenceType::compareTo);
		}
	}
}
