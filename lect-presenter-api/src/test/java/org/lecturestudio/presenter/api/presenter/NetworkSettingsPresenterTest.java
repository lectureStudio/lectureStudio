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

package org.lecturestudio.presenter.api.presenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.NetworkInterface;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.media.config.NetworkConfiguration;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.view.NetworkSettingsView;
import org.lecturestudio.web.api.filter.IpFilter;
import org.lecturestudio.web.api.filter.IpRangeRule;

class NetworkSettingsPresenterTest extends PresenterTest {

	@Test
	void testReset() {
		NetworkSettingsMockView view = new NetworkSettingsMockView();

		NetworkSettingsPresenter presenter = new NetworkSettingsPresenter(context, view);
		presenter.initialize();

		view.resetAction.execute();

		PresenterConfiguration presenterConfig = (PresenterConfiguration) context.getConfiguration();

		NetworkConfiguration config = presenterConfig.getNetworkConfig();
		NetworkConfiguration defaultConfig = new DefaultConfiguration().getNetworkConfig();

		assertEquals(defaultConfig.getAdapter(), config.getAdapter());
		assertEquals(defaultConfig.getIpFilter(), config.getIpFilter());
	}

	@Test
	void testSetIpRangeRules() {
		AtomicReference<List<IpRangeRule>> rulesRef = new AtomicReference<>();

		PresenterConfiguration presenterConfig = (PresenterConfiguration) context.getConfiguration();
		NetworkConfiguration config = presenterConfig.getNetworkConfig();

		NetworkSettingsMockView view = new NetworkSettingsMockView() {
			@Override
			public void setIpRules(List<IpRangeRule> ipRules) {
				rulesRef.set(ipRules);
			}
		};

		NetworkSettingsPresenter presenter = new NetworkSettingsPresenter(context, view);
		presenter.initialize();

		assertEquals(3, config.getIpFilter().getRules().size());
		assertEquals(rulesRef.get(), config.getIpFilter().getRules());
	}

	@Test
	void testAddIpRangeRule() {
		AtomicReference<List<IpRangeRule>> rulesRef = new AtomicReference<>();

		PresenterConfiguration presenterConfig = (PresenterConfiguration) context.getConfiguration();
		NetworkConfiguration config = presenterConfig.getNetworkConfig();

		NetworkSettingsMockView view = new NetworkSettingsMockView() {
			@Override
			public void setIpRules(List<IpRangeRule> ipRules) {
				rulesRef.set(ipRules);
			}
		};

		NetworkSettingsPresenter presenter = new NetworkSettingsPresenter(context, view);
		presenter.initialize();

		view.addIpRuleAction.execute();

		assertEquals(4, config.getIpFilter().getRules().size());
		assertEquals(rulesRef.get(), config.getIpFilter().getRules());

		view.addIpRuleAction.execute();

		assertEquals(5, config.getIpFilter().getRules().size());
		assertEquals(rulesRef.get(), config.getIpFilter().getRules());
	}

	@Test
	void testDeleteIpRangeRule() {
		AtomicReference<List<IpRangeRule>> rulesRef = new AtomicReference<>();

		PresenterConfiguration presenterConfig = (PresenterConfiguration) context.getConfiguration();
		NetworkConfiguration config = presenterConfig.getNetworkConfig();
		IpFilter ipFilter = config.getIpFilter();

		NetworkSettingsMockView view = new NetworkSettingsMockView() {
			@Override
			public void setIpRules(List<IpRangeRule> ipRules) {
				rulesRef.set(ipRules);
			}
		};

		NetworkSettingsPresenter presenter = new NetworkSettingsPresenter(context, view);
		presenter.initialize();

		view.deleteIpRuleAction.execute(ipFilter.getRules().get(1));

		assertEquals(2, config.getIpFilter().getRules().size());
		assertEquals(rulesRef.get(), config.getIpFilter().getRules());

		view.deleteIpRuleAction.execute(ipFilter.getRules().get(0));

		assertEquals(1, config.getIpFilter().getRules().size());
		assertEquals(rulesRef.get(), config.getIpFilter().getRules());
	}



	private static class NetworkSettingsMockView implements NetworkSettingsView {

		Action resetAction;

		Action addIpRuleAction;

		ConsumerAction<IpRangeRule> deleteIpRuleAction;


		@Override
		public void setNetworkInterface(StringProperty networkInterface) {

		}

		@Override
		public void setNetworkInterfaces(List<NetworkInterface> networkInterfaces) {

		}

		@Override
		public void setIPv4Address(String address) {

		}

		@Override
		public void setIPv6Address(String address) {

		}

		@Override
		public void setIpRules(List<IpRangeRule> ipRules) {

		}

		@Override
		public void setOnAddIpRule(Action action) {
			assertNotNull(action);

			addIpRuleAction = action;
		}

		@Override
		public void setOnDeleteIpRule(ConsumerAction<IpRangeRule> action) {
			assertNotNull(action);

			deleteIpRuleAction = action;
		}

		@Override
		public void setOnClose(Action action) {

		}

		@Override
		public void setOnReset(Action action) {
			assertNotNull(action);

			resetAction = action;
		}
	}
}