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

package org.lecturestudio.core;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.bus.EventBus;

import org.junit.jupiter.api.BeforeEach;

public abstract class CoreTest {

	protected ApplicationContext context;


	@BeforeEach
	void setUpServiceTest() {
		Configuration config = new Configuration();

		EventBus eventBus = new EventBus();
		EventBus audioBus = new EventBus();

		Dictionary dict = new Dictionary() {

			@Override
			public String get(String key) throws NullPointerException {
				return key;
			}

			@Override
			public boolean contains(String key) {
				return false;
			}
		};

		context = new ApplicationContext(null, config, dict, eventBus, audioBus) {

			@Override
			public void saveConfiguration() {

			}
		};
	}

}
