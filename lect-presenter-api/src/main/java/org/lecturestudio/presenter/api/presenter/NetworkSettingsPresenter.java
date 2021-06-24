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

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.util.ListChangeListener;
import org.lecturestudio.core.util.NetUtils;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.config.NetworkConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.view.NetworkSettingsView;
import org.lecturestudio.web.api.filter.IpFilter;
import org.lecturestudio.web.api.filter.IpRangeRule;

import javax.inject.Inject;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class NetworkSettingsPresenter extends Presenter<NetworkSettingsView> {

	private final NetworkConfiguration netConfig;


	@Inject
	NetworkSettingsPresenter(ApplicationContext context, NetworkSettingsView view) {
		super(context, view);

		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();

		this.netConfig = config.getNetworkConfig();
	}

	@Override
	public void initialize() {
		List<NetworkInterface> networkInterfaces = NetUtils.getNetworkInterfaces();

		view.setNetworkInterfaces(networkInterfaces);
		view.setNetworkInterface(netConfig.adapterProperty());

		updateNetAdapter(netConfig.getAdapter());

		netConfig.adapterProperty().addListener((observable, oldAdapter, newAdapter) -> {
			updateNetAdapter(newAdapter);
		});

		List<IpRangeRule> ipRules = new ArrayList<>();

		// Fill IP table.
		IpFilter ipFilter = netConfig.getIpFilter();
		if (nonNull(ipFilter)) {
			ipRules = ipFilter.getRules();
			ipFilter.addListener(new ListChangeListener<>() {

				@Override
				public void listChanged(ObservableList<IpRangeRule> list) {
					view.setIpRules(list);
				}
			});
		}

		view.setIpRules(ipRules);
		view.setOnAddIpRule(this::addIpRule);
		view.setOnDeleteIpRule(this::deleteIpRule);
		view.setOnReset(this::reset);
	}

	private void addIpRule() {
		IpFilter ipFilter = netConfig.getIpFilter();

		ipFilter.registerRule(new IpRangeRule());
	}

	private void deleteIpRule(IpRangeRule ipRule) {
		if (nonNull(ipRule)) {
			IpFilter ipFilter = netConfig.getIpFilter();
			ipFilter.unregisterRule(ipRule);
		}
	}

	private void updateNetAdapter(String interfaceName) {
		List<NetworkInterface> networkInterfaces = NetUtils.getNetworkInterfaces();
		NetworkInterface networkInterface = null;

		for (NetworkInterface netInterface : networkInterfaces) {
			if (netInterface.getName().equals(interfaceName)) {
				networkInterface = netInterface;
				break;
			}
		}

		if (isNull(networkInterface) && !networkInterfaces.isEmpty()) {
			// Use the first available NetworkInterface as the default one.
			networkInterface = networkInterfaces.get(0);
		}

		if (nonNull(networkInterface)) {
			setIpAddress(networkInterface);
		}
	}

	private void setIpAddress(NetworkInterface networkInterface) {
		Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();

		for (InetAddress inetAddress : Collections.list(inetAddresses)) {
			if (inetAddress instanceof Inet6Address) {
				view.setIPv6Address(inetAddress.getHostAddress());
			}
			else {
				view.setIPv4Address(inetAddress.getHostAddress());
			}
		}
	}

	private void reset() {
		NetworkConfiguration defaultConfig = new DefaultConfiguration().getNetworkConfig();

		netConfig.setAdapter(defaultConfig.getAdapter());
		netConfig.getIpFilter().setRules(defaultConfig.getIpFilter().getRules());
	}
}