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

package org.lecturestudio.presenter.api.model;

import static java.util.Objects.nonNull;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.model.Time;

/**
 * Stopwatch that needs to be updated every second to show the correct time.
 *
 * @author Dustin Ringel
 * @author Alex Andres
 */
public class Stopwatch extends ExecutableBase {

	public enum StopwatchType {
		TIMER,
		STOPWATCH
	}

	public enum TimeIndication {
		WAITING,
		OPTIMAL,
		SPEED_UP,
		SLOW_DOWN,
		ENDED
	}

	// Duration in minutes.
	private Long duration;

	private LocalTime endTime;

	private LocalTime startTime;

	private Time time = new Time();

	private TimeIndication timeIndication;

	private StopwatchType type = StopwatchType.STOPWATCH;

	private boolean presentationStarted;

	private boolean runStopwatch;


	/**
	 * Creates a new Stopwatch.
	 */
	public Stopwatch() {
		reset();
	}

	/**
	 * Sets the duration of the presentation.
	 *
	 * @param duration The duration in minutes.
	 */
	public void setDuration(Long duration) {
		this.duration = duration;
	}

	/**
	 * Sets the end time of the presentation.
	 *
	 * @param time The end time in the local time format HH:MM (24h).
	 */
	public void setEndTime(LocalTime time) {
		this.endTime = time;
	}

	/**
	 * Sets the start time of the presentation.
	 *
	 * @param time The start time in the local time format HH:MM (24h).
	 */
	public void setStartTime(LocalTime time) {
		this.startTime = time;
	}

	/**
	 * Gets the current time, whether it is the waiting time or the presentation time.
	 *
	 * @return The current time.
	 */
	public Time getTime() {
		return time;
	}

	/**
	 * Sets the current time, whether it is the waiting time or the presentation time.
	 *
	 * @param time The current time.
	 */
	private void setTime(Time time) {
		this.time = time;
	}

	/**
	 * Gets the current time indication, which tells the user whether there is time
	 * left to start the presentation, the pace should increase / decrease or the time
	 * is up.
	 *
	 * @return The time current indication.
	 */
	public TimeIndication getTimeIndication() {
		return timeIndication;
	}

	/**
	 * Sets the current time indication, which tells the user whether there is time
	 * left to start the presentation, the pace should increase / decrease or the time
	 * is up.
	 *
	 * @param timeIndication The time current indication.
	 */
	public void setTimeIndication(TimeIndication timeIndication) {
		this.timeIndication = timeIndication;
	}

	/**
	 * Gets the type of this watch. The type STOPWATCH will count up the time, the type
	 * TIMER will count down the time left for the presentation.
	 *
	 * @return The current type of the watch.
	 */
	public StopwatchType getType() {
		return type;
	}

	/**
	 * Gets the type of this watch. The type STOPWATCH will count up the time, the type
	 * TIMER will count down the time left for the presentation.
	 *
	 * @param type The current type of the watch.
	 */
	public void setType(StopwatchType type) {
		this.type = type;
	}

	/**
	 * Changes the internal state to start the timer with the duration of the presentation.
	 */
	public void setPresentationStarted() {
		setTimeIndication(TimeIndication.OPTIMAL);

		if (nonNull(duration)) {
			startTime = LocalTime.now().plusMinutes(duration);
		}
		else {
			startTime = LocalTime.now();
		}

		presentationStarted = true;
	}

	/**
     * Creates a string with the current time.
     *
     * @return the current stopwatch time as string
     */
	public String calculateCurrentStopwatch() {
		return time.toString();
	}

	/**
	 * Transforms a given string into an actual stopwatch time.
	 *
	 * @param time The stopwatch time in format "ss", "mm:ss" or "hh:mm:ss"
	 */
	public void setStopwatchIntervalByString(String time) {
		if (!time.trim().isEmpty()) {
			//stopwatchInterval = actualTime;
		}
	}

	/**
	 * Reset the stopwatch to the last configured time and stops it
	 */
	public void reset() {
		presentationStarted = false;
		startTime = null;
		timeIndication = TimeIndication.OPTIMAL;

		if (type == StopwatchType.STOPWATCH) {
			// Autostart the counting.
			startTime = LocalTime.now();
			presentationStarted = true;
		}
	}

	/**
	 * Handles all incoming changes to the current stopwatch.
	 */
	public synchronized void update() {
		if (runStopwatch) {
			if (type == StopwatchType.STOPWATCH) {
				runStopwatch();
			}
			else if (type == StopwatchType.TIMER) {
				runTimer();
			}
		}
	}

	@Override
	protected void initInternal() throws ExecutableException {
		if (nonNull(startTime) && nonNull(endTime)) {
			setType(StopwatchType.TIMER);
			setDuration(Duration.between(startTime, endTime).toMinutes());
			setTime(new Time(Math.abs(endTime.minusMinutes(duration)
						.until(LocalTime.now(), ChronoUnit.MILLIS))));
		}
		else if (nonNull(endTime) && nonNull(duration)) {
			setType(StopwatchType.TIMER);
			setTime(new Time(Math.abs(endTime.minusMinutes(duration)
						.until(LocalTime.now(), ChronoUnit.MILLIS))));
		}
	}

	@Override
	protected void suspendInternal() {
		runStopwatch = false;
	}

	@Override
	protected void startInternal() {
		runStopwatch = true;
	}

	@Override
	protected void stopInternal() {
		runStopwatch = false;
	}

	@Override
	protected void destroyInternal() {

	}

	private Time createTime(long millis) {
		return new Time(Duration.ofMillis(Math.abs(millis)).toMillis());
	}

	private void runStopwatch() {
		if (presentationStarted) {
			// Count presentation time down.
			long timeDiffMs = startTime.until(LocalTime.now(), ChronoUnit.MILLIS);

			setTime(createTime(timeDiffMs));
		}
	}

	private void runTimer() {
		if (presentationStarted) {
			// Count presentation time down.
			long timeDiffMs = startTime.until(LocalTime.now(), ChronoUnit.MILLIS);

			setTime(createTime(timeDiffMs));
		}
		else {
			long timeDiffMs = endTime.minusMinutes(duration).until(LocalTime.now(), ChronoUnit.MILLIS);

			if (timeDiffMs < 0) {
				// Count waiting time down.
				setTimeIndication(TimeIndication.WAITING);
				setTime(createTime(timeDiffMs));
			}
			else {
				if (timeIndication == TimeIndication.WAITING) {
					// Update once, switch indication.
					setTime(new Time(Duration.ofMinutes(duration).toMillis()));
					setTimeIndication(TimeIndication.OPTIMAL);
				}
				else {
					// Count presentation time up.
					timeDiffMs = endTime.until(LocalTime.now(), ChronoUnit.MILLIS);

					setTime(createTime(timeDiffMs));
				}
			}
		}
	}
}
