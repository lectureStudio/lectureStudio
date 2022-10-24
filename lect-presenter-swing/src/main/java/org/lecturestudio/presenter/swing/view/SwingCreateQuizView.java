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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.Component;
import java.awt.Container;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import net.atlanticbb.tantlinger.shef.HTMLEditorPane;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.presenter.api.view.CreateQuizOptionView;
import org.lecturestudio.presenter.api.view.CreateQuizView;
import org.lecturestudio.swing.components.ContentPane;
import org.lecturestudio.swing.input.KeyUtils;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;
import org.lecturestudio.web.api.model.quiz.Quiz.QuizType;

@SwingView(name = "create-quiz")
public class SwingCreateQuizView extends ContentPane implements CreateQuizView {

	private final ResourceBundle resources;

	private KeyboardFocusManager oldKFM;

	private ConsumerAction<QuizType> quizTypeAction;

	private Container toolbarContainer;

	private Container optionContainer;

	private HTMLEditorPane htmlEditor;

	private JComboBox<Document> docSetComboBox;

	private JRadioButton multipleTypeRadioButton;

	private JRadioButton singleTypeRadioButton;

	private JRadioButton numericTypeRadioButton;

	private String optionTooltip;

	private String lastOptionTooltip;

	private JButton newOptionButton;

	private JButton closeButton;

	private JButton saveQuizButton;

	private JButton saveAndNextQuizButton;

	private JButton startQuizButton;

	public javax.swing.Action typeAction = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			QuizType type = QuizType.valueOf(e.getActionCommand());

			executeAction(quizTypeAction, type);
		}
	};


	@Inject
	SwingCreateQuizView(ResourceBundle resources) {
		super();

		this.resources = resources;
	}

	@Override
	public void clearOptions() {
		SwingUtils.invoke(() -> {
			optionContainer.removeAll();
			optionContainer.revalidate();
			optionContainer.repaint();

			htmlEditor.requestFocus();
		});
	}

	@Override
	public void addQuizOptionView(CreateQuizOptionView optionView) {
		if (SwingUtils.isComponent(optionView)) {
			SwingUtils.invoke(() -> {
				optionContainer.add((Component) optionView);
				optionContainer.revalidate();

				setFieldTooltips();
			});
		}
	}

	@Override
	public void removeQuizOptionView(CreateQuizOptionView optionView) {
		if (SwingUtils.isComponent(optionView)) {
			SwingUtils.invoke(() -> {
				optionContainer.remove((Component) optionView);
				optionContainer.revalidate();

				setFieldTooltips();
			});
		}
	}

	@Override
	public void moveQuizOptionViewUp(CreateQuizOptionView optionView) {
		if (!SwingUtils.isComponent(optionView)) {
			return;
		}

		int index = -1;

		for (int i = 0; i < optionContainer.getComponentCount(); i++) {
			if (optionContainer.getComponent(i) == optionView) {
				index = i;
			}
		}

		int newIndex = index - 1;

		if (index < 0 || newIndex < 0) {
			return;
		}

		optionContainer.remove(index);
		optionContainer.add((Component) optionView, newIndex);
		optionContainer.revalidate();

		setFieldTooltips();
	}

	@Override
	public void moveQuizOptionViewDown(CreateQuizOptionView optionView) {
		if (!SwingUtils.isComponent(optionView)) {
			return;
		}

		int count = optionContainer.getComponentCount();
		int index = -1;

		for (int i = 0; i < count; i++) {
			if (optionContainer.getComponent(i) == optionView) {
				index = i;
			}
		}

		int newIndex = index + 1;

		if (index < 0 || newIndex >= count) {
			return;
		}

		optionContainer.remove(index);
		optionContainer.add((Component) optionView, newIndex);
		optionContainer.revalidate();

		setFieldTooltips();
	}

	@Override
	public String getQuizText() {
		return htmlEditor.getText();
	}

	@Override
	public void setQuizText(String text) {
		htmlEditor.setText(text);
	}

	@Override
	public void setDocuments(List<Document> documents) {
		SwingUtils.invoke(() -> docSetComboBox.setModel(
				new DefaultComboBoxModel<>(new Vector<>(documents))));
	}

	@Override
	public void setDocument(Document doc) {
		SwingUtils.invoke(() -> docSetComboBox.setSelectedItem(doc));
	}

	@Override
	public void setQuizType(QuizType type) {
		SwingUtils.invoke(() -> {
			switch (type) {
				case MULTIPLE -> multipleTypeRadioButton.setSelected(true);
				case NUMERIC -> numericTypeRadioButton.setSelected(true);
				case SINGLE -> singleTypeRadioButton.setSelected(true);
			}
		});
	}

	@Override
	public void setOnDocumentSelected(ConsumerAction<Document> action) {
		docSetComboBox.addItemListener(e -> {
			int stateChange = e.getStateChange();

			if (stateChange == ItemEvent.SELECTED) {
				executeAction(action, docSetComboBox.getModel()
						.getElementAt(docSetComboBox.getSelectedIndex()));
			}
		});
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnNewOption(Action action) {
		SwingUtils.bindAction(newOptionButton, action);
	}

	@Override
	public void setOnSaveQuiz(Action action) {
		SwingUtils.bindAction(saveQuizButton, action);
	}

	@Override
	public void setOnSaveAndNextQuiz(Action action) {
		SwingUtils.bindAction(saveAndNextQuizButton, action);
	}

	@Override
	public void setOnStartQuiz(Action action) {
		SwingUtils.bindAction(startQuizButton, action);
	}

	@Override
	public void setOnQuizType(ConsumerAction<QuizType> action) {
		quizTypeAction = action;
	}

	@ViewPostConstruct
	private void initialize() {
		traverseButtons(this);

		oldKFM = KeyboardFocusManager.getCurrentKeyboardFocusManager();

		// Use custom keyboard manager.
		addHierarchyListener(e -> {
			if (e.getChangeFlags() == HierarchyEvent.PARENT_CHANGED) {
				if (isNull(e.getComponent().getParent())) {
					KeyboardFocusManager.setCurrentKeyboardFocusManager(oldKFM);
				}
				else {
					KeyboardFocusManager.setCurrentKeyboardFocusManager(null);
					KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
					focusManager.addKeyEventDispatcher(new KeyboardManager());
				}
			}
		});

		toolbarContainer.add(htmlEditor.getFormatToolBar());

		optionTooltip = createMultilineTooltip(new LinkedHashMap<>() {
			{
				put(resources.getString("create.quiz.option.next.tooltip"),
						KeyUtils.getDisplayText(KeyStroke.getKeyStroke("TAB")));
				put(resources.getString("create.quiz.option.up.tooltip"),
						KeyUtils.getDisplayText(KeyStroke.getKeyStroke("UP")));
				put(resources.getString("create.quiz.option.down.tooltip"),
						KeyUtils.getDisplayText(KeyStroke.getKeyStroke("DOWN")));
			}
		});
		lastOptionTooltip = createMultilineTooltip(new LinkedHashMap<>() {
			{
				put(resources.getString("create.quiz.option.add.tooltip"),
						KeyUtils.getDisplayText(KeyStroke.getKeyStroke("TAB")));
				put(resources.getString("create.quiz.option.up.tooltip"),
						KeyUtils.getDisplayText(KeyStroke.getKeyStroke("UP")));
			}
		});
	}

	public static void traverseButtons(Container container) {
		if (container != null) {
			for (int i = 0; i < container.getComponentCount(); ++i) {
				Component c = container.getComponent(i);
				if (c instanceof JComponent) {
					JComponent cc = (JComponent) c;
					String ks = (String) cc.getClientProperty("KEY_STRING");

					if (nonNull(ks)) {
						String tooltip = cc.getToolTipText();

						cc.setToolTipText(nonNull(tooltip)
								? tooltip + " [" + ks + "]"
								: ks);
					}

					traverseButtons(cc);
				}
			}
		}
	}

	private String createMultilineTooltip(Map<String, String> data) {
		StringBuilder builder = new StringBuilder();
		builder.append("<html>");
		builder.append("<table>");

		for (Entry<String, String> entry : data.entrySet()) {
			builder.append("<tr>");
			builder.append("<td>");
			builder.append(entry.getKey());
			builder.append("</td>");
			builder.append("<td style=\"font-size:14px;\">");
			builder.append(entry.getValue());
			builder.append("</td>");
			builder.append("</tr>");
		}

		builder.append("</table>");
		builder.append("</html>");

		return builder.toString();
	}

	private void setFieldTooltips() {
		int count = optionContainer.getComponentCount();

		for (int i = 0; i < count; i++) {
			JComponent component = (JComponent) optionContainer.getComponent(i);

			updateOptionProperties(component, i, count);
		}
	}

	private void updateOptionProperties(JComponent component, int row, int rowCount) {
		if (component.getComponentCount() > 0) {
			int count = component.getComponentCount();

			for (int i = 0; i < count; i++) {
				JComponent c = (JComponent) component.getComponent(i);
				updateOptionProperties(c, row, rowCount);
			}
		}
		else {
			String name = component.getName();

			if (nonNull(name) && name.equalsIgnoreCase("last-input")) {
				if (row == rowCount - 1) {
					component.putClientProperty("option", "last");
					component.setToolTipText(lastOptionTooltip);
				}
				else {
					component.putClientProperty("option", null);
					component.setToolTipText(optionTooltip);
				}
			}
		}
	}



	private class KeyboardManager extends DefaultKeyboardFocusManager {

		@Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			Component source = event.getComponent();

			if (nonNull(source) && source.isShowing()) {
				boolean isTextComponent = source instanceof JTextComponent;
				boolean isAlphaNum = Character.isLetterOrDigit(event.getKeyCode()) |
						Character.isSpaceChar(event.getKeyCode());

				if (isTextComponent && isAlphaNum) {
					return !event.isControlDown() && !event.isShiftDown()
							&& !event.isAltDown() && !event.isMetaDown();
				}
			}

			if (event.getID() != KeyEvent.KEY_PRESSED) {
				return super.dispatchKeyEvent(event);
			}

			String name = (String) ((JComponent) source).getClientProperty("option");
			boolean lastField = nonNull(name) && name.equalsIgnoreCase("last");

			if (event.getKeyCode() == KeyEvent.VK_TAB && lastField) {
				newOptionButton.doClick();
				return true;
			}

			return super.dispatchKeyEvent(event);
		}
	}
}
