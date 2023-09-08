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

package org.lecturestudio.presenter.api.service;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.web.api.janus.JanusStateHandlerListener;

/**
 * Stream state observer to executable component adapter. This component will
 * set its executable state depending on the stream state.
 *
 * @author Alex Andres
 */
public class StreamStateHandler extends ExecutableBase
		implements JanusStateHandlerListener {

	@Override
	public void connected() {
		setStarted();
	}

	@Override
	public void disconnected() {
		setStopped();
	}

	@Override
	public void failed() {
		setStopped();
	}

	@Override
	public void error(Throwable throwable) {
		setStopped();
	}

	@Override
	protected void initInternal() {
	}

	@Override
	protected void startInternal() {
	}

	@Override
	protected void stopInternal() {
	}

	@Override
	protected void destroyInternal() {
	}

	private void setStarted() {
		try {
			start();
		}
		catch (ExecutableException e) {
			e.printStackTrace();
		}
	}

	private void setStopped() {
		try {
			stop();
		}
		catch (ExecutableException e) {
			e.printStackTrace();
		}
	}
}
