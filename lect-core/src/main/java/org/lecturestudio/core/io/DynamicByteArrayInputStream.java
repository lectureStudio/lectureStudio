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

package org.lecturestudio.core.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.model.Interval;

public class DynamicByteArrayInputStream extends DynamicInputStream {

	/** Logger for {@link DynamicByteArrayInputStream} */
	private static final Logger LOG = LogManager.getLogger(DynamicByteArrayInputStream.class);

	/** The data of the {@link DynamicByteArrayInputStream} */
	private final byte[] data;

	/**
	 * Creates a new instance of {@link DynamicByteArrayInputStream} with the specified data.
	 *
	 * @param data The data.
	 */
	public DynamicByteArrayInputStream(byte[] data) throws UnsupportedAudioFileException, IOException {
		super(AudioSystem.getAudioInputStream(new ByteArrayInputStream(data)));
		
		this.data = data;
	}

	@Override
	public DynamicByteArrayInputStream clone() {
		DynamicByteArrayInputStream clone = null;
		
		try {
			clone = new DynamicByteArrayInputStream(data);
			
			for (Interval<Long> iv : exclusions) {
				clone.addExclusion(new Interval<>(iv.getStart(), iv.getEnd()));
			}
		}
		catch (Exception e) {
			LOG.error(e);
		}
		
		return clone;
	}
	
}
