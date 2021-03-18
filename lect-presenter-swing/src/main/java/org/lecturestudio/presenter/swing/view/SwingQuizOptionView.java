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

package org.lecturestudio.presenter.swing.view;

import java.awt.Container;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.view.CreateQuizOptionView;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;

@SwingView(name = "quiz-option")
public abstract class SwingQuizOptionView extends JPanel implements CreateQuizOptionView {

	private Action moveUpAction;

	private Action moveDownAction;

	private Action enterKeyAction;

	private Action tabKeyAction;

	private Container buttons;

	private JButton removeButton;

	private JButton moveUpButton;

	private JButton moveDownButton;


	abstract void setOptionTooltip(String tooltip);


	public Container getButtons() {
		return buttons;
	}

	@Override
	public void setOnRemove(Action action) {
		SwingUtils.bindAction(removeButton, action);
	}

	@Override
	public void setOnMoveUp(Action action) {
		this.moveUpAction = action;

		SwingUtils.bindAction(moveUpButton, action);
	}

	@Override
	public void setOnMoveDown(Action action) {
		this.moveDownAction = action;

		SwingUtils.bindAction(moveDownButton, action);
	}

	@Override
	public void setOnEnterKey(Action action) {
		this.enterKeyAction = action;
	}

	@Override
	public void setOnTabKey(Action action) {
		this.tabKeyAction = action;
	}

	protected void upDownKeyHandler(KeyEvent event) {
		switch (event.getKeyCode()) {
			case KeyEvent.VK_UP:
				executeAction(moveUpAction);
				event.consume();
				break;

			case KeyEvent.VK_DOWN:
				executeAction(moveDownAction);
				event.consume();
				break;

			default:
				break;
		}
	}

	protected void tabKeyHandler(KeyEvent event) {
		if (event.isAltDown() || event.isControlDown() || event.isShiftDown()) {
			return;
		}
		if (event.getKeyCode() == KeyEvent.VK_TAB) {
			executeAction(tabKeyAction);
			event.consume();
		}
	}

	protected void enterKeyHandler(KeyEvent event) {
		if (event.isAltDown() || event.isControlDown() || event.isShiftDown()) {
			return;
		}
		if (event.getKeyCode() == KeyEvent.VK_ENTER) {
			executeAction(enterKeyAction);
			event.consume();
		}
	}

}
