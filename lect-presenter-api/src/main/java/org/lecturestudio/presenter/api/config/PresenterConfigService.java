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

package org.lecturestudio.presenter.api.config;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.lecturestudio.core.app.configuration.JsonConfigurationService;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.presenter.api.model.bind.IpRangeRuleDeserializer;
import org.lecturestudio.presenter.api.model.bind.RegexRuleDeserializer;
import org.lecturestudio.web.api.filter.IpRangeRule;
import org.lecturestudio.web.api.filter.RegexRule;

public class PresenterConfigService extends JsonConfigurationService<PresenterConfiguration> {

	@Override
	protected void initModules(ObjectMapper mapper) {
		SimpleModule module = new SimpleModule();
		module.addDeserializer(RegexRule.class, new RegexRuleDeserializer());
		module.addDeserializer(IpRangeRule.class, new IpRangeRuleDeserializer());

		mapper.registerModule(module);
	}

	@Override
	public void validate(PresenterConfiguration config) {
		DefaultConfiguration defaultConfig = new DefaultConfiguration();

		if (isNull(config.getApplicationName())) {
			config.setApplicationName(defaultConfig.getApplicationName());
		}
		if (isNull(config.getLocale())) {
			config.setLocale(defaultConfig.getLocale());
		}
		if (isNull(config.getAutostartRecording())) {
			config.setAutostartRecording(defaultConfig.getAutostartRecording());
		}
		if (isNull(config.getNotifyToRecord())) {
			config.setNotifyToRecord(defaultConfig.getNotifyToRecord());
		}
		if (isNull(config.getStreamConfig().getServerName())) {
			config.getStreamConfig().setServerName(defaultConfig.getStreamConfig().getServerName());
		}
		if (isNull(config.getStreamConfig().getParticipantVideoLayout())) {
			config.getStreamConfig().setParticipantVideoLayout(defaultConfig.getStreamConfig().getParticipantVideoLayout());
		}
		if (isNull(config.getExternalMessagesConfig().getSize())) {
			config.getExternalMessagesConfig().setSize(defaultConfig.getExternalMessagesConfig().getSize());
		}
		if (isNull(config.getExternalParticipantsConfig().getSize())) {
			config.getExternalParticipantsConfig().setSize(defaultConfig.getExternalParticipantsConfig().getSize());
		}
		if (isNull(config.getExternalParticipantVideoConfig().getSize())) {
			config.getExternalParticipantVideoConfig().setSize(defaultConfig.getExternalParticipantVideoConfig().getSize());
		}
		if (isNull(config.getExternalSlidePreviewConfig().getSize())) {
			config.getExternalSlidePreviewConfig().setSize(defaultConfig.getExternalSlidePreviewConfig().getSize());
		}
		if (isNull(config.getExternalNotesConfig().getSize())) {
			config.getExternalNotesConfig().setSize(defaultConfig.getExternalNotesConfig().getSize());
		}
		if (isNull(config.getExternalSlideNotesConfig().getSize())) {
			config.getExternalSlideNotesConfig().setSize(defaultConfig.getExternalSlideNotesConfig().getSize());
		}

		config.getToolConfig().getTextSettings().setFont(new Font("Open Sans Regular", 14));

		config.getTemplateConfig().getAll().forEach(this::checkBoundsValid);
	}

	private void checkBoundsValid(DocumentTemplateConfiguration tplConfig) {
		Rectangle2D bounds = tplConfig.getBounds();

		if (isNull(bounds)) {
			return;
		}

		if (bounds.getX() > 1 || bounds.getY() > 1 || bounds.getWidth() > 1
				|| bounds.getHeight() > 1) {
			tplConfig.setBounds(new DocumentTemplateConfiguration().getBounds());
		}
	}
}
