/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.web.api.stream.action;

import java.io.IOException;

public class StreamActionFactory {

	public static StreamAction createAction(int actionType, byte[] input)
			throws IOException {
		return getAction(actionType, input);
	}

	private static StreamAction getAction(int actionType, byte[] input)
			throws IOException {
		StreamActionType type = StreamActionType.values()[actionType];

		switch (type) {
			case STREAM_START:
				return new StreamStartAction(input);
			case STREAM_PAGE_ACTION:
				return new StreamPagePlaybackAction(input);
			case STREAM_PAGE_ACTIONS:
				return new StreamPageActionsAction(input);
			case STREAM_PAGE_CREATED:
				return new StreamPageCreatedAction(input);
			case STREAM_PAGE_DELETED:
				return new StreamPageDeletedAction(input);
			case STREAM_PAGE_SELECTED:
				return new StreamPageSelectedAction(input);
			case STREAM_DOCUMENT_CREATED:
				return new StreamDocumentCreateAction(input);
			case STREAM_DOCUMENT_CLOSED:
				return new StreamDocumentCloseAction(input);
			case STREAM_DOCUMENT_SELECTED:
				return new StreamDocumentSelectAction(input);
			default:
				throw new IOException("Action not defined: " + type);
		}
	}
}
