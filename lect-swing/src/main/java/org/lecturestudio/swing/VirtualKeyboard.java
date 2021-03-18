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

package org.lecturestudio.swing;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.util.OsInfo;

public class VirtualKeyboard {
	
	private final static Logger LOG = LogManager.getLogger(VirtualKeyboard.class);

	
	public static void show() {
		if (!OsInfo.isWindows())
			return;
		
		String[] processCommand = { "cmd", "/c", "C:\\Program Files\\Common Files\\Microsoft Shared\\ink\\TabTip.exe" };
		
		try {
			ProcessBuilder builder = new ProcessBuilder(processCommand);
			builder.redirectErrorStream(true);
			Process proc = builder.start();
			
			StringBuffer errorBuffer = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				errorBuffer.append(line + "\r\n");
			}
			
			if (errorBuffer.length() > 1) {
				LOG.error("Show virtual keyboard failed. " + errorBuffer);
			}
		}
		catch (Exception e) {
			LOG.error("Show virtual keyboard failed.", e);
		}
	}
	
	public static void hide() {
		if (!OsInfo.isWindows())
			return;
		
		String[] processCommand = { "cmd", "/c", "taskkill", "/IM", "C:\\Program Files\\Common Files\\Microsoft Shared\\ink\\TabTip.exe" };
		
		try {
			ProcessBuilder builder = new ProcessBuilder(processCommand);
			builder.redirectErrorStream(true);
			Process proc = builder.start();
			
			StringBuffer errorBuffer = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				errorBuffer.append(line + "\r\n");
			}
			
			if (errorBuffer.length() > 1) {
				LOG.error("Hide virtual keyboard failed. " + errorBuffer);
			}
		}
		catch (Exception e) {
			LOG.error("Hide virtual keyboard failed.", e);
		}
	}
	
}
