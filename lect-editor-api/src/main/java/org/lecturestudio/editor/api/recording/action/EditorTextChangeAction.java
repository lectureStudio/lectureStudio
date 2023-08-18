package org.lecturestudio.editor.api.recording.action;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.recording.action.TextChangeAction;
import org.lecturestudio.editor.api.controller.EditorToolController;

public class EditorTextChangeAction extends TextChangeAction {
	private int handle;
	private StringProperty textProperty;

	public EditorTextChangeAction(int handle, StringProperty textProperty) {
		super(handle, textProperty.get());
		this.handle = handle;
		this.textProperty = textProperty;

		textProperty.addListener((observable, oldValue, newValue) -> {
			System.out.println(newValue);
		});
	}

	public EditorTextChangeAction(byte[] input) throws IOException {
		super(input);
		parseFrom(input);

		textProperty.addListener((observable, oldValue, newValue) -> {
			System.out.println(newValue);
		});
	}

	@Override
	public void execute(ToolController controller) throws Exception {
		if (controller instanceof EditorToolController editorToolController) {
			editorToolController.setText(handle, textProperty);
		}
		else {
			controller.setText(handle, textProperty.get());
		}
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
