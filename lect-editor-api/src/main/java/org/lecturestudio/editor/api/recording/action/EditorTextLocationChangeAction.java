package org.lecturestudio.editor.api.recording.action;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.recording.action.TextLocationChangeAction;
import org.lecturestudio.editor.api.controller.EditorToolController;

public class EditorTextLocationChangeAction extends TextLocationChangeAction {
	private int handle;
	private Rectangle2D location;

	public EditorTextLocationChangeAction(int handle, Rectangle2D location) {
		super(handle, location.getLocation());
		this.handle = handle;
		this.location = location;
	}

	public EditorTextLocationChangeAction(byte[] input) throws IOException {
		super(input);
		parseFrom(input);
	}

	@Override
	public void execute(ToolController controller) throws Exception {
		if (controller instanceof EditorToolController editorToolController) {
			editorToolController.setTextLocation(handle, location);
		}
		else {
			controller.setTextLocation(handle, location.getLocation());
		}
	}

	@Override
	public byte[] toByteArray() throws IOException {
		int payloadBytes = 4 + 16;

		ByteBuffer buffer = createBuffer(payloadBytes);

		// Shape handle.
		buffer.putInt(handle);

		// Location
		buffer.putDouble(location.getX());
		buffer.putDouble(location.getY());

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = createBuffer(input);

		handle = buffer.getInt();

		double x = buffer.getDouble();
		double y = buffer.getDouble();

		location = new Rectangle2D();
		location.setLocation(x, y);
	}


}
