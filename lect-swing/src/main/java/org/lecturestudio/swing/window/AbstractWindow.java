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

public abstract class AbstractWindow implements WindowView {

	private static final Logger LOG = LogManager.getLogger(AbstractWindow.class);

	private final ApplicationContext context;

	private Window window;
	
	private Document document;
	
	private Page page;
	

	abstract protected Window createWindow(GraphicsConfiguration gc);
	


	public AbstractWindow(ApplicationContext context) {
		this.context = context;

		init(null);
	}

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
	
	public Document getDocument() {
		return document;
	}
	
	protected Page getPage() {
		return page;
	}

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
	
	public void setPage(Page page) {
		this.page = page;
	}
	
	protected ApplicationContext getContext() {
		return context;
	}
	
	/**
	 * Run this Runnable in the Swing Event Dispatching Thread (EDT). This method can be
	 * called whether or not the current thread is the EDT.
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

	private void init(GraphicsConfiguration gc) {
		window = createWindow(gc);
	}

	public void setSize(int width, int height) {
		window.setSize(new Dimension(width, height));
	}

	public void setLayout(LayoutManager manager) {
		window.setLayout(manager);
	}

	public Dimension getSize() {
		return window.getSize();
	}

	public void setBounds(Rectangle rect) {
		window.setBounds(rect);
	}

	public void setAlwaysOnTop(boolean onTop) {
		window.setAlwaysOnTop(onTop);
	}

	public void setVisible(boolean visible) {
		window.setVisible(visible);
	}

	public boolean isVisible() {
		return window.isVisible();
	}

	public Window getWindow() {
		return window;
	}

	public void dispose() {
		window.dispose();
	}

	public void setBackground(Color color) {
		window.setBackground(color);
	}

}
