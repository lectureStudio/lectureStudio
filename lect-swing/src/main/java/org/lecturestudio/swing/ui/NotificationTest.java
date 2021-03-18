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

public class NotificationTest {

	public static void main(String[] args) {
		try {
			NotificationManager m = NotificationManager.getInstance();
			m.showNotification("You have got 1 new Message.");
			Thread.sleep(700);
			m.showNotification("You have got 2 new Messages.");
			Thread.sleep(700);
			m.showNotification("You have got 3 new Messages.");
			
			Thread.sleep(1400);
			m.showNotification("You have got 4 new Messages.");
			Thread.sleep(600);
			m.showNotification("You have got 5 new Messages.");
			Thread.sleep(800);
			m.showNotification("You have got 6 new Messages.");
		}
		catch (Exception e) {

		}
	}

}
