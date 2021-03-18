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

package org.lecturestudio.swing.ui.loader;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ComponentInputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.swing.AwtResourceLoader;
import org.lecturestudio.swing.ui.CMenuBar;
import org.lecturestudio.swing.window.MainWindow;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class MenuDocumentHandler extends DefaultHandler {

	/**
	 * Elements
	 */
	private static final String ELEMENT_ROOT = "menubar";
	
	private static final String ELEMENT_MENU = "menu";

	private static final String ELEMENT_MENU_ITEM = "menuItem";

	private static final String ELEMENT_SEPARATOR = "separator";

	private static final String ELEMENT_ACTION_LISTENER = "actionListener";
	
	private static final String ELEMENT_HORIZONTAL_GLUE = "horizontal-glue";
	
	private static final String ELEMENT_HORIZONTAL_STRUT = "horizontal-strut";

	/**
	 * Attributes
	 */
	private static final String ATTRIBUTE_ACCELERATOR = "accelerator";

	private static final String ATTRIBUTE_MNEMONIC = "mnemonic";

	private static final String ATTRIBUTE_ICON = "icon";

	private static final String ATTRIBUTE_ICON_SIZE = "icon-size";
	
	private static final String ATTRIBUTE_DISABLED_ICON = "disabled-icon";

	private static final String ATTRIBUTE_ACTION = "action";

	private static final String ATTRIBUTE_TEXT = "text";

	private static final String ATTRIBUTE_TYPE = "type";

	private static final String ATTRIBUTE_TYPE_CHECKBOX = "checkbox";

	private static final String ATTRIBUTE_TYPE_RADIO = "radio";

	private static final String ATTRIBUTE_SELECTED = "selected";

	private static final String ATTRIBUTE_ENABLED = "enabled";

	private static final String ATTRIBUTE_VISIBLE = "visible";
	
	private static final String ATTRIBUTE_WIDTH = "width";

	/**
	 * Menu content and temporary fields
	 */

	private final ActionListener actionListener = new MenuActionListener();

	private final MainWindow window;

	private final Dictionary dict;

	private CMenuBar menuBar;

	private ButtonGroup buttonGroup;

	private JMenuItem item;

	private Stack<JMenu> menuStack;

	private HashMap<String, ActionListener> listeners;

	boolean hasAction = false;

	private Integer iconSize;


	public MenuDocumentHandler(MainWindow window, Dictionary dict) {
		this.window = window;
		this.dict = dict;
	}

	@Override
	public void startDocument() {
	}

	@Override
	public void endDocument() {
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) {
		switch (qName) {
			case ELEMENT_ROOT:
				createMenuBar(atts);
				break;

			case ELEMENT_MENU:
				createMenu(atts);
				break;

			case ELEMENT_MENU_ITEM:
				createMenuItem(atts);
				break;

			case ELEMENT_ACTION_LISTENER:
				hasAction = true;
				break;

			case ELEMENT_SEPARATOR:
				menuStack.peek().addSeparator();
				buttonGroup = null;
				break;

			case ELEMENT_HORIZONTAL_GLUE:
				menuBar.add(Box.createHorizontalGlue());
				buttonGroup = null;
				break;

			case ELEMENT_HORIZONTAL_STRUT:
				String width = atts.getValue(ATTRIBUTE_WIDTH);
				menuBar.add(Box.createHorizontalStrut(Integer.parseInt(width)));
				buttonGroup = null;
				break;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		switch (qName) {
			case ELEMENT_ROOT:
				menuStack.clear();
				listeners.clear();
				break;

			case ELEMENT_MENU:
				JMenu menu = menuStack.pop();
				int menuCount = menuStack.size();

				if (menuCount > 0) {
					menuStack.peek().add(menu);
				}
				else {
					menuBar.add(menu);
				}
				break;

			case ELEMENT_MENU_ITEM:
				item = null;
				break;

			case ELEMENT_ACTION_LISTENER:
				hasAction = false;
				break;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (hasAction) {
			String text = new String(ch, start, length);
			text = text.trim();

			try {
				ActionListener listener;

				if (listeners.containsKey(text)) {
					listener = listeners.get(text);
				}
				else {
					listener = (ActionListener) Class.forName(text).getConstructor().newInstance();
					listeners.put(text, listener);
				}

				item.addActionListener(listener);
			}
			catch (Exception e) {
				throw new SAXException(e);
			}
		}
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		throw new SAXException(e);
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		throw new SAXException(e);
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		throw new SAXException(e);
	}

	private void createMenuBar(Attributes atts) {
		menuBar = new CMenuBar();
		menuStack = new Stack<>();
		listeners = new HashMap<>();

		String iconSizeStr = atts.getValue(ATTRIBUTE_ICON_SIZE);

		if (nonNull(iconSizeStr)) {
			iconSize = Integer.valueOf(iconSizeStr);
		}
	}

	private void createMenu(Attributes atts) {
		String text = atts.getValue(ATTRIBUTE_TEXT);
		String iconPath = atts.getValue(ATTRIBUTE_ICON);
		String disabledIconPath = atts.getValue(ATTRIBUTE_DISABLED_ICON);
		String enabled = atts.getValue(ATTRIBUTE_ENABLED);
		String action = atts.getValue(ATTRIBUTE_ACTION);
		String visible = atts.getValue(ATTRIBUTE_VISIBLE);

		JMenu menu = new JMenu();
		menu.setActionCommand(action);

		if (nonNull(text)) {
			menu.setText(dict.contains(text) ? dict.get(text) : text);
		}
		if (nonNull(enabled)) {
			menu.setEnabled(Boolean.parseBoolean(enabled));
		}
		if (nonNull(visible)) {
			menu.setVisible(Boolean.parseBoolean(visible));
		}
		if (nonNull(iconPath)) {
			menu.setIcon(AwtResourceLoader.getIcon(iconPath, iconSize));
		}
		if (nonNull(disabledIconPath)) {
			menu.setDisabledIcon(AwtResourceLoader.getIcon(disabledIconPath, iconSize));
		}

		menuStack.push(menu);
	}

	private void createMenuItem(Attributes atts) {
		String text = atts.getValue(ATTRIBUTE_TEXT);
		String icon = atts.getValue(ATTRIBUTE_ICON);
		String disabledIcon = atts.getValue(ATTRIBUTE_DISABLED_ICON);
		String enabled = atts.getValue(ATTRIBUTE_ENABLED);
		String action = atts.getValue(ATTRIBUTE_ACTION);
		String type = atts.getValue(ATTRIBUTE_TYPE);

		if (isNull(type)) {
			item = new JMenuItem();
			buttonGroup = null;
		}
		else if (type.equals(ATTRIBUTE_TYPE_CHECKBOX)) {
			item = new JCheckBoxMenuItem();
			buttonGroup = null;

			if (Boolean.parseBoolean(atts.getValue(ATTRIBUTE_SELECTED))) {
				item.setSelected(true);
			}
		}
		else if (type.equals(ATTRIBUTE_TYPE_RADIO)) {
			item = new JRadioButtonMenuItem();

			if (Boolean.parseBoolean(atts.getValue(ATTRIBUTE_SELECTED))) {
				item.setSelected(true);
			}

			if (isNull(buttonGroup)) {
				buttonGroup = new ButtonGroup();
			}

			buttonGroup.add(item);
		}

		if (nonNull(text)) {
			item.setText(dict.contains(text) ? dict.get(text) : text);
		}
		if (nonNull(enabled)) {
			item.setEnabled(Boolean.parseBoolean(atts.getValue(ATTRIBUTE_ENABLED)));
		}
		if (nonNull(icon)) {
			item.setIcon(AwtResourceLoader.getIcon(icon));
		}
		if (nonNull(disabledIcon)) {
			item.setDisabledIcon(AwtResourceLoader.getIcon(disabledIcon));
		}

		item.setActionCommand(action);

		if (!hasAction) {
			item.addActionListener(actionListener);
			item.setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, new ComponentInputMap(item));

			// Register accelerator.
			AbstractAction acceleratorAction = new AbstractAction() {

				private static final long serialVersionUID = 5296358506728420284L;

				@Override
				public void actionPerformed(ActionEvent e) {
					window.processMenuAction(e);
				}
			};

			acceleratorAction.putValue(Action.ACTION_COMMAND_KEY, action);
		}

		menuStack.peek().add(item);
	}

	public CMenuBar getMenuBar() {
		return menuBar;
	}



	private class MenuActionListener extends AbstractAction {

		private static final long serialVersionUID = 6541130660919202689L;

		public void actionPerformed(ActionEvent e) {
			if (window != null) {
				window.processMenuAction(e);
			}
		}

	}

}
