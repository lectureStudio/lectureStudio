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

import static java.util.Objects.nonNull;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.geometry.Position;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.presenter.api.view.WebServiceSettingsView;
import org.lecturestudio.presenter.swing.view.model.QuizRegexTableModel;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;
import org.lecturestudio.web.api.filter.RegexRule;

@SwingView(name = "webservice-settings", presenter = org.lecturestudio.presenter.api.presenter.WebServiceSettingsPresenter.class)
public class SwingWebServiceSettingsView extends JPanel implements WebServiceSettingsView {

	private final ObjectProperty<Position> addressPosition;

	private JTextField lectureTitleTextField;

	private JTextField lectureTitleShortTextField;

	private JTable quizRegexTable;

	private Container ipPosContainer;

	private ButtonGroup ipPositionGroup;

	private JButton addQuizRegexButton;

	private JButton closeButton;

	private JButton resetButton;

	private ConsumerAction<RegexRule> deleteQuizRegexAction;

	public javax.swing.Action deleteAction = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			int row = Integer.parseInt(e.getActionCommand());
			QuizRegexTableModel model = (QuizRegexTableModel) quizRegexTable.getModel();
			RegexRule regexRule = model.getItem(row);

			executeAction(deleteQuizRegexAction, regexRule);
		}
	};


	public SwingWebServiceSettingsView() {
		super();

		addressPosition = new ObjectProperty<>();
	}

	@Override
	public void setClassroomName(StringProperty name) {
		SwingUtils.bindBidirectional(lectureTitleTextField, name);
	}

	@Override
	public void setClassroomShortName(StringProperty shortName) {
		SwingUtils.bindBidirectional(lectureTitleShortTextField, shortName);
	}

	@Override
	public void setOnAddQuizRegex(Action action) {
		SwingUtils.bindAction(addQuizRegexButton, action);
	}

	@Override
	public void setOnDeleteQuizRegex(ConsumerAction<RegexRule> action) {
		this.deleteQuizRegexAction = action;
	}

	@Override
	public void setQuizRegexRules(List<RegexRule> regexRules) {
		SwingUtils.invoke(() -> {
			QuizRegexTableModel model = (QuizRegexTableModel) quizRegexTable.getModel();
			model.setItems(regexRules);
		});
	}

	@Override
	public void setDisplayIpPosition(ObjectProperty<Position> position) {
		// Observe config property.
		addressPosition.addListener((observable, oldValue, newValue) -> {
			ipPositionGroup.setSelected(getIpPosToggle(addressPosition.get()).getModel(), true);
		});

		SwingUtils.bindBidirectional(position, addressPosition);
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		SwingUtils.bindAction(resetButton, action);
	}

	private AbstractButton getIpPosToggle(Position pos) {
		Enumeration<AbstractButton> buttons = ipPositionGroup.getElements();

		while (buttons.hasMoreElements()) {
			AbstractButton button = buttons.nextElement();
			String posName = button.getName();

			if (nonNull(posName) && Position.valueOf(posName) == pos) {
				return button;
			}
		}

		return ipPositionGroup.getElements().nextElement();
	}

	@ViewPostConstruct
	private void initialize() {
		quizRegexTable.setModel(new QuizRegexTableModel(quizRegexTable.getColumnModel()));

		// Observe the ButtonGroup.
		ActionListener listener = e -> {
			AbstractButton button = (AbstractButton) e.getSource();
			addressPosition.set(Position.valueOf(button.getName()));
		};

		ipPositionGroup = new ButtonGroup();

		for (Component component : ipPosContainer.getComponents()) {
			AbstractButton button = (AbstractButton) component;
			button.addActionListener(listener);

			ipPositionGroup.add(button);
		}
	}
}
