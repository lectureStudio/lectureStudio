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

package org.lecturestudio.core.recording.action;

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.text.FontPosture;
import org.lecturestudio.core.text.FontWeight;
import org.lecturestudio.core.text.TextAttributes;

public class TextFontChangeAction extends PlaybackAction {

	protected int handle;

	protected TextAttributes attributes;

	protected Color color;

	protected Font font;


	public TextFontChangeAction(int handle, Color color, Font font, TextAttributes attributes) {
		this.handle = handle;
		this.color = color;
		this.font = font;
		this.attributes = attributes;
	}

	public TextFontChangeAction(byte[] input) throws IOException {
		parseFrom(input);
	}

	@Override
	public void execute(ToolController controller) throws Exception {
		controller.setTextFont(handle, color, font, attributes);
	}

	@Override
	public byte[] toByteArray() throws IOException {
		TextAttributes attributes = font.getTextAttributes();
		byte[] fontFamily = font.getFamilyName().getBytes();

		int payloadBytes = 4 + 2 + 4 + 14 + fontFamily.length;

		ByteBuffer buffer = createBuffer(payloadBytes);

		// Shape handle.
		buffer.putInt(handle);

		buffer.putInt(color.getRGBA());

		// Font: 14 + X bytes.
		buffer.putInt(fontFamily.length);
		buffer.put(fontFamily);
		buffer.putDouble(font.getSize());
		buffer.put((byte) font.getPosture().ordinal());
		buffer.put((byte) font.getWeight().ordinal());

		// Text attributes: 2 bytes.
		buffer.put((byte) ((nonNull(attributes) && attributes.isStrikethrough()) ? 1 : 0));
		buffer.put((byte) ((nonNull(attributes) && attributes.isUnderline()) ? 1 : 0));

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = createBuffer(input);

		handle = buffer.getInt();

		color = new Color(buffer.getInt());

		// Font
		int familyLength = buffer.getInt();
		byte[] familyStr = new byte[familyLength];
		buffer.get(familyStr);
		double fontSize = buffer.getDouble();
		FontPosture posture = FontPosture.values()[buffer.get()];
		FontWeight weight = FontWeight.values()[buffer.get()];

		font = new Font(new String(familyStr), fontSize);
		font.setPosture(posture);
		font.setWeight(weight);

		// Text attributes
		boolean strikethrough = buffer.get() > 0;
		boolean underline = buffer.get() > 0;

		attributes = new TextAttributes();
		attributes.setStrikethrough(strikethrough);
		attributes.setUnderline(underline);

		font.setTextAttributes(attributes.clone());
	}

	@Override
	public ActionType getType() {
		return ActionType.TEXT_FONT_CHANGE;
	}

	@Override
	public boolean hasHandle() {
		return true;
	}

	@Override
	public int getHandle() {
		return handle;
	}
}