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

package org.lecturestudio.presenter.swing.view;

import java.awt.FlowLayout;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.presenter.api.presenter.DocumentTemplateSettingsPresenter;
import org.lecturestudio.presenter.api.view.DocumentTemplateSettingsView;
import org.lecturestudio.swing.components.DocumentPreview;
import org.lecturestudio.swing.layout.WrapFlowLayout;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

@SwingView(name = "template-settings", presenter = DocumentTemplateSettingsPresenter.class)
public class SwingDocumentTemplateSettingsView extends JPanel implements DocumentTemplateSettingsView {

	private final RenderController renderer;

	private JPanel container;

	private DocumentPreview chatMessagePreview;

	private DocumentPreview hallMessagePreview;

	private DocumentPreview quizPreview;

	private DocumentPreview whiteboardPreview;

	private JButton selectChatMessagePathButton;

	private JButton selectHallMessagePathButton;

	private JButton selectQuizPathButton;

	private JButton selectWhiteboardPathButton;

	private JButton closeButton;

	private JButton resetButton;


	@Inject
	SwingDocumentTemplateSettingsView(RenderController renderer) {
		super();

		this.renderer = renderer;
	}

	@Override
	public void setChatMessagePage(Page page, PresentationParameter parameter) {
		chatMessagePreview.setPage(page, parameter);
	}

	@Override
	public void setHallMessagePage(Page page, PresentationParameter parameter) {
		hallMessagePreview.setPage(page, parameter);
	}

	@Override
	public void setQuizPage(Page page, PresentationParameter parameter) {
		quizPreview.setPage(page, parameter);
	}

	@Override
	public void setWhiteboardPage(Page page, PresentationParameter parameter) {
		whiteboardPreview.setPage(page, parameter);
	}

	@Override
	public void bindChatMessageBounds(ObjectProperty<Rectangle2D> bounds) {
		chatMessagePreview.setOverlayBounds(bounds);
	}

	@Override
	public void bindHallMessageBounds(ObjectProperty<Rectangle2D> bounds) {
		hallMessagePreview.setOverlayBounds(bounds);
	}

	@Override
	public void bindQuizBounds(ObjectProperty<Rectangle2D> bounds) {
		quizPreview.setOverlayBounds(bounds);
	}

	@Override
	public void setOnSelectChatMessageTemplatePath(Action action) {
		SwingUtils.bindAction(selectChatMessagePathButton, action);
	}

	@Override
	public void setOnSelectHallMessageTemplatePath(Action action) {
		SwingUtils.bindAction(selectHallMessagePathButton, action);
	}

	@Override
	public void setOnSelectQuizTemplatePath(Action action) {
		SwingUtils.bindAction(selectQuizPathButton, action);
	}

	@Override
	public void setOnSelectWhiteboardTemplatePath(Action action) {
		SwingUtils.bindAction(selectWhiteboardPathButton, action);
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		SwingUtils.bindAction(resetButton, action);
	}

	@ViewPostConstruct
	private void initialize() {
		chatMessagePreview.setRenderController(renderer);
		hallMessagePreview.setRenderController(renderer);
		quizPreview.setRenderController(renderer);
		whiteboardPreview.setRenderController(renderer);
		whiteboardPreview.showOverlay(false);

		container.setLayout(new WrapFlowLayout(FlowLayout.LEFT));
	}
}
