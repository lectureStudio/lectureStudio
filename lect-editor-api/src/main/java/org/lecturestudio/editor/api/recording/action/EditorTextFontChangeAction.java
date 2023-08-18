package org.lecturestudio.editor.api.recording.action;

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.recording.action.TextFontChangeAction;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.text.FontPosture;
import org.lecturestudio.core.text.FontWeight;
import org.lecturestudio.core.text.TextAttributes;

public class EditorTextFontChangeAction extends TextFontChangeAction {

	private int handle;

	protected ObjectProperty<Color> color;

	protected ObjectProperty<Font> font;

	protected ObjectProperty<TextAttributes> attributes;

	public EditorTextFontChangeAction(int handle, ObjectProperty<Color> color,
	                                  ObjectProperty<Font> font,
	                                  ObjectProperty<TextAttributes> attributes) {
		super(handle, color.get(), font.get(), attributes.get());

		this.handle = handle;
		this.color = color;
		this.font = font;
		this.attributes = attributes;
	}

	public EditorTextFontChangeAction(byte[] input) throws IOException {
		super(input);
		parseFrom(input);
	}

	@Override
	public void execute(ToolController controller) throws Exception {
		controller.setTextFont(handle, color.get(), font.get(), attributes.get());
	}

	@Override
	public byte[] toByteArray() throws IOException {
		TextAttributes attributes = font.get().getTextAttributes();
		byte[] fontFamily = font.get().getFamilyName().getBytes();

		int payloadBytes = 4 + 2 + 4 + 14 + fontFamily.length;

		ByteBuffer buffer = createBuffer(payloadBytes);

		// Shape handle.
		buffer.putInt(handle);

		buffer.putInt(color.get().getRGBA());

		// Font: 14 + X bytes.
		buffer.putInt(fontFamily.length);
		buffer.put(fontFamily);
		buffer.putDouble(font.get().getSize());
		buffer.put((byte) font.get().getPosture().ordinal());
		buffer.put((byte) font.get().getWeight().ordinal());

		// Text attributes: 2 bytes.
		buffer.put((byte) ((nonNull(attributes) && attributes.isStrikethrough()) ? 1 : 0));
		buffer.put((byte) ((nonNull(attributes) && attributes.isUnderline()) ? 1 : 0));

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = createBuffer(input);

		handle = buffer.getInt();

		color.set(new Color(buffer.getInt()));

		// Font
		int familyLength = buffer.getInt();
		byte[] familyStr = new byte[familyLength];
		buffer.get(familyStr);
		double fontSize = buffer.getDouble();
		FontPosture posture = FontPosture.values()[buffer.get()];
		FontWeight weight = FontWeight.values()[buffer.get()];

		font.set(new Font(new String(familyStr), fontSize));
		font.get().setPosture(posture);
		font.get().setWeight(weight);

		// Text attributes
		boolean strikethrough = buffer.get() > 0;
		boolean underline = buffer.get() > 0;

		attributes.set(new TextAttributes());
		attributes.get().setStrikethrough(strikethrough);
		attributes.get().setUnderline(underline);

		font.get().setTextAttributes(attributes.get().clone());
	}
}
