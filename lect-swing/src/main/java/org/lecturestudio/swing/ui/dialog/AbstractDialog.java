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

package org.lecturestudio.swing.ui.dialog;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.configuration.WindowConfiguration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.app.view.Screens;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.app.ApplicationContext;

public abstract class AbstractDialog<T> extends JDialog {

	private static final long serialVersionUID = -6286851107180750137L;

	private static final Logger LOG = LogManager.getLogger(AbstractDialog.class);
	
	protected Dictionary dict;

	protected Configuration config;
	
	protected ApplicationContext context;


	abstract public T open();

	abstract protected void initComponents();
	

	public AbstractDialog(Frame parent, ApplicationContext context) {
		super(parent);

		this.context = context;
		this.dict = context.getDictionary();
		this.config = context.getConfiguration();

		initListeners();
	}

	public void centerToScreen() {
		GraphicsDevice dev = Screens.getScreenDevice(getParent());
		Rectangle screenBounds = dev.getDefaultConfiguration().getBounds();
		Dimension size = getPreferredSize();
		
		setLocation(
				screenBounds.x + (screenBounds.width - size.width) / 2,
				screenBounds.y + (screenBounds.height - size.height) / 2);
	}

	public void close() {
		saveBounds();
		dispose();
	}
	
	protected ApplicationContext getContext() {
		return context;
	}
	
	@Override
	protected JRootPane createRootPane() {
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				closeDialog();
			}
		};

		JRootPane rootPane = new JRootPane();
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		return rootPane;
	}

	protected void closeDialog() {
		close();
	}

	protected void initListeners() {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				closeDialog();
			}
		});
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
	
	protected void layoutOnScreen(Dimension size) {
		Set<WindowConfiguration> windowConfigs = config.getWindowConfigurations();
		Class<?> cls = getClass();
		WindowConfiguration windowConfig = null;
		
		for (WindowConfiguration conf : windowConfigs) {
			if (conf.getWindowClass().equals(cls)) {
				windowConfig = conf;
				break;
			}
		}
		
		if (windowConfig == null) {
			if (size == null) {
				pack();
			}
			else {
				setSize(size);
				setPreferredSize(size);
			}
			
			centerToScreen();
		}
		else {
			Rectangle2D bounds = windowConfig.getWindowBounds();
			int x = (int) bounds.getX();
			int y = (int) bounds.getY();
			int w = (int) bounds.getWidth();
			int h = (int) bounds.getHeight();
			
			setBounds(x, y, w, h);
		}
	}
	
	protected void saveBounds() {
		Rectangle rect = getBounds();
		
		WindowConfiguration wc = new WindowConfiguration();
		wc.setWindowClass(getClass());
		wc.setDeviceId(getGraphicsConfiguration().getDevice().getIDstring());
		wc.setWindowBounds(new Rectangle2D(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
		
		config.addWindowConfiguration(wc);
		
		try {
			context.saveConfiguration();
		}
		catch (Exception e) {
			LOG.error("Save configuration failed.", e);
		}
	}

}
