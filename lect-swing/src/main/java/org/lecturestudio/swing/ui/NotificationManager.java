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

import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.lecturestudio.core.app.view.Screens;

/**
 * Application-wide Notification Manager which shows important messages to the user. This manager
 * will restrain new notifications, if the threshold of maximum visible notifications is reached.
 * Restrained notifications appear after all previous notifications disappeared.
 * 
 * @author Alex Andres
 */
public class NotificationManager {

	/** Notification view location on the screen. */
	public enum Location { TopLeft, TopRight, BottomLeft, BottomRight };
	
	/** Singleton instance. */
	private static NotificationManager instance;
	
	/** Maximum visible notifications at once. */
	private static final int MAX_VISIBLE_NOTIFICATIONS = 5;
	
	/** Notifications hold back, if MAX_VISIBLE_NOTIFICATIONS reached. */
	private Stack<NotificationPopup> notificationStack = new Stack<>();
	
	/** Visible notifications. */
	private List<NotificationPopup> list = new ArrayList<>(MAX_VISIBLE_NOTIFICATIONS);
	
	/** Visible notifications until MAX_VISIBLE_NOTIFICATIONS reached. */
	private int notifications = 0;
	
	/** Lock manager and hold back new notifications. */
	private boolean locked = false;
	
	/** Notification location on the screen. */
	private Location location = Location.BottomRight;
	
	
	/**
	 * Prevent public initialization.
	 */
	private NotificationManager() { }
	
	/**
	 * Get the singleton instance of this {@code NotificationManager}.
	 * 
	 * @return NotificationManager singleton.
	 */
	public static synchronized NotificationManager getInstance() {
		if (NotificationManager.instance == null) {
			NotificationManager.instance = new NotificationManager();
		}
		return NotificationManager.instance;
	}
	
	/**
	 * Show new simple notification. If the threshold of maximum visible notifications is reached,
	 * the new notification is restrained until old notifications disappear.
	 * 
	 * @param message notification message.
	 */
	public void showNotification(String message) {
		NotificationPopup notification = new NotificationPopup(this, message);
		openNotification(notification);
	}
	
	/**
	 * Show new simple notification. If the threshold of maximum visible notifications is reached,
	 * the new notification is restrained until old notifications disappear.
	 * 
	 * @param message notification message.
	 * @param pending true, if this is a progress notification.
	 */
	public NotificationPopup showNotification(String message, boolean pending) {
		NotificationPopup notification = new NotificationPopup(this, message, pending);
		openNotification(notification);
		
		return notification;
	}
	
	/**
	 * Closes the given notification.
	 * 
	 * @param notification The notification to close.
	 */
	public void closeNotification(NotificationPopup notification) {
		notification.close();
	}
	
	/**
	 * Set new location {@code (TopLeft, TopRight, BottomLeft, BottomRight)} for notifications on
	 * the screen. Default value is {@code BottomRight}.
	 * 
	 * @param location new location.
	 */
	public void setNotificationLocation(Location location) {
		this.location = location;
	}
	
	/**
	 * Get current location for notifications.
	 * 
	 * @return notification location.
	 */
	public Location getNotificationLocation() {
		return location;
	}
	
	/**
	 * Callback from NotificationPopup which is about to close.
	 * 
	 * @param notification The closing notification.
	 */
	void notificationClosed(NotificationPopup notification) {
		list.remove(notification);
		
		if (list.isEmpty()) {
			// Open stacked notifications
			locked = false;
			
			notifications = 0;
			
			for (int i = 0; i < MAX_VISIBLE_NOTIFICATIONS; i++) {
				if (notificationStack.size() > 0) {
					NotificationPopup n = notificationStack.firstElement();
					openNotification(n);
					
					notificationStack.removeElementAt(0);
				}
			}
		}
	}
	
	/**
	 * Show new notification on the screen.
	 * 
	 * @param notification the notification to open.
	 */
	private void openNotification(NotificationPopup notification) {
		if (notifications >= MAX_VISIBLE_NOTIFICATIONS || locked) {
			notificationStack.push(notification);
			return;
		}
		
		list.add(notification);
		
		notifications++;
		
		if (notifications >= MAX_VISIBLE_NOTIFICATIONS) {
			locked = true;
		}
		
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Rectangle screenBounds = Screens.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
		
		// Height of the task bar
		Insets scnMax = toolkit.getScreenInsets(notification.getGraphicsConfiguration());
		int taskBarSize = scnMax.bottom;
		int x = screenBounds.x;
		int y = screenBounds.y;
		
		// Place on the screen
		if (location == Location.TopLeft) {
			y += (notification.getHeight() - 1) * notifications;
		}
		else if (location == Location.TopRight) {
			x += screenBounds.width - notification.getWidth();
			y += (notification.getHeight() - 1) * notifications;
		}
		else if (location == Location.BottomLeft) {
			y += screenBounds.height + 1 - taskBarSize - ((notification.getHeight() - 1) * notifications);
		}
		else if (location == Location.BottomRight) {
			x += screenBounds.width - notification.getWidth();
			y += screenBounds.height + 1 - taskBarSize - ((notification.getHeight() - 1) * notifications);
		}
		
		notification.setLocation(x, y);
		notification.open();
	}
	
}
