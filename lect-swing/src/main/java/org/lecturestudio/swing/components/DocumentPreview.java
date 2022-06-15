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
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.swing.AwtResourceLoader;
import org.lecturestudio.swing.util.SwingUtils;

public class DocumentPreview extends JPanel {

	private static final Color OVERLAY_COLOR = new Color(20, 184, 166, 170);

	private SlideView slideView;

	private Resizable resizable;

	private JPanel viewPanel;

	private JPanel buttonPanel;

	private JButton openButton;

	private JButton resetButton;


	@Inject
	public DocumentPreview(ResourceBundle bundle) {
		super();

		initialize(bundle);
	}

	public void setOpenTemplateAction(Action action) {
		SwingUtils.bindAction(openButton, action);
	}

	public void setResetTemplateAction(Action action) {
		SwingUtils.bindAction(resetButton, action);
	}

	public void setPage(Page page, PresentationParameter parameter) {
		// Set size to fit the slide-view without empty space around.
		Dimension size = getPreferredSize();
		double height = size.getHeight() - buttonPanel.getPreferredSize().getHeight();
		size.setSize(page.getPageMetrics().getWidth(height), height);

		viewPanel.setPreferredSize(size);
		viewPanel.setSize(size);
		viewPanel.setMaximumSize(size);
		viewPanel.revalidate();

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
		Rectangle parentBounds = viewPanel.getBounds();

		if (parentBounds.isEmpty()) {
			parentBounds.setSize(viewPanel.getPreferredSize());
		}

		double x = bounds.getX() / parentBounds.getWidth();
		double y = bounds.getY() / parentBounds.getHeight();
		double w = bounds.getWidth() / parentBounds.getWidth();
		double h = bounds.getHeight() / parentBounds.getHeight();

		return new Rectangle2D(x, y, w, h);
	}

	private Rectangle convertToViewBounds(Rectangle2D bounds) {
		Rectangle parentBounds = viewPanel.getBounds();

		if (parentBounds.isEmpty()) {
			parentBounds.setSize(viewPanel.getPreferredSize());
		}
		if (isNull(bounds)) {
			parentBounds.setLocation(0, 0);
			return parentBounds;
		}

		double x = bounds.getX() * parentBounds.getWidth();
		double y = bounds.getY() * parentBounds.getHeight();
		double w = bounds.getWidth() * parentBounds.getWidth();
		double h = bounds.getHeight() * parentBounds.getHeight();

		Rectangle viewBounds = new Rectangle();
		viewBounds.setFrame(x, y, w, h);

		return viewBounds;
	}

	private void initialize(ResourceBundle bundle) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		slideView = new SlideView();
		slideView.setViewType(ViewType.User);

		JPanel overlay = new JPanel();
		overlay.setBackground(OVERLAY_COLOR);

		resizable = new Resizable(overlay);

		viewPanel = new JPanel(null);
		viewPanel.add(resizable);
		viewPanel.add(slideView);
		viewPanel.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				JComponent component = (JComponent) e.getComponent();

				slideView.setBounds(component.getBounds());
			}
		});

		openButton = new JButton(bundle.getString("template.settings.open"));
		openButton.setIcon(AwtResourceLoader.getIcon("folder-open.svg", 20));

		resetButton = new JButton(bundle.getString("template.settings.reset"));
		resetButton.setIcon(AwtResourceLoader.getIcon("reset.svg", 20));

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(resetButton);
		buttonPanel.add(Box.createHorizontalStrut(5));
		buttonPanel.add(openButton);
		buttonPanel.add(Box.createHorizontalGlue());

		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent me) {
				requestFocus();
				resizable.repaint();
			}
		});
		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				slideView.renderPage();
			}
		});

		add(viewPanel);
		add(buttonPanel);
	}
}
