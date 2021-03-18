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

package org.lecturestudio.swing.ui;

import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;

public class CMenuBar extends JMenuBar implements ContainerListener {

	private static final long serialVersionUID = -8705744993332651639L;

	private final Map<String, JMenuItem> items = new HashMap<>();


	protected void getMenuItems(MenuElement element) {
		MenuElement[] elements = element.getSubElements();

		if (elements.length == 0) {
			registerMenuElement(element);
		}
		else {
			for (MenuElement e : elements) {
				registerMenuElement(element);
				getMenuItems(e);
			}
		}
	}

	private void registerMenuElement(MenuElement element) {
		// Avoid to add JPopupMenu.
		if (element instanceof JMenuItem) {
			JMenuItem item = (JMenuItem) element.getComponent();
			items.put(item.getActionCommand(), item);
		}
	}

	private void unregisterMenuElement(MenuElement element) {
		if (element instanceof JMenuItem) {
			JMenuItem item = (JMenuItem) element.getComponent();
			items.remove(item.getActionCommand());
		}
	}

	public JMenu registerMenu(String key, JMenu menu) {
		menu.setActionCommand(key);
		add(menu);

		return menu;
	}

	@Override
	public JMenu add(JMenu menu) {
		menu.getPopupMenu().addContainerListener(this);
		getMenuItems(menu);

		return super.add(menu);
	}

	public JMenu getMenu(String caption) {
		return (JMenu) items.get(caption);
	}

	public JMenuItem getMenuItem(String actionCommand) {
		return items.get(actionCommand);
	}

	public JMenuItem createMenuItem(String key, Action action, boolean enabled) {
		JMenuItem menuItem = new JMenuItem(action);
		menuItem.setEnabled(enabled);

		items.put(key, menuItem);

		return menuItem;
	}

	public JMenuItem createMenuItem(String key, Class<? extends JMenuItem> klass, Action action, boolean enabled) {
		JMenuItem menuItem;

		try {
			// Get the dynamic constructor method.
			Constructor<? extends JMenuItem> ctor = klass.getConstructor(Action.class);

			menuItem = ctor.newInstance(action);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		menuItem.setEnabled(enabled);

		items.put(key, menuItem);

		return menuItem;
	}

	public void setItemEnabled(String actionCommand, boolean enabled) {
		JMenuItem menuItem = items.get(actionCommand);

		if (menuItem != null)
			menuItem.setEnabled(enabled);
	}

	public void setItemSelected(String actionCommand, boolean selected) {
		JMenuItem menuItem = items.get(actionCommand);

		if (menuItem != null)
			menuItem.setSelected(selected);
	}

	@Override
	public void componentAdded(ContainerEvent e) {
		if (e.getChild() instanceof MenuElement) {
			MenuElement element = (MenuElement) e.getChild();
			registerMenuElement(element);
		}
	}

	@Override
	public void componentRemoved(ContainerEvent e) {
		if (e.getChild() instanceof MenuElement) {
			MenuElement element = (MenuElement) e.getChild();
			unregisterMenuElement(element);
		}
	}

}
