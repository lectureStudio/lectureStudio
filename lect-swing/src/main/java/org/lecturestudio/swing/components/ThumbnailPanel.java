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

import com.google.common.eventbus.Subscribe;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.PageMetrics;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.PageEditEvent;
import org.lecturestudio.core.model.listener.PageEditEvent.Type;
import org.lecturestudio.core.model.listener.PageEditedListener;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.TeXShape;
import org.lecturestudio.core.model.shape.TextShape;
import org.lecturestudio.core.render.RenderThread;
import org.lecturestudio.core.render.RenderThreadTask;
import org.lecturestudio.core.view.SlideView;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.swing.renderer.ViewRenderer;

public class ThumbnailPanel extends JScrollPane {

	private static final long serialVersionUID = -2964160936646334803L;

	private final ApplicationContext context;

	private final RenderController renderController;

	private final JList<Page> list;

	private final PropertyChangeSupport pcs;

	private Page selectedPage;

	private Document document;

	private JPopupMenu popupMenu;


	public ThumbnailPanel(ApplicationContext context, RenderController renderController) {
		super();

		setFocusable(false);
		setIgnoreRepaint(true);
		getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		this.context = context;
		this.renderController = renderController;

		pcs = new PropertyChangeSupport(this);

		list = new JList<>();
		list.setLayoutOrientation(JList.VERTICAL);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setIgnoreRepaint(true);
		list.addListSelectionListener(event -> {
			if (!event.getValueIsAdjusting()) {
				fireThumbSelected(list.getSelectedValue());
			}
		});

		getViewport().add(list);

		ApplicationBus.register(this);

		initListeners();
	}

	private void initListeners() {
		addComponentListener(new ComponentAdapter() {

			public void componentResized(ComponentEvent e) {
				resizeContent();
			}
		});
	}

	@Subscribe
	public void onEvent(final PageEvent event) {
		SwingUtilities.invokeLater(() -> {
			if (event.getType() == PageEvent.Type.CREATED) {
				pageCreated(event.getPage());
			}
			else if (event.getType() == PageEvent.Type.REMOVED) {
				pageRemoved(event.getPage());
			}
			else if (event.getType() == PageEvent.Type.SELECTED) {
				Page page = event.getPage();

				if (!page.equals(selectedPage)) {
					setSelectedThumbnail(page);
				}
			}
		});
	}

	private void fireThumbSelected(Page page) {
		Page oldPage = this.selectedPage;
		pcs.firePropertyChange("selectedSlide", oldPage, page);
	}

	@Override
	public void setBackground(Color color) {
		super.setBackground(color);

		if (list != null) {
			list.setBackground(color);
		}
	}

	public void setPopupMenu(JPopupMenu popupMenu) {
		this.popupMenu = popupMenu;
	}

	private void resizeContent() {
		if (document == null) {
			return;
		}

		int width = list.getWidth() - 2 * 3;
		PageMetrics metrics = getDocument().getPages().get(0).getPageMetrics();
		Dimension size = new Dimension(width, (int) metrics.getHeight(width));

		size.height += 3;

		list.setCellRenderer(new PageRenderer(context, renderController, size));
		list.setFixedCellWidth(width);

		scrollToSelected();
	}

	private void createThumbnails() {
		int size = document.getPageCount();

		DefaultListModel<Page> listModel = new DefaultListModel<>();

		for (int i = 0; i < size; i++) {
			listModel.addElement(document.getPage(i));
		}

		list.setModel(listModel);
		list.setPrototypeCellValue(listModel.getElementAt(0));
		list.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					Page page = list.getModel().getElementAt(list.locationToIndex(e.getPoint()));

					for (int i = 0; i < popupMenu.getComponentCount(); i++) {
						JMenuItem item = (JMenuItem) popupMenu.getComponents()[i];
						item.putClientProperty("page", page);
					}

					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
	}

	private void setSelectedThumbnail(Page selectedSlide) {
		if (this.document != selectedSlide.getDocument())
			return;

		if (this.selectedPage != selectedSlide) {
			DefaultListModel<Page> listModel = (DefaultListModel<Page>) list.getModel();
			int index = document.getPageIndex(selectedSlide);

			if (listModel.getSize() <= index) {
				return;
			}

			this.selectedPage = selectedSlide;

			list.setSelectedValue(selectedSlide, false);

			scrollToSelected();
		}
	}

	public void reload() {
		createThumbnails();
		setSelectedThumbnail(document.getCurrentPage());

		updateUI();

		dispatchEvent(new ComponentEvent(this, ComponentEvent.COMPONENT_RESIZED));

		resizeContent();
	}

	public void setDocument(Document doc) {
		this.document = doc;

		if (nonNull(document)) {
			createThumbnails();
			setSelectedThumbnail(doc.getCurrentPage());
		}
	}

	public Document getDocument() {
		return document;
	}

	public void addSelectedSlideChangedListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener("selectedSlide", listener);
	}

	public void removeSelectedSlideChangedListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener("selectedSlide", listener);
	}

	private void pageCreated(final Page page) {
		if (!document.equals(page.getDocument())) {
			return;
		}

		DefaultListModel<Page> listModel = (DefaultListModel<Page>) list.getModel();
		listModel.addElement(page);

		resizeContent();
		setSelectedThumbnail(page);
	}

	private void pageRemoved(final Page page) {
		if (!document.equals(page.getDocument())) {
			return;
		}

		DefaultListModel<Page> listModel = (DefaultListModel<Page>) list.getModel();

		for (int i = 0; i < listModel.getSize(); i++) {
			Page modelPage = listModel.get(i);

			if (modelPage.equals(page)) {
				listModel.removeElementAt(i);
				break;
			}
		}

		resizeContent();
	}

	private void scrollToSelected() {
		final int index = list.getSelectedIndex();
		if (index < 0) {
			return;
		}

		final Rectangle thumbBounds = list.getCellBounds(index, index);
		if (thumbBounds.isEmpty()) {
			return;
		}

		JScrollBar bar = getVerticalScrollBar();
		bar.setValue(thumbBounds.y + thumbBounds.height);
	}



	private static class PageRenderer extends JPanel implements ListCellRenderer<Page>, SlideView {

		private static final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		private static final GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();

		private static final RenderThread renderThread = new RenderThread();

		private static final int BORDER_WIDTH = 3;

		private final ApplicationContext context;

		private final Dimension size;

		private final EditPainter pageEditedHandler = new EditPainter();

		private final ViewRenderer renderer;

		private Page page;

		private boolean selected;


		static {
			try {
				renderThread.start();
			}
			catch (ExecutableException e) {
				e.printStackTrace();
			}
		}


		PageRenderer(ApplicationContext context, RenderController renderController, Dimension size) {
			super();

			this.context = context;
			this.size = size;

			renderer = new ViewRenderer(ViewType.Preview, context.getConfiguration());
			renderer.setDeviceTransform(gc.getDefaultTransform());
			renderer.setRenderController(renderController);
		}

		@Override
		public Dimension getPreferredSize() {
			return size;
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends Page> list, Page value, int index, boolean isSelected,
													  boolean cellHasFocus) {

			selected = isSelected;

			setPage(value);

			return this;
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			if (renderer == null) {
				return;
			}

			renderPage();

			BufferedImage image = renderer.getImage();

			if (page == null || image == null) {
				return;
			}

			/*
			 * HiDPI scaling:
			 * Set to Identity transform, since the renderer scales the image
			 * to the appropriate size.
			 */

			Graphics2D g2d = (Graphics2D) g;
			final AffineTransform transform = g2d.getTransform();
			final AffineTransform imageTransform = new AffineTransform();
			imageTransform.translate(transform.getTranslateX(), transform.getTranslateY());

			g2d.setColor(getBackground());
			g2d.fillRect(0, 0, getWidth(), getHeight());
			g2d.setTransform(imageTransform);
			g2d.drawImage(image, BORDER_WIDTH, BORDER_WIDTH, null);
			g2d.setTransform(transform);

			if (selected) {
				Stroke oldStroke = g2d.getStroke();

				g2d.setColor(Color.BLUE);
				g2d.setStroke(new BasicStroke(BORDER_WIDTH));
				g2d.drawRect(0, 0, getWidth(), getHeight());
				g2d.setStroke(oldStroke);
			}
		}

		@Override
		public void setPage(Page page) {
			if (this.page == page) {
				return;
			}
			if (this.page != null) {
				this.page.removePageEditedListener(pageEditedHandler);
			}
			if (page == null) {
				this.page = null;
			}
			else {
				this.page = page;

				if (renderer != null) {
					renderer.setPage(page);
					renderer.setParameter(context.getPagePropertyPropvider(ViewType.Preview));
				}

//				page.addPageEditedListener(pageEditedHandler);
			}
		}

		private void renderPage() {
			if (page == null || getBounds().isEmpty()) {
				return;
			}
			if (renderer != null) {
				try {
					Dimension pageSize = new Dimension(size);

					renderer.renderPage(page, pageSize);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}



		private class EditPainter implements PageEditedListener, RenderThreadTask {

			private final BlockingQueue<PageEditEvent> queue = new LinkedBlockingQueue<>();


			@Override
			public void pageEdited(final PageEditEvent event) {
				queue.offer(event);

				renderThread.onTask(pageEditedHandler);
			}

			@Override
			public void render() throws Exception {
				PageEditEvent event = queue.poll();

				if (event == null) {
					return;
				}

				Shape shape = event.getShape();

				if (shape == null) {
					renderer.renderForeground();
					repaint();
					return;
				}

				PageEditEvent.Type type = event.getType();

				if (type == Type.CLEAR || type == Type.SHAPES_ADDED
						|| type == Type.SHAPE_ADDED
						|| type == Type.SHAPE_REMOVED
						|| shape instanceof TextShape
						|| shape instanceof TeXShape) {

					renderer.renderForeground();
				}
				else {
					org.lecturestudio.core.geometry.Rectangle2D clipRect = event.getDirtyArea();

					// TODO: fix zoom clip rect
					renderer.render(page, shape, clipRect);
				}

				repaint();
			}
		}
	}

}
