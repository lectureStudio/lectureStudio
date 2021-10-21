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
/* based on the code from the Pumpernickel project under the following licence: */
/*
 * This software is released as part of the Pumpernickel project.
 *
 * All com.pump resources in the Pumpernickel project are distributed under the
 * MIT License:
 * https://raw.githubusercontent.com/mickleness/pumpernickel/master/License.txt
 *
 * More information about the Pumpernickel project is available here:
 * https://mickleness.github.io/pumpernickel/
 */
package org.lecturestudio.swing.components.toolbar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

public class CustomizedToolbarOptions extends JPanel {

	private static final long serialVersionUID = 8916594208363548892L;

	/**
	 * Analogous to the toolbar's list of components, except a MockComponent is used for each object.
	 */
	public MockComponent[] componentList;

	private final CustomizedToolbar toolbar;

	public JButton closeButton;

	private static final DragSource dragSource = DragSource.getDefaultDragSource();
	private final DragSourceListener dragSourceListener = new DragSourceAdapter() {
		@Override
		public void dragDropEnd(DragSourceDropEvent dsde) {
			toolbar.endDrag(dsde);
		}
	};
	DragGestureListener dragGestureListener = new DragGestureListener() {

		public void dragGestureRecognized(DragGestureEvent dge) {
			Point p = dge.getDragOrigin();
			MockComponent mc = (MockComponent) dge.getComponent();
			Transferable transferable = new MockComponentTransferable(mc);
			BufferedImage bi = mc.getBufferedImage();
			if (mc.getName().equals("-")) {
				toolbar.draggingComponent = toolbar.getNewSeparatorName();
			} else if (mc.getName().equals(" ")) {
				toolbar.draggingComponent = toolbar.getNewSpaceName();
			} else if (mc.getName().equals("\t")) {
				toolbar.draggingComponent = toolbar.getNewFlexibleSpaceName();
			} else {
				toolbar.draggingComponent = mc.getName();
			}
			toolbar.draggingFromToolbar = false;
			dge.startDrag(DragSource.DefaultMoveDrop, bi, new Point(-p.x, -p.y), transferable, dragSourceListener);
		}

	};

	public CustomizedToolbarOptions(CustomizedToolbar t, int maxWidth, ResourceBundle resourceBundle) {
		toolbar = t;
		JComponent[] options = t.getPossibleComponents();
		componentList = new MockComponent[options.length + 3];
		for (int a = 0; a < options.length; a++) {
			componentList[a] = (new MockComponent(options[a]));
			componentList[a].setVisible(!options[a].isVisible());
		}
		JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
		separator.setUI(new MacToolbarSeparatorUI());
		separator.setName("-");
		Dimension separatorSize = separator.getPreferredSize();
		separatorSize.height = toolbar.minimumHeight;
		separator.setSize(separatorSize);
		separator.setPreferredSize(separatorSize);
		componentList[componentList.length - 3] = new MockComponent(separator);

		SpaceComponent space = new SpaceComponent(toolbar, false);
		space.setName(" ");
		componentList[componentList.length - 2] = new MockComponent(space);

		SpaceComponent flexSpace = new SpaceComponent(toolbar, true);
		flexSpace.setName("\t");
		componentList[componentList.length - 1] = new MockComponent(flexSpace);

		for (MockComponent mockComponent : componentList) {
			dragSource.createDefaultDragGestureRecognizer(mockComponent, DnDConstants.ACTION_MOVE, dragGestureListener);
		}

		GridBagConstraints c = new GridBagConstraints();
		int a = 0;
		JPanel componentPanel = new JPanel(new GridBagLayout());
		while (a < componentList.length) {
			JPanel row = new JPanel(new GridBagLayout());
			c.gridy = componentPanel.getComponentCount();
			c.gridx = 0;
			c.weightx = 1;
			c.weighty = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0, 0, 0, 0);
			componentPanel.add(row, c);
			int width = 0;
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1;
			c.weighty = 1;
			c.insets = new Insets(5, 5, 5, 5);
			while (width < maxWidth && a < componentList.length) {
				Dimension d = componentList[a].getPreferredSize();
				if (d.width + 6 > maxWidth) {
					break;
				}
				row.add(componentList[a], c);
				c.gridx++;
				width += d.width + c.insets.left + c.insets.right;
				a++;
			}
		}

		setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy++;
		c.insets = new Insets(20, 20, 10, 20);
		add(componentPanel, c);
		c.fill = GridBagConstraints.NONE;
		c.gridy++;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(10, 20, 20, 20);
		closeButton = new JButton(resourceBundle.getString("button.close"));
		add(closeButton, c);

		setOpaque(true);
		setBackground(this, Color.white);
	}

	private static void setBackground(Component c, Color color) {
		c.setBackground(color);
		if (c instanceof Container) {
			Container c2 = (Container) c;
			for (int a = 0; a < c2.getComponentCount(); a++) {
				setBackground(c2.getComponent(a), color);
			}
		}
	}
}
