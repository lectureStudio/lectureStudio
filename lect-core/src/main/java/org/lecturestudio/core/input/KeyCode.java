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

package org.lecturestudio.core.input;

/**
 * Enum with key code names the underlying platform codes used to represent the characters.
 */
public enum KeyCode {

	ENTER(0x0A, "Enter"),
	BACK_SPACE(0x08, "Backspace"),
	TAB(0x09, "Tab"),
	CANCEL(0x03, "Cancel"),
	CLEAR(0x0C, "Clear"),
	SHIFT(0x10, "Shift"),
	CONTROL(0x11, "Ctrl"),
	ALT(0x12, "Alt"),
	PAUSE(0x13, "Pause"),
	CAPS(0x14, "Caps Lock"),
	ESCAPE(0x1B, "Esc"),
	SPACE(0x20, "Space"),
	PAGE_UP(0x21, "Page Up"),
	PAGE_DOWN(0x22, "Page Down"),
	END(0x23, "End"),
	HOME(0x24, "Home"),
	LEFT(0x25, "Left"),
	UP(0x26, "Up"),
	RIGHT(0x27, "Right"),
	DOWN(0x28, "Down"),
	COMMA(0x2C, "Comma"),
	MINUS(0x2D, "Minus"),
	PERIOD(0x2E, "Period"),
	SLASH(0x2F, "Slash"),
	DIGIT0(0x30, "0"),
	DIGIT1(0x31, "1"),
	DIGIT2(0x32, "2"),
	DIGIT3(0x33, "3"),
	DIGIT4(0x34, "4"),
	DIGIT5(0x35, "5"),
	DIGIT6(0x36, "6"),
	DIGIT7(0x37, "7"),
	DIGIT8(0x38, "8"),
	DIGIT9(0x39, "9"),
	SEMICOLON(0x3B, "Semicolon"),
	EQUALS(0x3D, "Equals"),
	A(0x41, "A"),
	B(0x42, "B"),
	C(0x43, "C"),
	D(0x44, "D"),
	E(0x45, "E"),
	F(0x46, "F"),
	G(0x47, "G"),
	H(0x48, "H"),
	I(0x49, "I"),
	J(0x4A, "J"),
	K(0x4B, "K"),
	L(0x4C, "L"),
	M(0x4D, "M"),
	N(0x4E, "N"),
	O(0x4F, "O"),
	P(0x50, "P"),
	Q(0x51, "Q"),
	R(0x52, "R"),
	S(0x53, "S"),
	T(0x54, "T"),
	U(0x55, "U"),
	V(0x56, "V"),
	W(0x57, "W"),
	X(0x58, "X"),
	Y(0x59, "Y"),
	Z(0x5A, "Z"),
	OPEN_BRACKET(0x5B, "Open Bracket"),
	BACK_SLASH(0x5C, "Back Slash"),
	CLOSE_BRACKET(0x5D, "Close Bracket"),
	NUMPAD0(0x60, "Numpad 0"),
	NUMPAD1(0x61, "Numpad 1"),
	NUMPAD2(0x62, "Numpad 2"),
	NUMPAD3(0x63, "Numpad 3"),
	NUMPAD4(0x64, "Numpad 4"),
	NUMPAD5(0x65, "Numpad 5"),
	NUMPAD6(0x66, "Numpad 6"),
	NUMPAD7(0x67, "Numpad 7"),
	NUMPAD8(0x68, "Numpad 8"),
	NUMPAD9(0x69, "Numpad 9"),
	MULTIPLY(0x6A, "Multiply"),
	ADD(0x6B, "Add"),
	SEPARATOR(0x6C, "Separator"),
	SUBTRACT(0x6D, "Subtract"),
	DECIMAL(0x6E, "Decimal"),
	DIVIDE(0x6F, "Divide"),
	DELETE(0x7F, "Delete"),
	NUM_LOCK(0x90, "Num Lock"),
	SCROLL_LOCK(0x91, "Scroll Lock"),
	F1(0x70, "F1"),
	F2(0x71, "F2"),
	F3(0x72, "F3"),
	F4(0x73, "F4"),
	F5(0x74, "F5"),
	F6(0x75, "F6"),
	F7(0x76, "F7"),
	F8(0x77, "F8"),
	F9(0x78, "F9"),
	F10(0x79, "F10"),
	F11(0x7A, "F11"),
	F12(0x7B, "F12"),
	F13(0xF000, "F13"),
	F14(0xF001, "F14"),
	F15(0xF002, "F15"),
	F16(0xF003, "F16"),
	F17(0xF004, "F17"),
	F18(0xF005, "F18"),
	F19(0xF006, "F19"),
	F20(0xF007, "F20"),
	F21(0xF008, "F21"),
	F22(0xF009, "F22"),
	F23(0xF00A, "F23"),
	F24(0xF00B, "F24"),
	PRINTSCREEN(0x9A, "Print Screen"),
	INSERT(0x9B, "Insert"),
	HELP(0x9C, "Help"),
	META(0x9D, "Meta"),
	BACK_QUOTE(0xC0, "Back Quote"),
	QUOTE(0xDE, "Quote"),
	KP_UP(0xE0, "Numpad Up"),
	KP_DOWN(0xE1, "Numpad Down"),
	KP_LEFT(0xE2, "Numpad Left"),
	KP_RIGHT(0xE3, "Numpad Right"),
	AMPERSAND(0x96, "Ampersand"),
	ASTERISK(0x97, "Asterisk"),
	QUOTEDBL(0x98, "Double Quote"),
	LESS(0x99, "Less"),
	GREATER(0xa0, "Greater"),
	BRACELEFT(0xa1, "Left Brace"),
	BRACERIGHT(0xa2, "Right Brace"),
	AT(0x0200, "At"),
	COLON(0x0201, "Colon"),
	CIRCUMFLEX(0x0202, "Circumflex"),
	DOLLAR(0x0203, "Dollar"),
	EURO_SIGN(0x0204, "Euro Sign"),
	EXCLAMATION_MARK(0x0205, "Exclamation Mark"),
	INVERTED_EXCLAMATION_MARK(0x0206, "Inverted Exclamation Mark"),
	LEFT_PARENTHESIS(0x0207, "Left Parenthesis"),
	NUMBER_SIGN(0x0208, "Number Sign"),
	PLUS(0x0209, "Plus"),
	RIGHT_PARENTHESIS(0x020A, "Right Parenthesis"),
	UNDERSCORE(0x020B, "Underscore"),
	WINDOWS(0x020C, "Windows"),
	CONTEXT_MENU(0x020D, "Context Menu"),
	FINAL(0x0018, "Final"),
	CONVERT(0x001C, "Convert"),
	NONCONVERT(0x001D, "Nonconvert"),
	ACCEPT(0x001E, "Accept"),
	MODECHANGE(0x001F, "Mode Change"),
	ALPHANUMERIC(0x00F0, "Alphanumeric"),
	CUT(0xFFD1, "Cut"),
	COPY(0xFFCD, "Copy"),
	PASTE(0xFFCF, "Paste"),
	UNDO(0xFFCB, "Undo"),
	ALT_GRAPH(0xFF7E, "Alt Graph"),
	SOFTKEY_0(0x1000, "Softkey 0"),
	SOFTKEY_1(0x1001, "Softkey 1"),
	SOFTKEY_2(0x1002, "Softkey 2"),
	SOFTKEY_3(0x1003, "Softkey 3"),
	SOFTKEY_4(0x1004, "Softkey 4"),
	SOFTKEY_5(0x1005, "Softkey 5"),
	SOFTKEY_6(0x1006, "Softkey 6"),
	SOFTKEY_7(0x1007, "Softkey 7"),
	SOFTKEY_8(0x1008, "Softkey 8"),
	SOFTKEY_9(0x1009, "Softkey 9");

	/** The platform code used to represent the character. */
	final int code;

	/** The string representation of the {@link #code}. */
	final String ch;

	/** The name of this key code. */
	final String name;

	KeyCode(int code, String name) {
		this.code = code;
		this.name = name;
		this.ch = String.valueOf((char)code);
	}

	/**
	 * Returns the underlying platform code used to represent the character.
	 *
	 * @return the underlying platform code.
	 */
	public final int getCode() {
		return code;
	}

	/**
	 * Returns the name of this key code.
	 *
	 * @return The name of this key code.
	 */
	public final String getName() {
		return name;
	}

}
