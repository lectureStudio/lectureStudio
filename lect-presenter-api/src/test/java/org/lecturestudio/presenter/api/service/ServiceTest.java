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

package org.lecturestudio.presenter.api.service;

import java.io.IOException;

import org.lecturestudio.core.app.AppDataLocator;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.lecturestudio.presenter.audio.DummyAudioSystemProvider;

abstract class ServiceTest {

	ApplicationContext context;

	AudioSystemProvider audioSystemProvider;

	DocumentService documentService;

	Document document1;
	Document document2;


	@BeforeEach
	void setUpServiceTest() throws IOException {
		Configuration config = new PresenterConfiguration();

		EventBus eventBus = new EventBus();
		EventBus audioBus = new EventBus();

		Dictionary dict = new Dictionary() {

			@Override
			public String get(String key) throws NullPointerException {
				return "";
			}

			@Override
			public boolean contains(String key) {
				return true;
			}
		};

		AppDataLocator locator = new AppDataLocator("test");

		context = new PresenterContext(locator, null, config, dict, eventBus, audioBus, null) {

			@Override
			public void saveConfiguration() {

			}
		};

		document1 = new Document();
		document1.createPage();
		document1.createPage();
		document1.createPage();

		document2 = new Document();
		document2.createPage();
		document2.createPage();
		document2.createPage();

		documentService = new DocumentService(context);
		documentService.addDocument(document1);
		documentService.addDocument(document2);
		documentService.selectDocument(document1);

		audioSystemProvider = new DummyAudioSystemProvider();
	}

	@AfterEach
	void tearDownServiceTest() {
		for (Document doc : documentService.getDocuments().getPdfDocuments()) {
			doc.close();
		}
	}
}
