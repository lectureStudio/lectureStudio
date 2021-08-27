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

package org.lecturestudio.presenter.api.util;

import static java.util.Objects.nonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The message logger that writes messages into a HTML based file.
 * 
 * @author Alex Andres
 */
public class HtmlMessageLogger {

	/**
	 * The file where the messages are written into.
	 */
	private final File logFile;

	/**
	 * The document.
	 */
	private Document doc;

	/**
	 * The root element of the document.
	 */
	private Element root;


	/**
	 * Creates a new {@link HtmlMessageLogger} that writes messages into the
	 * provided file.
	 * 
	 * @param file
	 *            the log file
	 */
	public HtmlMessageLogger(File file) {
		this.logFile = file;

		if (file.exists()) {
			file.delete();
		}

		try {
			createXmlDocument();
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates the XML document and adds the lecture title into it.
	 * 
	 * @throws ParserConfigurationException
	 */
	private void createXmlDocument() throws ParserConfigurationException {
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
		doc = docBuilder.newDocument();
		root = doc.createElement("log");
		doc.appendChild(root);
	}

	/**
	 * Writes a message into the log file.
	 * 
	 * @param host
	 *            the host name or IP address of the sender
	 * @param date
	 *            the current date
	 * @param message
	 *            the message to log
	 */
	public void logMessage(String host, ZonedDateTime date, String message) {
		ZonedDateTime dateUTC = date.withZoneSameInstant(ZoneId.of("Etc/UTC"));
		String formattedDate = dateUTC.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));

		Element entryNode = doc.createElement("entry");
		Element hostNode = doc.createElement("host");
		hostNode.appendChild(doc.createTextNode(nonNull(host) ? host : ""));

		Element dateNode = doc.createElement("date");
		dateNode.appendChild(doc.createTextNode(formattedDate));

		Element messageNode = doc.createElement("message");
		messageNode.appendChild(doc.createTextNode(message));

		entryNode.appendChild(hostNode);
		entryNode.appendChild(dateNode);
		entryNode.appendChild(messageNode);

		root.appendChild(entryNode);

		try {
			writeLog();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes a abuse message to the log file.
	 * 
	 * @param host
	 *            the host name or IP address of the sender
	 * @param message
	 *            the message to log
	 */
	public void logAbuse(String host, String message) {
		NodeList childNodes = root.getElementsByTagName("entry");

		for (int i = 0; i < childNodes.getLength(); i++) {
			Element node = (Element) childNodes.item(i);

			Node hostNode = node.getFirstChild();
			Node messageNode = node.getLastChild();

			if (hostNode.getTextContent().equals(host) && messageNode.getTextContent().equals(message)) {

				node.setAttribute("abuse", "true");

				try {
					writeLog();
				}
				catch (Exception e) {
					e.printStackTrace();
				}

				break;
			}
		}
	}

	/**
	 * Writes the XML document to the file system.
	 * 
	 * @throws Exception
	 */
	private void writeLog() throws Exception {
		TransformerFactory transformFactory = TransformerFactory.newInstance();
		transformFactory.setAttribute("indent-number", 4);

		String xslFile = "resources/xsl/messenger-log.xsl";
		StreamSource xslSource = new StreamSource(getClass().getClassLoader().getResourceAsStream(xslFile));

		Transformer transformer = transformFactory.newTransformer(xslSource);
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "html");

		FileOutputStream outputStream = new FileOutputStream(logFile);

		DOMSource docSource = new DOMSource(doc);
		StreamResult result = new StreamResult(new OutputStreamWriter(outputStream));
		transformer.transform(docSource, result);
		
		outputStream.close();
	}
	
}
