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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.model.shape.Shape;

public abstract class TextInputPageObject<T extends Shape> extends PageObject<T> {

	private JComponent content;

	private JComponent header;


	abstract protected Color getThemeColor();

	abstract protected JComponent createContent();

	abstract protected void onRelocateShape(Point2D location);


	public TextInputPageObject() {
		super();

		// Initialize the PageObject in the AWT event dispatching thread.
		if (SwingUtilities.isEventDispatchThread()) {
			initialize();
		}
		else {
			Lock lock = new ReentrantLock();
			Condition condition = lock.newCondition();
			lock.lock();

			try {
				SwingUtilities.invokeLater(() -> {
					initialize();

					lock.lock();

					try {
						condition.signal();
					}
					finally {
						lock.unlock();
					}
				});

				condition.await();
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			finally {
				lock.unlock();
			}
		}
	}

	@Override
	public void setFocus(boolean focus) {
		if (getFocus() == focus) {
			return;
		}

		super.setFocus(focus);

		content.setVisible(focus);

		updateContentSize();
	}

	protected void dispose() {
		if (nonNull(getOnClose())) {
			getOnClose().execute();
		}
	}

	protected void updateContentSize() {
		Dimension contentSize = content.getPreferredSize();
		Insets insets = getInsets();

		int borderWidth = insets.left + insets.right;
		int borderHeight = insets.top + insets.bottom;

		if (header.isVisible()) {
			borderHeight += header.getPreferredSize().height;
		}

		int minWidth = borderWidth + header.getPreferredSize().width;
		int minHeight = borderHeight;

		int boxWidth = Math.max(minWidth, contentSize.width + borderWidth);
		int boxHeight = Math.max(minHeight, contentSize.height + borderHeight);

		setSize(boxWidth, boxHeight);
	}

	private void relocatePageObject(int dx, int dy) {
		AffineTransform transform = getPageTransform();
		Shape shape = getPageShape();
		Point location = getLocation();

		super.setLocation(location.x + dx, location.y + dy);

		Point2D shapeLocation = shape.getBounds().getLocation();
		shapeLocation.set(shapeLocation.getX() + dx / transform.getScaleX(),
				shapeLocation.getY() + dy / transform.getScaleY());

		onRelocateShape(shapeLocation);
	}

	private void initialize() {
		setLayout(new BorderLayout());
		setBorder(new LineBorder(getThemeColor(), 2));
		setOpaque(false);
		setFocusable(true);

		// Create box header.
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.anchor = GridBagConstraints.EAST;

		header = new JPanel(new GridBagLayout());
		header.setBackground(getThemeColor());
		header.setVisible(false);

		JButton closeButton = new JButton("X");
		closeButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		closeButton.setBackground(header.getBackground());
		closeButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				dispose();
			}
		});

		header.add(closeButton, c);

		content = createContent();

		// add(header, BorderLayout.NORTH);
		add(content, BorderLayout.CENTER);

		initDefaultListeners();
		updateContentSize();
	}

	private void initDefaultListeners() {
		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
				removeAncestorListener(this);

				doLayout();
				updateToTransform(getPageTransform());
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {

			}

			@Override
			public void ancestorMoved(AncestorEvent event) {

			}
		});

		content.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					getParent().requestFocus();
					setFocus(false);

					e.consume();
				}
			}
		});

		MouseActionHandler mouseHandler = new MouseActionHandler();
		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseHandler);
	}



	private class MouseActionHandler extends MouseAdapter {

		private int lastX = 0;
		private int lastY = 0;


		@Override
		public void mousePressed(MouseEvent e) {
			lastX = e.getX();
			lastY = e.getY();

			// Copy page object.
			int mask = MouseEvent.CTRL_DOWN_MASK;

			if ((e.getModifiersEx() & mask) == mask) {
				if (nonNull(getOnCopy())) {
					getOnCopy().execute();
				}
			}

			setFocus(true);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int dx = e.getX() - lastX;
			int dy = e.getY() - lastY;

			relocatePageObject(dx, dy);
		}
	}
}
