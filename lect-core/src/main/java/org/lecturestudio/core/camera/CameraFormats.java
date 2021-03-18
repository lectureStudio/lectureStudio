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

package org.lecturestudio.core.camera;

/**
 * Default camera formats collection.
 *
 * @author Alex Andres
 */
public class CameraFormats {

	public static final CameraFormat QCGA       = new CameraFormat(160, 100, 30);     // 16:10
	public static final CameraFormat QQVGA      = new CameraFormat(160, 120, 30);     // 4:3
	public static final CameraFormat QCIF       = new CameraFormat(176, 144, 30);     // 4:3
	public static final CameraFormat CGA        = new CameraFormat(320, 200, 30);     // 16:10
	public static final CameraFormat QVGA       = new CameraFormat(320, 240, 30);     // 4:3
	public static final CameraFormat CIF        = new CameraFormat(352, 288, 30);     // 4:3
	public static final CameraFormat WQVGA      = new CameraFormat(384, 240, 30);     // 16:10
	public static final CameraFormat nHD        = new CameraFormat(640, 360, 30);     // 16:9
	public static final CameraFormat VGA        = new CameraFormat(640, 480, 30);     // 4:3
	public static final CameraFormat PAL        = new CameraFormat(768, 576, 30);     // 4:3
	public static final CameraFormat SVGA       = new CameraFormat(800, 600, 30);     // 4:3
	public static final CameraFormat WVGA       = new CameraFormat(854, 480, 30);     // 16:9
	public static final CameraFormat qHD        = new CameraFormat(960, 540, 30);     // 16:9
	public static final CameraFormat WSVGA      = new CameraFormat(1024, 576, 30);    // 16:9
	public static final CameraFormat XGA        = new CameraFormat(1024, 768, 30);    // 4:3
	public static final CameraFormat WXGA       = new CameraFormat(1200, 800, 30);    // 16:10
	public static final CameraFormat HD720p     = new CameraFormat(1280, 720, 30);    // 16:9
	public static final CameraFormat SXGA       = new CameraFormat(1280, 960, 30);    // 4:3
	public static final CameraFormat SXGAplus   = new CameraFormat(1400, 1050, 30);   // 4:3
	public static final CameraFormat HDplus     = new CameraFormat(1600, 900, 30);    // 16:9
	public static final CameraFormat UXGA       = new CameraFormat(1600, 1200, 30);   // 4:3
	public static final CameraFormat HD         = new CameraFormat(1920, 1080, 30);   // 16:9
	public static final CameraFormat QWXGA      = new CameraFormat(2048, 1152, 30);   // 16:9
	public static final CameraFormat QXGA       = new CameraFormat(2048, 1536, 30);   // 4:3


	/** Mixed set of standard 4:3 and wide screen formats. */
	public static final CameraFormat[] DefaultSet = {
			QQVGA, QVGA, nHD, VGA
	};

	/** All 4:3 screen formats. */
	public static final CameraFormat[] Standard = {
			QQVGA, QCIF, QVGA, CIF, VGA, PAL, SVGA, XGA, SXGA, SXGAplus, UXGA, QXGA
	};

	/** All wide screen formats. This includes 16:9 and 16:10 formats. */
	public static final CameraFormat[] Widescreen = {
			QCGA, CGA, WQVGA, nHD, WVGA, qHD, WSVGA, WXGA, HD720p, HDplus, HD, QWXGA
	};

}
