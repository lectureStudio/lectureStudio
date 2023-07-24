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

package org.lecturestudio.presenter.swing.utils;

import java.util.Objects;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.swing.components.MessagePanel;
import org.lecturestudio.swing.components.NotesPanel;
import org.lecturestudio.web.api.message.UserMessage;
import org.lecturestudio.web.api.model.UserInfo;

public class ViewUtil {

	/**
	 * Creates a concrete MessagePanel specified by the provided class
	 * parameter. The returned panel will have the username and date set which
	 * are retrieved from the provided message.
	 *
	 * @param c        The class of the concrete MessagePanel to create.
	 * @param userInfo The local user information.
	 * @param message  The message bound to the new panel.
	 * @param dict     The dictionary to translate content in the new panel.
	 * @param <T>      The type of the panel to create.
	 *
	 * @return A new panel implementing the MessagePanel.
	 */
	public static <T extends MessagePanel> T createMessageView(Class<T> c,
			UserInfo userInfo, UserMessage message, Dictionary dict) {
		String myId = userInfo.getUserId();
		boolean byMe = Objects.equals(message.getUserId(), myId);
		String sender;

		if (byMe) {
			sender = dict.get("text.message.me");
		}
		else {
			String nameFull = message.getFirstName() + " " + message.getFamilyName();
			String[] nameParts = nameFull.split(" ");
			String firstName = nameParts.length > 0 ? nameParts[0] : "";
			String lastName = nameParts.length > 1 ? nameParts[nameParts.length - 1] : "";

			sender = String.format("%s %s", firstName, lastName);
		}

		try {
			T view = c.getDeclaredConstructor(Dictionary.class)
					.newInstance(dict);
			view.setUserName(sender);
			view.setDate(message.getDate());

			return view;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Creates a concrete NotesPanel specified by the provided class
	 * parameter.
	 *
	 * @param c        The class of the concrete NotesPanel to create.
	 * @param dict     The dictionary to translate content in the new panel.
	 * @param <T>      The type of the panel to create.
	 *
	 * @return A new panel implementing the NotesPanel.
	 */
	public static <T extends NotesPanel> T createNotesView(Class<T> c, Dictionary dict) {

		try {
			T view = c.getDeclaredConstructor(Dictionary.class).newInstance(dict);
			return view;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
