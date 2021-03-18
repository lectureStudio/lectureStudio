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

package org.lecturestudio.presenter.javafx.factory;

import javafx.beans.NamedArg;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import org.lecturestudio.javafx.control.SvgIcon;
import org.lecturestudio.presenter.javafx.view.model.QuizTableItem;
import org.lecturestudio.web.api.model.quiz.Quiz.QuizSet;

public class QuizSetCellFactory implements Callback<TableColumn<QuizTableItem, QuizSet>, TableCell<QuizTableItem, QuizSet>> {

	/** Generic set indicator. */
	private final String genericStyle;

	/** Document set indicator. */
	private final String documentStyle;


	public QuizSetCellFactory(@NamedArg("genericStyle") String genericStyle, @NamedArg("documentStyle") String documentStyle) {
		this.genericStyle = genericStyle;
		this.documentStyle = documentStyle;
	}

	@Override
	public TableCell<QuizTableItem, QuizSet> call(TableColumn<QuizTableItem, QuizSet> param) {
		return new ButtonCell();
	}



	private class ButtonCell extends TableCell<QuizTableItem, QuizSet> {

		@Override
		protected void updateItem(QuizSet item, boolean empty) {
			super.updateItem(item, empty);

			if (!empty) {
				SvgIcon icon = new SvgIcon();

				if (item == QuizSet.GENERIC) {
					icon.getStyleClass().add(genericStyle);
				}
				else if (item == QuizSet.DOCUMENT_SPECIFIC) {
					icon.getStyleClass().add(documentStyle);
				}

				setGraphic(icon);
			}
			else {
				setGraphic(null);
			}
		}

	}

}
