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

package org.lecturestudio.swing.input;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.lecturestudio.core.util.OsInfo;

public class KeyUtils {

	/**
	 * Returns a string representation of a {@code KeyStroke} that is suitable
	 * for display in a user interface.
	 *
	 * @return A string representation of a {@code KeyStroke}.
	 */
	public static String getDisplayText(KeyStroke keyStroke) {
		StringBuilder builder = new StringBuilder();
		int code = keyStroke.getKeyCode();
		int modifiers = keyStroke.getModifiers();

		if (modifiers > 0) {
			builder.append(KeyEvent.getModifiersExText(modifiers));
			builder.append("+");
		}

		char c = getChar(code);

		if (c != 0) {
			builder.append(c);
		}
		else {
			builder.append(KeyEvent.getKeyText(code));
		}

		return builder.toString();
	}

	private static char getChar(int code) {
		switch (code) {
			case KeyEvent.VK_TAB:				return '\u2B7E';
			case KeyEvent.VK_ENTER:				return '\u21B5';
			case KeyEvent.VK_LEFT:				return '\u2190';
			case KeyEvent.VK_UP:				return '\u2191';
			case KeyEvent.VK_RIGHT:				return '\u2192';
			case KeyEvent.VK_DOWN:				return '\u2193';
			case KeyEvent.VK_COMMA:				return ',';
			case KeyEvent.VK_MINUS:				return '-';
			case KeyEvent.VK_PERIOD:			return '.';
			case KeyEvent.VK_SLASH:				return '/';
			case KeyEvent.VK_SEMICOLON:			return ';';
			case KeyEvent.VK_EQUALS:			return '=';
			case KeyEvent.VK_OPEN_BRACKET:		return '[';
			case KeyEvent.VK_BACK_SLASH:		return '\\';
			case KeyEvent.VK_CLOSE_BRACKET:		return ']';
			case KeyEvent.VK_MULTIPLY:			return '*';
			case KeyEvent.VK_ADD:				return '+';
			case KeyEvent.VK_SUBTRACT:			return '-';
			case KeyEvent.VK_DECIMAL:			return '.';
			case KeyEvent.VK_DIVIDE:			return '/';
			case KeyEvent.VK_BACK_QUOTE:		return '`';
			case KeyEvent.VK_QUOTE:				return '"';
			case KeyEvent.VK_AMPERSAND:			return '&';
			case KeyEvent.VK_ASTERISK:			return '*';
			case KeyEvent.VK_LESS:				return '<';
			case KeyEvent.VK_GREATER:			return '>';
			case KeyEvent.VK_BRACELEFT:			return '{';
			case KeyEvent.VK_BRACERIGHT:		return '}';
			case KeyEvent.VK_AT:				return '@';
			case KeyEvent.VK_COLON:				return ':';
			case KeyEvent.VK_CIRCUMFLEX:		return '^';
			case KeyEvent.VK_DOLLAR:			return '$';
			case KeyEvent.VK_EURO_SIGN:			return '\u20AC';
			case KeyEvent.VK_EXCLAMATION_MARK:	return '!';
			case KeyEvent.VK_LEFT_PARENTHESIS:	return '(';
			case KeyEvent.VK_NUMBER_SIGN:		return '#';
			case KeyEvent.VK_PLUS:				return '+';
			case KeyEvent.VK_RIGHT_PARENTHESIS:	return ')';
			case KeyEvent.VK_UNDERSCORE:		return '_';
			case KeyEvent.VK_0:					return '0';
			case KeyEvent.VK_1:					return '1';
			case KeyEvent.VK_2:					return '2';
			case KeyEvent.VK_3:					return '3';
			case KeyEvent.VK_4:					return '4';
			case KeyEvent.VK_5:					return '5';
			case KeyEvent.VK_6:					return '6';
			case KeyEvent.VK_7:					return '7';
			case KeyEvent.VK_8:					return '8';
			case KeyEvent.VK_9:					return '9';
			default:
				break;
		}

		if (OsInfo.isMac()) {
			switch (code) {
				case KeyEvent.VK_BACK_SPACE:	return '\u232B';
				case KeyEvent.VK_ESCAPE:		return '\u238B';
				case KeyEvent.VK_DELETE:		return '\u2326';
				default:
					break;
			}
		}

		return 0;
	}
}
