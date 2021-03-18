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

package org.lecturestudio.core.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.model.RecentDocument;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JsonRecentDocumentSource implements RecentDocumentSource {

	private final static Logger LOG = LogManager.getLogger(JsonRecentDocumentSource.class);

	private final File file;

	/**
	 * The object mapper that configures the conversion to and from the JSON
	 * format.
	 */
	private final ObjectMapper mapper;

	private List<RecentDocument> list;


	public JsonRecentDocumentSource(File file) throws IOException {
		this.file = file;

		mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		load();
	}

	@Override
	public void add(RecentDocument document) {
		if (list.contains(document)) {
			return;
		}

		list.add(document);

		save();
	}

	@Override
	public void addAtIndex(int index, RecentDocument document) {
		list.add(index, document);

		save();
	}

	@Override
	public void delete(RecentDocument document) {
		list.remove(document);

		save();
	}

	@Override
	public void deleteAtIndex(int index) {
		list.remove(index);

		save();
	}

	@Override
	public List<RecentDocument> getAll() {
		return new ArrayList<>(list);
	}

	private void load() throws IOException {
		if (!file.exists()) {
			list = new ArrayList<>();
			return;
		}

		try (InputStream input = new FileInputStream(file)) {
			list = mapper.readValue(input, new TypeReference<List<RecentDocument>>(){});
		}
	}

	private void save() {
		try {
			mapper.writeValue(file, list);
		}
		catch (IOException e) {
			LOG.error("Write file failed", e);
		}
	}
}
