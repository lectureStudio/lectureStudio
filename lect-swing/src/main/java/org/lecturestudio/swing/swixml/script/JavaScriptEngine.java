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

import java.io.Reader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class JavaScriptEngine implements ScriptEngine {

	@Override
	public Object eval(String script, ScriptContext context) {
		return null;
	}

	@Override
	public Object eval(Reader reader, ScriptContext context) {
		return null;
	}

	@Override
	public Object eval(String script) {
		return null;
	}

	@Override
	public Object eval(Reader reader) {
		return null;
	}

	@Override
	public Object eval(String script, Bindings n) {
		return null;
	}

	@Override
	public Object eval(Reader reader, Bindings n) {
		return null;
	}

	@Override
	public void put(String key, Object value) {

	}

	@Override
	public Object get(String key) {
		return null;
	}

	@Override
	public Bindings getBindings(int scope) {
		return null;
	}

	@Override
	public void setBindings(Bindings bindings, int scope) {

	}

	@Override
	public Bindings createBindings() {
		return null;
	}

	@Override
	public ScriptContext getContext() {
		return null;
	}

	@Override
	public void setContext(ScriptContext context) {

	}

	@Override
	public ScriptEngineFactory getFactory() {
		return new JavaScriptEngineFactory();
	}
}
