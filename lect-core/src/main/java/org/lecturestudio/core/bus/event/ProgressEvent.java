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
package org.lecturestudio.core.bus.event;

public class ProgressEvent extends BusEvent {

	private enum State { STARTED, RUNNING, FINISHED }
	
	private State state;
	
	private float progress;


	public ProgressEvent() {
		setProgress(0);
	}
	
	public ProgressEvent(float progress) {
		setProgress(progress);
	}
	
	public boolean started() {
		return state == State.STARTED;
	}
	
	public boolean running() {
		return state == State.RUNNING;
	}
	
	public boolean finished() {
		return state == State.FINISHED;
	}	

	public float getProgress() {
		return progress;
	}

	public void setProgress(float progress) {
		this.progress = progress;
		
		if (progress <= 0) {
			this.state = State.STARTED;
		}
		else if (progress > 0 && progress < 1) {
			this.state = State.RUNNING;
		}
		else if (progress >= 1) {
			this.state = State.FINISHED;
		}
	}
	
}
