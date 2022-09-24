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

public enum StreamActionType {

	STREAM_INIT,
	STREAM_START,

	STREAM_PAGE_ACTION,
	STREAM_PAGE_ACTIONS,
	STREAM_PAGE_CREATED,
	STREAM_PAGE_DELETED,
	STREAM_PAGE_SELECTED,

	STREAM_DOCUMENT_CREATED,
	STREAM_DOCUMENT_CLOSED,
	STREAM_DOCUMENT_SELECTED,

	STREAM_SPEECH_PUBLISHED,

	STREAM_CAMERA_CHANGE,
	STREAM_MICROPHONE_CHANGE,
	STREAM_SCREEN_SHARE_CHANGE;

}
