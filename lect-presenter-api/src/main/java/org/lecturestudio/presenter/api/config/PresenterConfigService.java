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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.lecturestudio.core.app.configuration.JsonConfigurationService;
import org.lecturestudio.presenter.api.model.bind.FilterRuleMixIn;
import org.lecturestudio.presenter.api.model.bind.IpRangeRuleMixIn;
import org.lecturestudio.presenter.api.model.bind.MinMaxRuleMixIn;
import org.lecturestudio.presenter.api.model.bind.RegexRuleMixIn;
import org.lecturestudio.web.api.filter.FilterRule;
import org.lecturestudio.web.api.filter.IpRangeRule;
import org.lecturestudio.web.api.filter.MinMaxRule;
import org.lecturestudio.web.api.filter.RegexRule;

public class PresenterConfigService extends JsonConfigurationService<PresenterConfiguration> {

	@Override
	protected void initModules(ObjectMapper mapper) {
		SimpleModule module = new SimpleModule();
		module.setMixInAnnotation(FilterRule.class, FilterRuleMixIn.class);
		module.setMixInAnnotation(RegexRule.class, RegexRuleMixIn.class);
		module.setMixInAnnotation(MinMaxRule.class, MinMaxRuleMixIn.class);
		module.setMixInAnnotation(IpRangeRule.class, IpRangeRuleMixIn.class);

		mapper.registerModule(module);
	}

}
