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
	protected ObjectProperty<Color> colorProperty;

	protected ObjectProperty<Font> fontProperty;

	protected ObjectProperty<TextAttributes> attributesProperty;

	public EditorTextFontChangeAction(int handle, ObjectProperty<Color> color,
									  ObjectProperty<Font> font,
									  ObjectProperty<TextAttributes> attributes) {
		super(handle, color.get(), font.get(), attributes.get());

		this.colorProperty = color;
		this.fontProperty = font;
		this.attributesProperty = attributes;
	}

	public EditorTextFontChangeAction(byte[] input) throws IOException {
		super(input);

		colorProperty = new ObjectProperty<>();
		fontProperty = new ObjectProperty<>();
		attributesProperty = new ObjectProperty<>();

		parseFrom(input);
	}

	@Override
	public void execute(ToolController controller) throws Exception {
		color = colorProperty.get();
		font = fontProperty.get();
		attributes = attributesProperty.get();

		super.execute(controller);
	}

	@Override
	public byte[] toByteArray() throws IOException {
		TextAttributes attributes = fontProperty.get().getTextAttributes();
		byte[] fontFamily = fontProperty.get().getFamilyName().getBytes();

		int payloadBytes = 4 + 2 + 4 + 14 + fontFamily.length;

		ByteBuffer buffer = createBuffer(payloadBytes);

		// Shape handle.
		buffer.putInt(handle);

		buffer.putInt(colorProperty.get().getRGBA());

		// Font: 14 + X bytes.
		buffer.putInt(fontFamily.length);
		buffer.put(fontFamily);
		buffer.putDouble(fontProperty.get().getSize());
		buffer.put((byte) fontProperty.get().getPosture().ordinal());
		buffer.put((byte) fontProperty.get().getWeight().ordinal());

		// Text attributes: 2 bytes.
		buffer.put((byte) ((nonNull(attributes) && attributes.isStrikethrough()) ? 1 : 0));
		buffer.put((byte) ((nonNull(attributes) && attributes.isUnderline()) ? 1 : 0));

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = createBuffer(input);

		handle = buffer.getInt();

		colorProperty.set(new Color(buffer.getInt()));

		// Font
		int familyLength = buffer.getInt();
		byte[] familyStr = new byte[familyLength];
		buffer.get(familyStr);
		double fontSize = buffer.getDouble();
		FontPosture posture = FontPosture.values()[buffer.get()];
		FontWeight weight = FontWeight.values()[buffer.get()];

		fontProperty.set(new Font(new String(familyStr), fontSize));
		fontProperty.get().setPosture(posture);
		fontProperty.get().setWeight(weight);

		// Text attributes
		boolean strikethrough = buffer.get() > 0;
		boolean underline = buffer.get() > 0;

		attributesProperty.set(new TextAttributes());
		attributesProperty.get().setStrikethrough(strikethrough);
		attributesProperty.get().setUnderline(underline);

		fontProperty.get().setTextAttributes(attributesProperty.get().clone());
	}
}
