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

package org.lecturestudio.presenter.swing.view;

import java.awt.event.ActionEvent;
import java.net.NetworkInterface;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.lecturestudio.core.beans.Converter;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.converter.NetAdapterConverter;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.presenter.api.view.NetworkSettingsView;
import org.lecturestudio.presenter.swing.view.model.IpRangeRuleTableModel;
import org.lecturestudio.swing.beans.ConvertibleObjectProperty;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;
import org.lecturestudio.web.api.filter.IpRangeRule;

@SwingView(name = "network-settings", presenter = org.lecturestudio.presenter.api.presenter.NetworkSettingsPresenter.class)
public class SwingNetworkSettingsView extends JPanel implements NetworkSettingsView {

	private JComboBox<NetworkInterface> netAdapterCombo;

	private JTextField ipv4TextField;

	private JTextField ipv6TextField;

	private JTable ipFilterTable;

	private JButton addIpRuleButton;

	private JButton closeButton;

	private JButton resetButton;

	private ConsumerAction<IpRangeRule> deleteIpRuleAction;

	public javax.swing.Action deleteAction = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			int row = Integer.parseInt(e.getActionCommand());
			IpRangeRuleTableModel model = (IpRangeRuleTableModel) ipFilterTable.getModel();
			IpRangeRule rangeRule = model.getItem(row);

			executeAction(deleteIpRuleAction, rangeRule);
		}
	};


	public SwingNetworkSettingsView() {
		super();
	}

	@Override
	public void setNetworkInterface(StringProperty networkInterface) {
		Converter<String, NetworkInterface> netAdapterConv = new NetAdapterConverter();
		ConvertibleObjectProperty<String, NetworkInterface> property = new ConvertibleObjectProperty<>(networkInterface, netAdapterConv);

		SwingUtils.bindBidirectional(netAdapterCombo, property);
	}

	@Override
	public void setNetworkInterfaces(List<NetworkInterface> networkInterfaces) {
		SwingUtils.invoke(() -> netAdapterCombo.setModel(
				new DefaultComboBoxModel<>(new Vector<>(networkInterfaces))));
	}

	@Override
	public void setIPv4Address(String address) {
		SwingUtils.invoke(() -> ipv4TextField.setText(address));
	}

	@Override
	public void setIPv6Address(String address) {
		SwingUtils.invoke(() -> ipv6TextField.setText(address));
	}

	@Override
	public void setIpRules(List<IpRangeRule> ipRules) {
		SwingUtils.invoke(() -> {
			IpRangeRuleTableModel model = (IpRangeRuleTableModel) ipFilterTable.getModel();
			model.setItems(ipRules);
		});
	}

	@Override
	public void setOnAddIpRule(Action action) {
		SwingUtils.bindAction(addIpRuleButton, action);
	}

	@Override
	public void setOnDeleteIpRule(ConsumerAction<IpRangeRule> action) {
		this.deleteIpRuleAction = action;
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		SwingUtils.bindAction(resetButton, action);
	}

	@ViewPostConstruct
	private void initialize() {
		ipFilterTable.setModel(new IpRangeRuleTableModel(ipFilterTable.getColumnModel()));
	}
}
