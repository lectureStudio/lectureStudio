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

package org.lecturestudio.swing.components;

import com.kitfox.svg.app.beans.SVGIcon;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.swing.AwtResourceLoader;

public class CToolBar extends JToolBar {

	private static final long serialVersionUID = 5669295405676667299L;

	private final Map<String, Component> components = new HashMap<>();

	private final Map<String, ButtonGroup> buttonGroups = new HashMap<>();


	public AbstractButton addButton(Dictionary dict, String key, Class<? extends AbstractButton> klass, Action action, boolean enabled) {
		AbstractButton button;
		Constructor<? extends AbstractButton> ctor;

		try {
			// get the dynamic constructor method
			ctor = klass.getConstructor();
			button = ctor.newInstance();
		}
		catch (Exception e) {
			try {
				// get the dynamic constructor method
				ctor = klass.getConstructor(ResourceBundle.class);
				button = ctor.newInstance(new ResourceBundle() {

					@Override
					protected Object handleGetObject(String key) {
						return dict.get(key);
					}

					@Override
					public Enumeration<String> getKeys() {
						return null;
					}
				});
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

		button.setAction(action);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setFocusable(false);
		button.setEnabled(enabled);
		button.setName("toolbar.button");

		addComponent(key, button);

		return button;
	}

	public AbstractButton addButton(Dictionary dict, String key, Class<? extends AbstractButton> klass, Action action, String groupName, boolean enabled) {
		AbstractButton button = addButton(dict, key, klass, action, enabled);
		addButtonToGroup(button, groupName);

		return button;
	}

	public void addSeparator() {
		URI uri;

		try {
			uri = AwtResourceLoader.getIconURI("toolbar-separator.svg");
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		SVGIcon svgicon = new SVGIcon();
		svgicon.setAntiAlias(true);
		svgicon.setAutosize(SVGIcon.AUTOSIZE_VERT);
		svgicon.setSvgURI(uri);
		svgicon.setPreferredSize(new Dimension(32, 32));

		JLabel separator = new JLabel();
		separator.setIcon(svgicon);
		separator.setBorder(new EmptyBorder(0, 5, 0, 5));

		add(separator);
	}

	public AbstractButton getButton(String key) {
		return (AbstractButton) components.get(key);
	}

	public void addButtonToGroup(AbstractButton button, String groupName) {
		ButtonGroup group = buttonGroups.get(groupName);

		if (group == null) {
			group = new ButtonGroup();
			buttonGroups.put(groupName, group);
		}

		group.add(button);
	}

	public void setEnabled(String item, boolean enabled) {
		Component component = components.get(item);

		if (component != null)
			component.setEnabled(enabled);
	}

	public void setSelected(String item, boolean selected) {
		AbstractButton button = (AbstractButton) components.get(item);
		
		if (button != null)
			button.setSelected(selected);
	}

	private void addComponent(String key, JComponent component) {
		add(component);
		components.put(key, component);
	}
	
}
