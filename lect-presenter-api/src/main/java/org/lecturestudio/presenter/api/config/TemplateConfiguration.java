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

package org.lecturestudio.presenter.api.config;

import java.beans.Transient;
import java.util.List;

public class TemplateConfiguration {

	private final DocumentTemplateConfiguration quizTemplateConfig = new DocumentTemplateConfiguration();

	private final DocumentTemplateConfiguration chatMessageTemplateConfig = new DocumentTemplateConfiguration();

	private final DocumentTemplateConfiguration hallMessageTemplateConfig = new DocumentTemplateConfiguration();

	private final DocumentTemplateConfiguration whiteboardTemplateConfig = new DocumentTemplateConfiguration();


	public DocumentTemplateConfiguration getQuizTemplateConfig() {
		return quizTemplateConfig;
	}

	public DocumentTemplateConfiguration getChatMessageTemplateConfig() {
		return chatMessageTemplateConfig;
	}

	public DocumentTemplateConfiguration getHallMessageTemplateConfig() {
		return hallMessageTemplateConfig;
	}

	public DocumentTemplateConfiguration getWhiteboardTemplateConfig() {
		return whiteboardTemplateConfig;
	}

	@Transient
	public List<DocumentTemplateConfiguration> getAll() {
		return List.of(quizTemplateConfig, chatMessageTemplateConfig,
				hallMessageTemplateConfig, whiteboardTemplateConfig);
	}
}
