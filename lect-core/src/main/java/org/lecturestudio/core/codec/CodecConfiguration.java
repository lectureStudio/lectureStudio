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

package org.lecturestudio.core.codec;

import org.lecturestudio.core.beans.IntegerProperty;

/**
 * The base codec configuration contains codec properties for basic usage.
 *
 * @author Alex Andres
 */
public abstract class CodecConfiguration {

	/** The encoder's target bitrate. */
	private final IntegerProperty bitrate = new IntegerProperty();


	/**
	 * Get the encoder's target bitrate.
	 *
	 * @return the encoder's target bitrate.
	 */
	public int getBitRate() {
		return bitrate.get();
	}

	/**
	 * Set the encoder's target bitrate. The value is an integer containing the
	 * bitrate in bps.
	 *
	 * @param bitrate The encoder's target bitrate in bps.
	 */
	public void setBitRate(int bitrate) {
		this.bitrate.set(bitrate);
	}

	/**
	 * Get the bitrate property.
	 *
	 * @return the bitrate property.
	 */
	public IntegerProperty bitRateProperty() {
		return bitrate;
	}
}
