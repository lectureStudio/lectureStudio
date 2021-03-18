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

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import org.lecturestudio.presenter.javafx.view.model.QuizTableItem;

import org.jsoup.Jsoup;

public class QuizQuestionCellFactory implements Callback<TableColumn<QuizTableItem, String>, TableCell<QuizTableItem, String>> {

	@Override
	public TableCell<QuizTableItem, String> call(TableColumn<QuizTableItem, String> param) {
		return new ButtonCell();
	}



	private static class ButtonCell extends TableCell<QuizTableItem, String> {

		@Override
		protected void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);

			if (!empty) {
				String question = Jsoup.parse(item).text();

				setText(question);
			}
			else {
				setText(null);
			}
		}

	}

}
