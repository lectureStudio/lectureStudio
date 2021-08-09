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

import org.lecturestudio.core.PageMetrics;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.DocumentChangeListener;
import org.lecturestudio.core.model.listener.PageEditEvent;
import org.lecturestudio.core.model.listener.PageEditedListener;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.SlideView;
import org.lecturestudio.core.view.ViewType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class ThumbPanel extends JPanel {

	private final DocumentChangeListener docChangeListener = new DocumentChangeListener() {

		@Override
		public void documentChanged(Document document) {
			SwingUtilities.invokeLater(() -> onDocumentChanged(document));
		}

		@Override
		public void pageRemoved(final Page page) {
			SwingUtilities.invokeLater(() -> onPageRemoved(page));
		}

		@Override
		public void pageAdded(final Page page) {
			SwingUtilities.invokeLater(() -> onPageAdded(page));
		}
	};

	private final JScrollPane scrollPane;

	protected final JList<Page> list;

	private final PropertyChangeSupport pcs;

	private final EditPainter pageEditedHandler;

	private RenderController renderController;

	protected PageRenderer pageRenderer;

	private Page selectedPage;

	private Document document;

	private JPopupMenu popupMenu;


	public ThumbPanel() {
		super();

		setLayout(new BorderLayout());
		setFocusable(false);
		setIgnoreRepaint(true);

		pcs = new PropertyChangeSupport(this);

		list = new JList<>();
		list.setLayoutOrientation(JList.VERTICAL);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setIgnoreRepaint(true);
		list.setFocusable(false);
		list.addListSelectionListener(event -> {
			if (!event.getValueIsAdjusting()) {
				fireThumbSelected(list.getSelectedValue());
			}
		});
		list.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3 && nonNull(popupMenu)) {
					Page page = list.getModel().getElementAt(list.locationToIndex(e.getPoint()));

					for (int i = 0; i < popupMenu.getComponentCount(); i++) {
						JMenuItem item = (JMenuItem) popupMenu.getComponents()[i];
						item.putClientProperty("page", page);
					}

					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getViewport().add(list);

		pageEditedHandler = new EditPainter(list);

		add(scrollPane, BorderLayout.CENTER);

		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				resizeContent();
			}
		});
	}

	public void setRenderController(RenderController renderController) {
		this.renderController = renderController;
	}

	public void selectPage(Page page) {
		if (isNull(page)) {
			return;
		}

		Document doc = page.getDocument();

		if (doc.equals(document)) {
			setSelectedThumbnail(doc.getCurrentPage());
		}
	}

	private void fireThumbSelected(Page page) {
		Page oldPage = this.selectedPage;
		pcs.firePropertyChange("selectedSlide", oldPage, page);
	}

	@Override
	public void setBackground(Color color) {
		super.setBackground(color);

		if (nonNull(list)) {
			list.setBackground(color);
		}
	}

	public void setPopupMenu(JPopupMenu popupMenu) {
		this.popupMenu = popupMenu;
	}

	private Dimension getThumbSize(int width) {
		PageMetrics metrics = document.getPage(0).getPageMetrics();
		return new Dimension(width, (int) metrics.getHeight(width));
	}

	private void resizeContent() {
		if (isNull(document)) {
			return;
		}

		GraphicsConfiguration gc = getGraphicsConfiguration();

		if (nonNull(gc)) {
			pageRenderer.setDeviceTransform(gc.getDefaultTransform());
		}

		Dimension size = getThumbSize(scrollPane.getViewport().getWidth());

		pageRenderer.setPreferredSize(size);

		list.setFixedCellHeight(size.height);
		list.setFixedCellWidth(size.width);

		scrollToSelected();

		repaint();
	}

	private void createThumbnails() {
		int size = document.getPageCount();

		DefaultListModel<Page> listModel = new DefaultListModel<>();

		for (int i = 0; i < size; i++) {
			listModel.addElement(document.getPage(i));
		}

		list.setModel(listModel);
		list.setPrototypeCellValue(listModel.getElementAt(0));
	}

	private void setSelectedThumbnail(Page selectedSlide) {
		if (this.document != selectedSlide.getDocument()) {
			return;
		}
		if (this.selectedPage != selectedSlide) {
			if (nonNull(selectedPage)) {
				selectedPage.removePageEditedListener(pageEditedHandler);
			}

			selectedSlide.addPageEditedListener(pageEditedHandler);

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

	public void setDocument(Document doc, PresentationParameterProvider ppProvider) {
		if (nonNull(document)) {
			document.removeChangeListener(docChangeListener);
		}
		if (nonNull(doc)) {
			document = doc;

			pageRenderer = new PageRenderer(renderController, ppProvider);
			pageRenderer.setPreferredSize(getThumbSize(getPreferredSize().width));

			list.setCellRenderer(pageRenderer);

			createThumbnails();
			setSelectedThumbnail(doc.getCurrentPage());

			document.addChangeListener(docChangeListener);
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

	private void scrollToSelected() {
		final int index = list.getSelectedIndex();
		if (index < 0) {
			return;
		}

		final Rectangle thumbBounds = list.getCellBounds(index, index);
		if (thumbBounds.isEmpty()) {
			return;
		}

		JScrollBar bar = scrollPane.getVerticalScrollBar();
		bar.setValue(thumbBounds.y + thumbBounds.height);
	}

	private void onDocumentChanged(Document document) {
		SwingUtilities.invokeLater(() -> {
			createThumbnails();
			resizeContent();

			list.revalidate();
			list.repaint();
			list.setSelectedValue(document.getCurrentPage(), false);

			scrollToSelected();
		});
	}

	private void onPageAdded(Page page) {
		DefaultListModel<Page> model = (DefaultListModel<Page>) list.getModel();
		model.addElement(page);

		SwingUtilities.invokeLater(() -> {
			resizeContent();
			setSelectedThumbnail(page);
		});
	}

	private void onPageRemoved(Page page) {
		DefaultListModel<Page> model = (DefaultListModel<Page>) list.getModel();
		model.removeElement(page);

		SwingUtilities.invokeLater(this::resizeContent);
	}



	protected static class PageRenderer extends JComponent implements ListCellRenderer<Page>, SlideView {

		protected final static Color DEFAULT_BORDER_COLOR = Color.BLUE;

		private final static int BORDER_SIZE = 3;

		private final PresentationParameterProvider ppProvider;

		private final SlideRenderer renderer;

		private Page page;

		private boolean selected;

		private Color selectedBorderColor = DEFAULT_BORDER_COLOR;


		PageRenderer(RenderController renderController,
				PresentationParameterProvider ppProvider) {
			super();

			this.ppProvider = ppProvider;

			renderer = new SlideRenderer(ViewType.Preview);
			renderer.setRenderController(renderController);
		}

		public void setDeviceTransform(AffineTransform transform) {
			renderer.setDeviceTransform(transform);
		}

		@Override
		public void setPreferredSize(Dimension size) {
			if (nonNull(page)) {
				PageMetrics metrics = page.getPageMetrics();
				int width = size.width - 2 * (BORDER_SIZE + 1);
				int height = (int) metrics.getHeight(width) - 1;

				renderer.resizeBuffer(new Dimension2D(width, height));
			}

			super.setPreferredSize(size);
		}

		@Override
		public Component getListCellRendererComponent(
				JList<? extends Page> list, Page value, int index,
				boolean isSelected, boolean cellHasFocus) {

			selected = isSelected;

			setPage(value);

			return this;
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			renderPage();

			BufferedImage image = renderer.getImage();

			if (isNull(page) || isNull(image)) {
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
			imageTransform.translate(transform.getTranslateX(),
					transform.getTranslateY());

			if (selected) {
				g2d.setColor(selectedBorderColor);
				g2d.fillRect(0, 0, getWidth(), getHeight());
			}
			else {
				g2d.setColor(getBackground());
				g2d.fillRect(0, 0, getWidth(), getHeight());
				g2d.setColor(Color.LIGHT_GRAY);
				g2d.drawRect(BORDER_SIZE, BORDER_SIZE,
						getWidth() - BORDER_SIZE * 2,
						getHeight() - BORDER_SIZE * 2);
			}

			g2d.setTransform(imageTransform);
			g2d.drawImage(image,
					(int) (transform.getScaleX() * BORDER_SIZE + 1),
					(int) (transform.getScaleY() * BORDER_SIZE + 1), null);
			g2d.setTransform(transform);
		}

		@Override
		public void setPage(Page page) {
			if (this.page == page) {
				return;
			}

			this.page = page;

			if (nonNull(this.page)) {
				if (ppProvider != null) {
					renderer.setPresentationParameter(ppProvider.getParameter(page));
				}
				renderer.setPage(page);
			}
		}

		public void setSelectedBorderColor(Color borderColor) {
			selectedBorderColor = borderColor;
		}

		private void renderPage() {
			if (isNull(page) || getBounds().isEmpty()) {
				return;
			}

			int pageCount = page.getDocument().getPageCount();

			if (page.getPageNumber() > pageCount - 1) {
				// Avoid rendering of outdated documents.
				return;
			}

			try {
				renderer.renderPage();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}



	private static class EditPainter implements PageEditedListener {

		private final JList<Page> list;


		EditPainter(JList<Page> list) {
			this.list = list;
		}

		@Override
		public void pageEdited(final PageEditEvent event) {
			int index = list.getSelectedIndex();

			list.repaint(list.getCellBounds(index, index));
		}
	}
}
