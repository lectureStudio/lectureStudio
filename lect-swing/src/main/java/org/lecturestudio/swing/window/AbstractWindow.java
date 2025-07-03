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

package org.lecturestudio.swing.window;

import com.google.common.eventbus.Subscribe;

import java.awt.*;

import javax.swing.SwingUtilities;

import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.WindowView;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstract base class for window implementations that provides common window functionality.
 * Implements the WindowView interface and handles basic window operations.
 *
 * @author Alex Andres
 */
public abstract class AbstractWindow implements WindowView {

	/** Logger for this class. */
	private static final Logger LOG = LogManager.getLogger(AbstractWindow.class);

	/** Application context containing shared application resources. */
	private final ApplicationContext context;

	/** The underlying window instance. */
	private Window window;
	
	/** The document currently displayed in this window. */
	private Document document;
	
	/** The current page being displayed from the document. */
	private Page page;


	/**
	 * Creates the window instance with the specified graphics configuration.
	 *
	 * @param gc the graphics configuration to use may be null.
	 *
	 * @return the created window instance.
	 */
	abstract protected Window createWindow(GraphicsConfiguration gc);
	


	/**
	 * Creates a new window with the specified application context.
	 *
	 * @param context the application context.
	 */
	public AbstractWindow(ApplicationContext context) {
		this.context = context;

		init(null);
	}

	/**
	 * Creates a new window with the specified application context and graphics configuration.
	 *
	 * @param context the application context.
	 * @param gc      the graphics configuration to use.
	 */
	public AbstractWindow(ApplicationContext context, GraphicsConfiguration gc) {
		this.context = context;

		init(gc);
	}

	@Override
	public void show() {
		setVisible(true);
	}
	
	@Override
	public void hide() {
		setVisible(false);
	}
	
	@Override
	public void close() {
		dispose();
	}
	
	@Subscribe
	public void onEvent(final PageEvent event) {
		if (event.getType() == PageEvent.Type.SELECTED) {
			setPage(event.getPage());
		}
	}

	/**
	 * Returns the document currently displayed in this window.
	 *
	 * @return the current document.
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Sets the current page to be displayed in this window.
	 *
	 * @param page the page to be displayed.
	 */
	public void setPage(Page page) {
		this.page = page;
	}

	/**
	 * Returns the current page being displayed.
	 *
	 * @return the current page.
	 */
	protected Page getPage() {
		return page;
	}

	/**
	 * Sets the document to be displayed in this window and updates the current page.
	 * If the document is not null and has pages, the current page from the document
	 * will be set. Otherwise, the current page is set to null.
	 *
	 * @param doc the document to be displayed.
	 */
	protected void setDocument(Document doc) {
		this.document = doc;

		if (doc != null) {
			if (doc.getPageCount() > 0) {
				setPage(doc.getCurrentPage());
			}
		}
		else {
			setPage(null);
		}
	}

	/**
	 * Returns the application context containing shared application resources.
	 *
	 * @return the application context.
	 */
	protected ApplicationContext getContext() {
		return context;
	}

	/**
	 * Run this Runnable in the Swing Event Dispatching Thread (EDT). This method can be
	 * called whether the current thread is the EDT.
	 *
	 * @param runnable the code to be executed in the EDT.
	 */
	protected void invoke(Runnable runnable) {
		if (runnable == null)
			return;
		
		try {
			if (SwingUtilities.isEventDispatchThread()) {
				runnable.run();
			}
			else {
				SwingUtilities.invokeLater(runnable);
			}
		}
		catch (Exception e) {
			LOG.error("Swing thread interrupted: ", e);
		}
	}

	/**
	 * Sets the size of the window.
	 *
	 * @param width  the new width of the window in pixels.
	 * @param height the new height of the window in pixels.
	 */
	public void setSize(int width, int height) {
		window.setSize(new Dimension(width, height));
	}

	/**
	 * Sets the layout manager for this window.
	 *
	 * @param manager the layout manager to be used.
	 */
	public void setLayout(LayoutManager manager) {
		window.setLayout(manager);
	}

	/**
	 * Gets the current size of the window.
	 *
	 * @return the current dimensions of the window.
	 */
	public Dimension getSize() {
		return window.getSize();
	}

	/**
	 * Sets the bounds of the window.
	 *
	 * @param rect the rectangle specifying position and size.
	 */
	public void setBounds(Rectangle rect) {
		window.setBounds(rect);
	}

	/**
	 * Sets whether this window should always be on top of other windows.
	 *
	 * @param onTop true to set the window always on top, false otherwise.
	 */
	public void setAlwaysOnTop(boolean onTop) {
		window.setAlwaysOnTop(onTop);
	}

	/**
	 * Sets the visibility of this window.
	 *
	 * @param visible true to make the window visible, false to hide it.
	 */
	public void setVisible(boolean visible) {
		window.setVisible(visible);
	}

	/**
	 * Checks whether this window is currently visible.
	 *
	 * @return true if the window is visible, false otherwise.
	 */
	public boolean isVisible() {
		return window.isVisible();
	}

	/**
	 * Returns the underlying window instance.
	 *
	 * @return the underlying window object.
	 */
	public Window getWindow() {
		return window;
	}

	/**
	 * Disposes of this window, releasing all resources used by it.
	 */
	public void dispose() {
		window.dispose();
	}

	/**
	 * Sets the background color of this window.
	 *
	 * @param color the new background color.
	 */
	public void setBackground(Color color) {
		window.setBackground(color);
	}

	private void init(GraphicsConfiguration gc) {
		window = createWindow(gc);
	}
}
