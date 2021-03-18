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

package org.lecturestudio.swing.swixml.script;

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class JavaScriptEngineFactory implements ScriptEngineFactory {

	private final ScriptEngine engine = new JavaScriptEngine();


	@Override
	public String getEngineName() {
		return "JavaScript";
	}

	@Override
	public String getEngineVersion() {
		return "1.0";
	}

	@Override
	public List<String> getExtensions() {
		return List.of("java");
	}

	@Override
	public List<String> getMimeTypes() {
		return List.of("text/x-java-source");
	}

	@Override
	public List<String> getNames() {
		return List.of("JavaScript");
	}

	@Override
	public String getLanguageName() {
		return "Java";
	}

	@Override
	public String getLanguageVersion() {
		return System.getProperty("java.version");
	}

	@Override
	public Object getParameter(String key) {
		return null;
	}

	@Override
	public String getMethodCallSyntax(String obj, String m, String... args) {
		return null;
	}

	@Override
	public String getOutputStatement(String toDisplay) {
		return null;
	}

	@Override
	public String getProgram(String... statements) {
		return null;
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return engine;
	}
}
