package org.lecturestudio.editor.api.view;

import org.lecturestudio.core.view.ProgressDialogView;
import org.lecturestudio.core.view.View;

public class ProgressDialogMockView implements ProgressDialogView {
	public String errorMessage;
	public String message;
	public String messageTitle;
	public double progress;
	public View parent;
	public Runnable onShown;
	public Runnable onHidden;
	public boolean opened;
	public boolean closed;
	public String errorMessageTitle;
	public String errorMessageText;

	@Override
	public void setError(String message) {
		this.errorMessage = message;
	}

	@Override
	public void setError(String message, String error) {
		this.errorMessageTitle = message;
		this.errorMessageText = error;
	}

	@Override
	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public void setMessageTitle(String title) {
		this.messageTitle = title;
	}

	@Override
	public void setProgress(double progress) {
		this.progress = progress;
	}

	@Override
	public void setParent(View parent) {
		this.parent = parent;
	}

	@Override
	public void setOnShown(Runnable runnable) {
		this.onShown = runnable;
	}

	@Override
	public void setOnHidden(Runnable runnable) {
		this.onHidden = runnable;
	}

	@Override
	public void open() {
		this.opened = true;
		this.closed = false;
	}

	@Override
	public void close() {
		this.closed = true;
		this.opened = false;
	}
}
