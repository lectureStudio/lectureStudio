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

package org.lecturestudio.presenter.api.util;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.web.api.message.MessengerMessage;

/**
 * The message logger that writes received messages into a PDF file.
 *
 * @author Alex Andres
 */
public class PDFMessageWriter {

	/**
	 * Writes messages into a OutputStream with a specific file format.
	 *
	 * @param messages   The message wich to persist.
	 * @param dictionary The dictionary to use translated text in the output.
	 * @param os         The output stream wich will contain the written data.
	 */
	public void write(List<MessengerMessage> messages, Dictionary dictionary,
			OutputStream os) throws IOException {
		var doc = Jsoup.parse("<html></html>");

		var table = doc.body().appendElement("table");
		table.attr("cellpadding", "8");
		table.attr("cellspacing", "1");
		table.attr("width", "100%");

		var row = table.appendElement("tr");
		row.appendElement("th").text(dictionary.get("text.message.time"));
		row.appendElement("th").text(dictionary.get("text.message"));

		for (var message : messages) {
			ZonedDateTime dateUTC = message.getDate()
					.withZoneSameInstant(ZoneId.systemDefault());
			String formattedDate = dateUTC.format(
					DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

			row = table.appendElement("tr");
			row.appendElement("td").text(formattedDate).attr("width", "150");
			row.appendElement("td").text(message.getMessage().getText());
		}

		PdfRendererBuilder builder = new PdfRendererBuilder();
		builder.withW3cDocument(new W3CDom().fromJsoup(doc), "/");
		builder.withProducer("lecturePresenter");
		builder.toStream(os);
		builder.run();
	}

	public Document createDocument(List<MessengerMessage> messages,
			Dictionary dictionary) throws IOException {
		if (messages.isEmpty()) {
			return null;
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		write(messages, dictionary, bos);

		Document document = new Document(bos.toByteArray());
		document.setTitle(dictionary.get("text.messages"));

		return document;
	}
}
