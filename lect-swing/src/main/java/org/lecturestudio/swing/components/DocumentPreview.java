/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

import static java.util.Objects.isNull;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.ViewType;

public class DocumentPreview extends JPanel {

	private static final Color OVERLAY_COLOR = new Color(226, 232, 240, 170);

	private SlideView slideView;

	private Resizable resizable;


	public DocumentPreview() {
		super();

		initialize();
	}

	public void setPage(Page page, PresentationParameter parameter) {
		// Set size to fit the slide-view without empty space around.
		Dimension size = getPreferredSize();
		double width = size.getWidth();
		size.setSize(width, page.getPageMetrics().getHeight(width));
		setPreferredSize(size);

		slideView.parameterChanged(page, parameter);
		slideView.setPage(page);
		slideView.renderPage();
	}

	public void setRenderController(RenderController renderer) {
		slideView.setPageRenderer(renderer);
	}

	public void setOverlayBounds(ObjectProperty<Rectangle2D> bounds) {
		setResizableBounds(bounds.get());

		// Bind bi-directional.
		resizable.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				setPropertyBounds(resizable.getBounds(), bounds);
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				setPropertyBounds(resizable.getBounds(), bounds);
			}
		});

		bounds.addListener((o, oldValue, newValue) -> {
			setResizableBounds(newValue);
		});
	}

	public void showOverlay(boolean show) {
		resizable.setVisible(show);
	}

	private void setPropertyBounds(Rectangle bounds,
			ObjectProperty<Rectangle2D> property) {
		property.set(convertToPageBounds(bounds));
	}

	private void setResizableBounds(Rectangle2D bounds) {
		SwingUtilities.invokeLater(() -> {
			resizable.setBounds(convertToViewBounds(bounds));
			resizable.revalidate();
			resizable.repaint();
		});
	}

	private Rectangle2D convertToPageBounds(Rectangle bounds) {
		Rectangle parentBounds = getBounds();

		if (parentBounds.isEmpty()) {
			parentBounds.setSize(getPreferredSize());
		}

		Rectangle2D pageRect = slideView.getPage().getPageRect();
		double sx = pageRect.getWidth() / parentBounds.getWidth();
		double sy = pageRect.getHeight() / parentBounds.getHeight();
		double x = Math.round(bounds.getX() * sx);
		double y = Math.round(bounds.getY() * sy);
		double w = Math.round(bounds.getWidth() * sx);
		double h = Math.round(bounds.getHeight() * sy);

		return new Rectangle2D(x, y, w, h);
	}

	private Rectangle convertToViewBounds(Rectangle2D bounds) {
		Rectangle parentBounds = getBounds();

		if (parentBounds.isEmpty()) {
			parentBounds.setSize(getPreferredSize());
		}
		if (isNull(bounds)) {
			parentBounds.setLocation(0, 0);
			return parentBounds;
		}

		Rectangle2D pageRect = slideView.getPage().getPageRect();
		double sx = parentBounds.getWidth() / pageRect.getWidth();
		double sy = parentBounds.getHeight() / pageRect.getHeight();
		double x = Math.round(bounds.getX() * sx);
		double y = Math.round(bounds.getY() * sy);
		double w = Math.round(bounds.getWidth() * sx);
		double h = Math.round(bounds.getHeight() * sy);

		Rectangle viewBounds = new Rectangle();
		viewBounds.setFrame(x, y, w, h);

		return viewBounds;
	}

	private void initialize() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		slideView = new SlideView();
		slideView.setViewType(ViewType.User);

		JPanel overlay = new JPanel();
		overlay.setBackground(OVERLAY_COLOR);

		resizable = new Resizable(overlay);

		JPanel viewPanel = new JPanel(null);
		viewPanel.add(resizable);
		viewPanel.add(slideView);
		viewPanel.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				JComponent component = (JComponent) e.getComponent();

				slideView.setBounds(component.getBounds());
			}
		});

		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent me) {
				requestFocus();
				resizable.repaint();
			}
		});

		add(viewPanel);
	}
}
