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

package org.lecturestudio.swing.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.border.EmptyBorder;

import org.lecturestudio.core.util.OsInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The NotificationPopup is used to present important information on the screen. This class should
 * not be used standalone. Notifications are managed by the {@link NotificationManager}. The
 * appearance of notifications may be changed by the configuration file
 * {@code notification.properties}.
 * 
 * @author Alex Andres
 */
public class NotificationPopup extends JWindow {
	
	private static final long serialVersionUID = 8646072591075886071L;
	
	private static final Logger LOG = LogManager.getLogger(NotificationPopup.class);

	private final static String CONFIG_FILE = "resources/notification.properties";

	private final Object lock = new Object();

	private final NotificationManager manager;

	private final LinearGradientPaint gradient;
	
	private final JTextArea messageLabel;
	
	private JProgressBar progressBar;
	
	private enum State { SHOWING, IDLE, HIDING };
	
	private State state = State.SHOWING;
	
	private String message;
	
	private int timeIdle;
	
	private float opacity;
	
	private boolean pending;
	

	/**
	 * Create a new {@code NotificationPopup}.
	 * 
	 * @param manager Notification manager.
	 * @param message Notification message.
	 */
	NotificationPopup(NotificationManager manager, String message) {
		this(manager, message, false);
	}
	
	/**
	 * Create a new {@code NotificationPopup}.
	 * 
	 * @param manager Notification manager.
	 * @param message Notification message.
	 * @param pending true, if this is a progress notification.
	 */
	NotificationPopup(NotificationManager manager, String message, boolean pending) {
		this.manager = manager;
		this.message = message;
		this.pending = pending;
		
		messageLabel = new JTextArea();
		
		Properties config = loadProperties();
		
		int width = Integer.parseInt(config.getProperty("window.width", "300"));
		int height = Integer.parseInt(config.getProperty("window.height", "70"));
		
		String[] chunks = message.split("\n");
		FontMetrics fm = messageLabel.getFontMetrics(messageLabel.getFont());
		
		height += (chunks.length - 1) * fm.getHeight();
		
		int pt = Integer.parseInt(config.getProperty("padding.top", "15"));
		int pr = Integer.parseInt(config.getProperty("padding.right", "15"));
		int pb = Integer.parseInt(config.getProperty("padding.bottom", "15"));
		int pl = Integer.parseInt(config.getProperty("padding.left", "15"));
		
		timeIdle = Integer.parseInt(config.getProperty("time.idle", "2000"));
		
		Color c0 = Color.decode(config.getProperty("color.background.start", "0x54585C"));
		Color c1 = Color.decode(config.getProperty("color.background.end", "0x3F4245"));
		Color ct = Color.decode(config.getProperty("color.foreground", "0xE6EBF1"));
		
		// Background paint
		gradient = new LinearGradientPaint(0, 0, 0, height, new float[] { 0f, 0.5f }, new Color[] { c0, c1 });

		BackgroundPanel panel = new BackgroundPanel();
		panel.addMouseListener(new DisposeListener());
		panel.setLayout(new GridBagLayout());

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0f;
		constraints.weighty = 1.0f;
		constraints.insets = new Insets(pt, pr, pb, pl);
		constraints.fill = GridBagConstraints.BOTH;

		messageLabel.setOpaque(false);
		messageLabel.setForeground(ct);
		messageLabel.setText(message);

		panel.add(messageLabel, constraints);
		
		if (pending) {
			constraints.gridy++;
			
			progressBar = new JProgressBar();
			progressBar.setIndeterminate(true);
			progressBar.setOpaque(false);
			progressBar.setBorder(new EmptyBorder(0, 0, 0, 0));
			
			panel.add(progressBar, constraints);
			
			height += 30;
		}

		System.out.println(width + " " + height);
		
		setAlwaysOnTop(true);
		setSize(width, height);
		setContentPane(panel);
		setVisible(false);
	}
	
	void open() {
		setVisible(true);
		
		// Start show animation.
		new Thread(AnimationTask).start();
	}
	
	void close() {
		manager.notificationClosed(this);
		
		dispose();
	}

	private Properties loadProperties() {
		Properties config = new Properties();
		try {
			config.load(getClass().getClassLoader().getResourceAsStream(CONFIG_FILE));
		}
		catch (Exception e) {
			LOG.error("Load notification popup configuration failed.", e);
		}
		return config;
	}
	
	@Override
	public void setOpacity(float opacity) {
		if (OsInfo.isLinux()) {
			this.opacity = opacity;
			setX11Opacity(opacity);
		}
		else if (OsInfo.isWindows()) {
			super.setOpacity(opacity);
		}
		else {
			// TODO: test on Mac OS
		}
	}
	
	@Override
	public float getOpacity() {
		if (OsInfo.isWindows()) {
			return super.getOpacity();
		}
		
		return opacity;
	}
	
	@Override
	public String toString() {
		return "Notification: " + message + " " + hashCode();
	}
	
	private void setX11Opacity(float opacity) {
		long value = (int) (0xff * opacity) << 24;
		try {
			// long windowId = peer.getWindow();
			Field peerField = Component.class.getDeclaredField("peer");
			peerField.setAccessible(true);
			Class<?> xWindowPeerClass = Class.forName("sun.awt.X11.XWindowPeer");
			Method getWindowMethod = xWindowPeerClass.getMethod("getWindow", new Class[0]);
			long windowId = ((Long) getWindowMethod.invoke(peerField.get(this), new Object[0])).longValue();

			// sun.awt.X11.XAtom.get("_NET_WM_WINDOW_OPACITY").setCard32Property(windowId, value);
			Class<?> xAtomClass = Class.forName("sun.awt.X11.XAtom");
			Method getMethod = xAtomClass.getMethod("get", String.class);
			Method setCard32PropertyMethod = xAtomClass.getMethod("setCard32Property", long.class, long.class);
			setCard32PropertyMethod.invoke(getMethod.invoke(null, "_NET_WM_WINDOW_OPACITY"), windowId, value);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex.getCause());
		}
	}
	
	
	private Runnable AnimationTask = new Runnable() {
		
		@Override
		public void run() {
			setOpacity(0);
			
			boolean run = true;
			while (run) {
				if (state == State.SHOWING) {
					idle(10);
					
					if (!show()) {
						state = State.IDLE;
					}
				}
				else if (state == State.IDLE) {
					idle(timeIdle);
					
					state = State.HIDING;
				}
				else if (state == State.HIDING) {
					idle(10);
					
					if (!hide()) {
						run = false;
					}
				}
			}
			
			close();
		}
		
		private void idle(int millis) {
			try {
				if (pending && state == State.IDLE) {
					synchronized (lock) {
						lock.wait();
					}
				}
				else {
					Thread.sleep(millis);
				}
			}
			catch (InterruptedException e) { }
		}
		
		private boolean show() {
			float opacity = getOpacity() + 0.04f;
			if (opacity >= 0.9f)
				opacity = 0.9f;
			
			try {
				setOpacity(opacity);
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			
			return opacity != 0.9f;
		}
		
		private boolean hide() {
			float opacity = getOpacity() - 0.04f;
			if (opacity <= 0)
				opacity = 0;
			
			try {
				setOpacity(opacity);
			}
			catch (Exception e) {
				LOG.error("Set opacity on notification popup failed.", e);
				return false;
			}
			
			return opacity != 0;
		}
		
	};
	
	private class DisposeListener extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent event) {
			close();
		}
		
	}
	
	private class BackgroundPanel extends JPanel {
		
		private static final long serialVersionUID = 6792448601622621471L;

		public BackgroundPanel() {
			setOpaque(true);
		}

		@Override
		protected void paintComponent(final Graphics g) {
			final Graphics2D g2d = (Graphics2D) g;
			
			// Background
			g2d.setPaint(gradient);
			g2d.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
			g2d.setColor(Color.BLACK);

			// Border
			g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		}
	}
	
}