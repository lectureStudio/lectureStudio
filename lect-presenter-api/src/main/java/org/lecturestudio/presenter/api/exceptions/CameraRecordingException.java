package org.lecturestudio.presenter.api.exceptions;

import org.lecturestudio.core.ExecutableException;

public class CameraRecordingException  extends ExecutableException {
	/**
	 * Construct a new ExecutableException with the specified message and
	 * throwable.
	 *
	 * @param message   A Message describing this exception.
	 * @param throwable A Throwable that caused this exception.
	 */
	public CameraRecordingException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
