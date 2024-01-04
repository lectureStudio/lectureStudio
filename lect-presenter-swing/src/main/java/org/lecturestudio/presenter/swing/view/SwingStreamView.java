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

import java.awt.Component;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.presenter.api.view.StreamView;
import org.lecturestudio.swing.AwtResourceLoader;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

@SwingView(name = "stream-view")
public class SwingStreamView extends JFrame implements StreamView {

	private JTextField addressTextField;

	private JButton reloadButton;

	private JPanel contentContainer;

	private Component browserContent;

	private boolean browserFocus = true;


	@Override
	public void setBrowseAddress(String address) {
		SwingUtils.invoke(() -> {
			addressTextField.setText(address);
		});
	}

	@Override
	public void setBrowserComponent(Component component) {
		SwingUtils.invoke(() -> {
			contentContainer.add(component);
		});
	}

	@Override
	public void setBrowserFocus(boolean focus) {
		if (browserFocus == focus) {
			return;
		}

		changeBrowserFocus(true);
	}

	@Override
	public void close() {
		SwingUtils.invoke(() -> {
			setVisible(false);
			dispose();
		});
	}

	@Override
	public void open() {
		SwingUtils.invoke(() -> {
			reloadButton.transferFocus();
			addressTextField.requestFocus();
			setVisible(true);
		});
	}

	@Override
	public void setOnOpenUrl(ConsumerAction<String> action) {
		addressTextField.addActionListener(e -> action
				.execute(addressTextField.getText()));
	}

	@Override
	public void setOnReloadUrl(Action action) {
		SwingUtils.bindAction(reloadButton, action);
	}

	@Override
	public void setOnClose(Action action) {
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				executeAction(action);
			}
		});
	}

	@ViewPostConstruct
	private void initialize() {
		// Clear focus from the browser when the address field gains focus.
		addressTextField.addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {
				if (!browserFocus) {
					return;
				}

				changeBrowserFocus(false);

				addressTextField.requestFocus();
			}
		});

		List<Image> icons = new ArrayList<>();
		icons.add(AwtResourceLoader.getImage("gfx/app-icon/24.png"));
		icons.add(AwtResourceLoader.getImage("gfx/app-icon/32.png"));
		icons.add(AwtResourceLoader.getImage("gfx/app-icon/48.png"));
		icons.add(AwtResourceLoader.getImage("gfx/app-icon/128.png"));

		setIconImages(icons);
	}

	private void changeBrowserFocus(boolean focus) {
		browserFocus = focus;

		KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
	}
}
