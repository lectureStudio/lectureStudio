/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

import java.util.Objects;

import org.lecturestudio.web.api.model.ScreenSource;

public class JanusScreenShareConfig {

	private ScreenSource screenSource;

	private Integer frameRate;

	private Integer bitRate;


	public ScreenSource getScreenSource() {
		return screenSource;
	}

	public void setScreenSource(ScreenSource source) {
		if (Objects.equals(screenSource, source)) {
			return;
		}

		this.screenSource = source;
	}

	public Integer getFrameRate() {
		return frameRate;
	}

	public void setFrameRate(Integer frameRate) {
		this.frameRate = frameRate;
	}

	public Integer getBitRate() {
		return bitRate;
	}

	public void setBitRate(Integer bitRate) {
		this.bitRate = bitRate;
	}
}
