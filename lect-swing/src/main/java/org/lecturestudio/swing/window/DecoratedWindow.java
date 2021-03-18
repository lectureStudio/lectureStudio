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

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.Window;

import javax.swing.JFrame;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.view.Screens;
import org.lecturestudio.swing.ui.CMenuBar;

public abstract class DecoratedWindow extends AbstractWindow {

	private Rectangle lastWindowBounds = null;
	
	
	public DecoratedWindow(ApplicationContext context) {
		super(context);
	}

	public void maximize() {
		JFrame frame = getWindow();
		frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	}
	
	public boolean isFullscreen() {
		return isUndecorated();
	}

	public void setFullscreen(boolean fullscreen) {
		boolean isFullscreen = isFullscreen();

		if (isFullscreen == fullscreen) {
			return;
		}

		JFrame window = getWindow();
		Rectangle windowBounds;
		boolean resizeable;

		if (fullscreen) {
			setUndecorated(true);
			
			GraphicsDevice device = Screens.getScreenDevice(window); 
			windowBounds = device.getDefaultConfiguration().getBounds();
			
			// TODO: workaround for Linux on multi-display environment is required.
			
			resizeable = false;
			
			lastWindowBounds = window.getBounds();
		}
		else {
			setUndecorated(false);
			windowBounds = lastWindowBounds;
			resizeable = true;
		}

		window.setBounds(windowBounds);
		window.setResizable(resizeable);
		window.validate();
		window.setVisible(true);
	}

	public void add(Component comp, Object constraints) {
		getWindow().getContentPane().add(comp, constraints);
	}

	public void remove(Component comp) {
		getWindow().remove(comp);
	}

	public void setTitle(String title) {
		getWindow().setTitle(title);
	}

	public void setMenuBar(CMenuBar menuBar) {
		getWindow().setJMenuBar(menuBar);
	}

	public CMenuBar getMenuBar() {
		return (CMenuBar) getWindow().getJMenuBar();
	}

	public void setUndecorated(boolean decorated) {
		dispose();
		getWindow().setUndecorated(decorated);
	}

	public boolean isUndecorated() {
		return getWindow().isUndecorated();
	}
	
	@Override
	protected Window createWindow(GraphicsConfiguration gc) {
		return new JFrame(gc);
	}

	@Override
	public JFrame getWindow() {
		return (JFrame) super.getWindow();
	}

}
