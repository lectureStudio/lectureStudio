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

package org.lecturestudio.presenter.javafx.view;

import java.net.NetworkInterface;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.beans.Converter;
import org.lecturestudio.core.converter.NetAdapterConverter;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.javafx.beans.ConvertibleObjectProperty;
import org.lecturestudio.javafx.event.CellButtonActionEvent;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.NetworkSettingsView;
import org.lecturestudio.presenter.javafx.view.model.IpRuleTableItem;
import org.lecturestudio.web.api.filter.IpRangeRule;

@FxmlView(name = "network-settings", presenter = org.lecturestudio.presenter.api.presenter.NetworkSettingsPresenter.class)
public class FxNetworkSettingsView extends GridPane implements NetworkSettingsView {

	@FXML
	private ComboBox<NetworkInterface> netAdapterCombo;

	@FXML
	private TextField ipv4TextField;

	@FXML
	private TextField ipv6TextField;

	@FXML
	private TableView<IpRuleTableItem> ipFilterTableView;

	@FXML
	private Hyperlink addIpRuleButton;

	@FXML
	private Button closeButton;

	@FXML
	private Button resetButton;

	private ConsumerAction<IpRangeRule> deleteIpRuleAction;


	public FxNetworkSettingsView() {
		super();
	}

	@Override
	public void setNetworkInterface(StringProperty networkInterface) {
		Converter<String, NetworkInterface> netAdapterConv = new NetAdapterConverter();
		ConvertibleObjectProperty<String, NetworkInterface> property = new ConvertibleObjectProperty<>(networkInterface, netAdapterConv);

		netAdapterCombo.valueProperty().bindBidirectional(property);
	}

	@Override
	public void setNetworkInterfaces(List<NetworkInterface> networkInterfaces) {
		FxUtils.invoke(() -> {
			netAdapterCombo.getItems().setAll(networkInterfaces);
		});
	}

	@Override
	public void setIPv4Address(String address) {
		FxUtils.invoke(() -> {
			ipv4TextField.setText(address);
		});
	}

	@Override
	public void setIPv6Address(String address) {
		FxUtils.invoke(() -> {
			ipv6TextField.setText(address);
		});
	}

	@Override
	public void setIpRules(List<IpRangeRule> ipRules) {
		FxUtils.invoke(() -> {
			ipFilterTableView.getItems().clear();

			for (IpRangeRule rule : ipRules) {
				ipFilterTableView.getItems().add(new IpRuleTableItem(rule));
			}
		});
	}

	@Override
	public void setOnAddIpRule(Action action) {
		FxUtils.bindAction(addIpRuleButton, action);
	}

	@Override
	public void setOnDeleteIpRule(ConsumerAction<IpRangeRule> action) {
		this.deleteIpRuleAction = action;
	}

	@Override
	public void setOnClose(Action action) {
		FxUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		FxUtils.bindAction(resetButton, action);
	}

	@FXML
	private void onDeleteIpRule(CellButtonActionEvent event) {
		IpRuleTableItem item = (IpRuleTableItem) event.getCellItem();
		IpRangeRule ipRule = item.getIpRangeRule();

		executeAction(deleteIpRuleAction, ipRule);
	}

	@FXML
	private void initialize() {
		// Set IP table edit policy.
		ObservableList<TableColumn<IpRuleTableItem, ?>> ipColumns = ipFilterTableView.getColumns();

		@SuppressWarnings("unchecked")
		TableColumn<IpRuleTableItem, String> fromIpColumn = (TableColumn<IpRuleTableItem, String>) ipColumns.get(0);
		fromIpColumn.setOnEditCommit(event -> {
			IpRuleTableItem item = event.getTableView().getItems().get(event.getTablePosition().getRow());
			item.setFromIP(event.getNewValue());
		});

		@SuppressWarnings("unchecked")
		TableColumn<IpRuleTableItem, String> toIpColumn = (TableColumn<IpRuleTableItem, String>) ipColumns.get(1);
		toIpColumn.setOnEditCommit(event -> {
			IpRuleTableItem item = event.getTableView().getItems().get(event.getTablePosition().getRow());
			item.setToIP(event.getNewValue());
		});
	}

}
