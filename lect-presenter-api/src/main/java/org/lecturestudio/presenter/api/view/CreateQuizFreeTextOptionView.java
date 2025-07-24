/*
 * Copyright (C) 2025 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.view;

/**
 * This interface represents a view for creating a quiz style with free-text answers.
 * In this quiz style, the user can submit free text answers, which are then evaluated in real-time.
 * Optionally, the answers can be visualized as a word cloud, where the size of each word corresponds to its frequency
 * in the answers submitted by users.
 */
public interface CreateQuizFreeTextOptionView extends CreateQuizOptionView {

}