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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.ViewType;

public class DocumentPreview extends JPanel {

	private SlideView slideView;


	public DocumentPreview() {
		super();

		initialize();
	}

	public void setPage(Page page, PresentationParameter parameter) {
		slideView.parameterChanged(page, parameter);
		slideView.setPage(page);
		slideView.renderPage();
	}

	public void setRenderController(RenderController renderer) {
		slideView.setPageRenderer(renderer);
	}

	private void initialize() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		slideView = new SlideView();
		slideView.setViewType(ViewType.User);

		add(slideView);
	}
}
