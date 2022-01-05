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

package org.lecturestudio.core.camera;

import org.lecturestudio.core.model.Quality;

/**
 * Default camera profile collection.
 *
 * @author Alex Andres
 */
public class CameraProfiles {

	/**
	 * All standard profiles with 4:3 image formats.
	 */
	public static final CameraProfile[] STANDARD = {
			new CameraProfile(CameraFormats.QVGA, Quality.LOW, 100),
			new CameraProfile(CameraFormats.VGA, Quality.MEDIUM, 200),
			new CameraProfile(CameraFormats.SXGA, Quality.HIGH, 400),
			new CameraProfile(CameraFormats.UXGA, Quality.HIGHEST, 600)
	};

	/**
	 * All wide screen profiles with 16:9 and 16:10 image formats.
	 */
	public static final CameraProfile[] WIDESCREEN = {
			new CameraProfile(CameraFormats.CGA, Quality.LOW, 100),
			new CameraProfile(CameraFormats.nHD, Quality.MEDIUM, 200),
			new CameraProfile(CameraFormats.HD720p, Quality.HIGH, 400),
			new CameraProfile(CameraFormats.HD, Quality.HIGHEST, 600)
	};


	public static CameraProfile[] forRatio(AspectRatio ratio) {
		CameraProfile[] profiles = null;

		if (ratio == AspectRatio.Standard) {
			profiles = CameraProfiles.STANDARD;
		}
		else if (ratio == AspectRatio.Widescreen) {
			profiles = CameraProfiles.WIDESCREEN;
		}

		return profiles;
	}
}
