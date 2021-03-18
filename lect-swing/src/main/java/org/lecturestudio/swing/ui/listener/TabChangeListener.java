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

package org.lecturestudio.swing.ui.listener;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.bus.event.SelectDocumentEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.swing.components.ThumbnailPanel;
import org.lecturestudio.swing.window.AbstractWindow;

public class TabChangeListener implements ChangeListener {

	private final AbstractWindow window;


	public TabChangeListener(AbstractWindow window) {
		this.window = window;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
		ThumbnailPanel thumbnailPanel = (ThumbnailPanel) tabbedPane
				.getSelectedComponent();

		if (thumbnailPanel != null) {
			Document document = thumbnailPanel.getDocument();
			Document current = window.getDocument();

			if (current != null && !current.equals(document)) {
				ApplicationBus.post(new SelectDocumentEvent(document));
			}
		}
	}

}
