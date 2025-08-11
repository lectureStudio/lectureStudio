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

package org.lecturestudio.presenter.swing.view.model;

import javax.swing.Icon;
import javax.swing.table.TableColumnModel;

import org.jsoup.Jsoup;

import org.lecturestudio.swing.AwtResourceLoader;
import org.lecturestudio.swing.table.TableModelBase;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.model.quiz.Quiz.QuizSet;
import org.lecturestudio.web.api.model.quiz.Quiz.QuizType;

public class QuizTableModel extends TableModelBase<Quiz> {

	private static final Icon DOC_TYPE = AwtResourceLoader.getIcon("doc-type.svg", 20);

	private static final Icon MULTIPLE_TYPE = AwtResourceLoader.getIcon("multiple-type.svg", 20);

	private static final Icon SINGLE_TYPE = AwtResourceLoader.getIcon("single-type.svg", 20);

	private static final Icon NUMERIC_TYPE = AwtResourceLoader.getIcon("numeric-type.svg", 20);

	private static final Icon FREE_TEXT_TYPE = AwtResourceLoader.getIcon("free-text-type.svg", 20);


	public QuizTableModel(TableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Quiz quiz = getItem(rowIndex);

		switch (columnIndex) {
			case 0:
				return getSetIcon(quiz.getQuizSet());

			case 1:
				return Jsoup.parse(quiz.getQuestion()).text();

			case 2:
				return getTypeIcon(quiz.getType());
		}

		return null;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return col > 2;
	}

	@Override
	public int getRow(Quiz item) {
		for (int i = 0; i < getRowCount(); i++) {
			if (item.equals(getItem(i))) {
				return i;
			}
		}

		return -1;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return Icon.class;
		}

		return super.getColumnClass(columnIndex);
	}

	private static Icon getSetIcon(QuizSet set) {
		if (set == QuizSet.DOCUMENT_SPECIFIC) {
			return DOC_TYPE;
		}
		return null;
	}

	private static Icon getTypeIcon(QuizType type) {
		return switch (type) {
			case MULTIPLE -> MULTIPLE_TYPE;
			case NUMERIC -> NUMERIC_TYPE;
			case SINGLE -> SINGLE_TYPE;
			case FREE_TEXT -> FREE_TEXT_TYPE;
		};
	}
}
