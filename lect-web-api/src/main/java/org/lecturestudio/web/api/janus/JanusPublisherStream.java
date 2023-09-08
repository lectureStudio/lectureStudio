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

package org.lecturestudio.web.api.janus;

/**
 * A Janus WebRTC video room stream.
 *
 * @author Alex Andres
 *
 * @apiNote https://janus.conf.meetecho.com/docs/videoroom.html
 */
public class JanusPublisherStream {

	private String type;

	private Integer mindex;

	private String mid;

	private String codec;


	/**
	 * @return The type of published stream (audio|video|data).
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return The unique mindex of published stream.
	 */
	public Integer getMindex() {
		return mindex;
	}

	/**
	 * @return The unique mid of published stream.
	 */
	public String getMid() {
		return mid;
	}

	/**
	 * @return The codec used for published stream.
	 */
	public String getCodec() {
		return codec;
	}
}
