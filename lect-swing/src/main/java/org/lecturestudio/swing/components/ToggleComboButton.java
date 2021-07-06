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

import static java.util.Objects.nonNull;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuListener;

import org.lecturestudio.swing.AwtResourceLoader;

public class ToggleComboButton<T> extends JToggleButton {

	public interface ItemChangeListener<T> extends EventListener {

		void itemChanged(T item);

	}



	private final List<ItemChangeListener<T>> listeners;

	private final JLabel iconLabel;

	private final JPopupMenu popup;

	private T selectedItem;

	private Component content;

	private Icon icon;

	private Icon selectedIcon;

	protected ComboBoxModel<T> dataModel;


	public ToggleComboButton() {
		setLayout(new BorderLayout(0, 0));

		listeners = new ArrayList<>();

		popup = new JPopupMenu();

		iconLabel = new JLabel();
		iconLabel.setBorder(BorderFactory.createEmptyBorder());
		iconLabel.setOpaque(false);

		JLabel arrowButton = new JLabel(AwtResourceLoader.getIcon("arrow-down-thin.svg"));
		arrowButton.setFocusable(false);
		arrowButton.setOpaque(false);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;

		JPanel container = new JPanel();
		container.setLayout(new GridBagLayout());
		container.setBorder(new EmptyBorder(0, 5, 0, 5));
		container.setOpaque(false);
		container.add(arrowButton, constraints);
		container.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (nonNull(content)) {
					if (popup.isVisible()) {
						hidePopup();
					}
					else {
						showPopup();
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				MouseEvent event = new MouseEvent(ToggleComboButton.this,
						e.getID(), e.getWhen(), e.getModifiersEx(), e.getX(),
						e.getY(), e.getClickCount(), true);

				ToggleComboButton.this.dispatchEvent(event);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				MouseEvent event = new MouseEvent(ToggleComboButton.this,
						e.getID(), e.getWhen(), e.getModifiersEx(), e.getX(),
						e.getY(), e.getClickCount(), true);

				ToggleComboButton.this.dispatchEvent(event);
			}
		});

		add(iconLabel, BorderLayout.WEST);
		add(container, BorderLayout.EAST);

		initListeners();
	}

	@Override
	public void setIcon(Icon icon) {
		this.icon = icon;

		selectIcon();
	}

	@Override
	public void setSelectedIcon(Icon icon) {
		this.selectedIcon = icon;

		selectIcon();
	}

	@Override
	public void setVisible(boolean visible) {
		if (!visible) {
			hidePopup();
		}

		super.setVisible(visible);
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (nonNull(content)) {
			content.setEnabled(enabled);
		}

		super.setEnabled(enabled);
	}

	public void setContent(Component content) {
		this.content = content;

		popup.add(content);
	}

	/**
	 * Returns the current selected item.
	 *
	 * @return the current selected item.
	 */
	public T getSelectedItem() {
		return selectedItem;
	}

	/**
	 * Sets the selected item in the combo button display area to the item in
	 * the argument.
	 * <p>
	 * If this constitutes a change in the selected item,
	 * <code>ItemListener</code>s added to the combo button will be notified
	 * with an <code>ItemEvent</code>.
	 * <p>
	 * <code>ActionListener</code>s added to the combo box will be notified
	 * with an <code>ActionEvent</code> when this method is called.
	 *
	 * @param item The item to select; use <code>null</code> to clear the
	 *             selection.
	 */
	public void setSelectedItem(T item) {
		Object oldItem = selectedItem;

		if (Objects.equals(oldItem, item)) {
			return;
		}

		selectedItem = item;

		selectedItemChanged();
		fireActionEvent();
	}

	public void addItemChangeListener(ItemChangeListener<T> listener) {
		listeners.add(listener);
	}

	public void removeItemChangeListener(ItemChangeListener<T> listener) {
		listeners.remove(listener);
	}

	/**
	 * Adds a <code>JPopupMenu</code> listener which will listen to notification
	 * messages from the popup portion of the combo button.
	 *
	 * @param listener The <code>PopupMenuListener</code> to add.
	 */
	public void addPopupMenuListener(PopupMenuListener listener) {
		popup.addPopupMenuListener(listener);
	}

	/**
	 * Removes a <code>PopupMenuListener</code>.
	 *
	 * @param listener The <code>PopupMenuListener</code> to remove.
	 */
	public void removePopupMenuListener(PopupMenuListener listener) {
		popup.removePopupMenuListener(listener);
	}

	protected void showPopup() {
		if (!popup.isVisible()) {
			popup.show(this, 0, getHeight());
		}
	}

	protected void hidePopup() {
		popup.setVisible(false);
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type.
	 */
	protected void fireActionEvent() {
		ActionListener[] listeners = listenerList.getListeners(ActionListener.class);
		AWTEvent currentEvent = EventQueue.getCurrentEvent();
		long mostRecentEventTime = System.currentTimeMillis();
		int modifiers = 0;

		if (currentEvent instanceof InputEvent) {
			modifiers = ((InputEvent) currentEvent).getModifiersEx();
		}
		else if (currentEvent instanceof ActionEvent) {
			modifiers = ((ActionEvent) currentEvent).getModifiers();
		}

		ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
				getActionCommand(), mostRecentEventTime, modifiers);

		for (ActionListener listener : listeners) {
			listener.actionPerformed(e);
		}
	}

	/**
	 * This protected method is implementation specific. Do not access directly
	 * or override.
	 */
	protected void selectedItemChanged() {
		if (nonNull(selectedItem)) {
			for (ItemChangeListener<T> listener : listeners) {
				listener.itemChanged(selectedItem);
			}
		}
	}

	private void selectIcon() {
		if (isSelected() && nonNull(selectedIcon)) {
			iconLabel.setIcon(selectedIcon);
		}
		else if (nonNull(icon)) {
			iconLabel.setIcon(icon);
		}
	}

	private void selectMouseOverIcon(boolean entered) {
		if ((isSelected() || entered) && nonNull(selectedIcon)) {
			iconLabel.setIcon(selectedIcon);
		}
		else if (nonNull(icon)) {
			iconLabel.setIcon(icon);
		}
	}

	private void initListeners() {
		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				selectMouseOverIcon(true);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				selectMouseOverIcon(false);
			}
		});

		addItemListener(event -> {
			if (event.getStateChange() == ItemEvent.DESELECTED) {
				selectIcon();
			}
		});
	}
}
