package org.lecturestudio.editor.api.recording.action;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.recording.action.TextChangeAction;

/**
 * Has an editable StringProperty, which allows for editing of the text content played by this PlaybackAction
 */
public class EditorTextChangeAction extends TextChangeAction {
	private StringProperty textProperty;

	public EditorTextChangeAction(int handle, StringProperty textProperty) {
		super(handle, textProperty.get());
		this.textProperty = textProperty;
	}

	public EditorTextChangeAction(byte[] input) throws IOException {
		super(input);
		parseFrom(input);
	}

	@Override
	public void execute(ToolController controller) throws Exception {
		controller.setText(handle, textProperty.get());
	}

	@Override
	public byte[] toByteArray() throws IOException {
		byte[] textData = textProperty.get().getBytes();
		int payloadBytes = 4 + 4 + textData.length;

		ByteBuffer buffer = createBuffer(payloadBytes);

		// Shape handle.
		buffer.putInt(handle);

		// Text
		buffer.putInt(textData.length);
		buffer.put(textData);

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = createBuffer(input);

		handle = buffer.getInt();

		int textLength = buffer.getInt();
		byte[] textData = new byte[textLength];

		buffer.get(textData);

		textProperty.set(new String(textData));
	}

}
