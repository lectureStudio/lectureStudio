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

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JWindow;

import org.lecturestudio.core.camera.bus.event.CameraImageEvent;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.bus.event.ShutdownEvent;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.PresentationView;
import org.lecturestudio.core.view.Screen;
import org.lecturestudio.core.view.ScreenViewType;
import org.lecturestudio.core.view.SlideViewOverlay;
import org.lecturestudio.swing.components.CameraImagePanel;
import org.lecturestudio.swing.converter.ColorConverter;
import org.lecturestudio.swing.converter.RectangleConverter;

public class CameraWindow extends AbstractWindow implements PresentationView {

	private CameraImagePanel cameraPanel;


	public CameraWindow(ApplicationContext context, Screen screen) {
		super(context);
		init(screen);
	}

	public ScreenViewType getType() {
		return ScreenViewType.CAMERA;
	}

	@Override
	public void setBackgroundColor(Color color) {
		setBackground(ColorConverter.INSTANCE.to(color));
	}

	@Subscribe
	public void onEvent(final CameraImageEvent event) {
		invoke(() -> cameraPanel.paintImage(event.getImage()));
	}

	@Subscribe
	public void onEvent(final ShutdownEvent event) {
		invoke(new Runnable() {

			@Override
			public void run() {
				close();
			}
		});
	}

	protected void init(Screen screen) {
		Rectangle screenBounds = RectangleConverter.INSTANCE.to(screen.getBounds());

		setBounds(screenBounds);
		setAlwaysOnTop(true);

		cameraPanel = new CameraImagePanel(getContext());
		cameraPanel.setPreferredSize(new Dimension(screenBounds.width, screenBounds.height));
		cameraPanel.addComponentListener(new ComponentAdapter() {

			public void componentResized(ComponentEvent e) {
				centerView();
			}
		});

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.CENTER;

		getWindow().getContentPane().setLayout(new GridBagLayout());
		getWindow().getContentPane().add(cameraPanel, constraints);

		ApplicationBus.register(this);
	}

	private void centerView() {
		Dimension size = getSize();
		Dimension viewSize = cameraPanel.getSize();

		int x = (size.width - viewSize.width) / 2;
		int y = (size.height - viewSize.height) / 2;

		cameraPanel.setBounds(x, y, cameraPanel.getWidth(), cameraPanel.getHeight());
	}

	@Override
	public void close() {
		ApplicationBus.unregister(this);

		cameraPanel.dispose();
		
		setVisible(false);
		dispose();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);

		if (visible && getDocument() != null) {
			centerView();
		}
	}

	@Override
	public void setOnVisible(Action action) {

	}

	@Override
	public void addOverlay(SlideViewOverlay overlay) {

	}

	@Override
	public void removeOverlay(SlideViewOverlay overlay) {

	}

	@Override
	protected Window createWindow(GraphicsConfiguration gc) {
		return new JWindow(gc);
	}

	@Override
	public void setBackground(java.awt.Color color) {
		getWindow().getContentPane().setBackground(color);
	}

	@Override
	public JWindow getWindow() {
		return (JWindow) super.getWindow();
	}

}
