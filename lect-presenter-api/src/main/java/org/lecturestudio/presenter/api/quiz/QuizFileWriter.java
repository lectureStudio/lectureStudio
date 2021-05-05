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

package org.lecturestudio.presenter.api.quiz;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Base64;

import org.lecturestudio.web.api.model.quiz.Quiz;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class QuizFileWriter implements QuizWriter {

	private final File file;


	public QuizFileWriter(File file) {
		this.file = file;
	}

	@Override
	public void writeQuiz(Quiz quiz) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
			Document doc = Jsoup.parse(quiz.getQuestion());
			doc.outputSettings().indentAmount(0);

			// make in-line
			String question = doc.body().html().replace("\n", "");
			question += "\b\b\b" + Base64.getEncoder().encodeToString(toBytes(quiz.getInputFilter().getRules()));

			writer.write(question);
			writer.newLine();
			writer.write(quiz.getType().toString());
			writer.newLine();
			writer.write(String.valueOf(quiz.getOptions().size()));
			writer.newLine();

			for (String option : quiz.getOptions()) {
				writer.write(option);
				writer.newLine();
			}

			writer.newLine();
			writer.flush();
		}
	}

	@Override
	public void clear() {
		file.delete();
	}

	private static byte[] toBytes(Object o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();

		byte[] output = baos.toByteArray();

		baos.close();

		return output;
	}

}
