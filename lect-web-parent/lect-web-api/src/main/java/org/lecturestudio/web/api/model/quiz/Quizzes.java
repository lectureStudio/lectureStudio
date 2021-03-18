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

package org.lecturestudio.web.api.model.quiz;

import org.lecturestudio.core.model.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Quizzes {

    private final Map<Document, List<Quiz>> docQuizzes = new HashMap<>();


    public void add(Document document, List<Quiz> quizzes) {
        List<Quiz> quizList = docQuizzes.get(document);

        if (quizList == null) {
            quizList = new ArrayList<>(quizzes);
            docQuizzes.put(document, quizList);
        }
        else {
            quizList.addAll(quizzes);
        }
    }

    public void remove(Document document) {
        docQuizzes.remove(document);
    }

    public List<Quiz> getQuizzes(Document document) {
        return docQuizzes.get(document);
    }

}
