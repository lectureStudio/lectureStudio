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

package org.lecturestudio.core.util;

public class OsInfo {

	private static final String platformName;

	private static String osName;

	private static int bitness;


	static {
		osName = System.getProperty("os.name").toLowerCase();
		String osArch = System.getProperty("os.arch").toLowerCase();
		String jvmName = System.getProperty("java.vm.name").toLowerCase();

		if (jvmName.startsWith("dalvik") && osName.startsWith("linux")) {
			osName = "android";
		}
		else if (osName.startsWith("mac os x")) {
			osName = "macos";
		}
		else {
			osName = osName.split(" ")[0];
		}

		if (osArch.endsWith("86")) {
			osArch = "x86";
			bitness = 32;
		}
		else if (osArch.equals("amd64") || osArch.equals("x86-64") || osArch.equals("x86_64")) {
			osArch = "x86_64";
			bitness = 64;
		}
		else if (osArch.startsWith("arm")) {
			osArch = "arm";
		}

		platformName = osName + "-" + osArch;
	}
	

	private OsInfo() {
	}

	public static boolean isLinux() {
		return osName.startsWith("linux");
	}

	public static boolean isMacOs() {
		return osName.startsWith("mac");
	}

	public static boolean isWindows() {
		return osName.startsWith("windows");
	}

	public static String getPlatformName() {
		return platformName;
	}

	public static int getBitness() {
		return bitness;
	}
	
}
