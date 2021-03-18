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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.util.Stack;

import javax.swing.JPanel;

public class GlassPane extends JPanel implements KeyListener {

	private final Stack<Component> backlog;


	public GlassPane() {
		super();

		setLayout(new GridBagLayout());
		setBackground(new Color(64, 64, 64, 180));
		setOpaque(false);
		setFocusTraversalKeysEnabled(false);

		// Disable Mouse and Key events.
		addMouseListener(new MouseAdapter() {});
		addMouseMotionListener(new MouseMotionAdapter() {});
		addKeyListener(this);

		backlog = new Stack<>();
	}

	@Override
	public Component add(Component comp) {
		if (getComponentCount() > 0) {
			backlog.push(getComponent(0));
			remove(0);
		}

		Component added = super.add(comp);

		revalidate();

		return added;
	}

	@Override
	public void remove(Component comp) {
		super.remove(comp);

		if (!backlog.empty()) {
			add(backlog.pop());
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// Ignore
	}

	@Override
	public void keyPressed(KeyEvent e) {
		e.consume();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		e.consume();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.setColor(getBackground());
		g.fillRect(0, 0, getSize().width, getSize().height);
	}
}
