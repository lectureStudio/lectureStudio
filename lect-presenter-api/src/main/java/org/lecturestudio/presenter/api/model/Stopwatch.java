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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

	private final AtomicBoolean runStopwatch = new AtomicBoolean(false);

	/** Duration in minutes. */
	private Long duration;

	private LocalDateTime endTime;

	private LocalTime startTime;

	/** The current time. */
	private Time time = new Time();

	private TimeIndication timeIndication = TimeIndication.OPTIMAL;

	private StopwatchType type = StopwatchType.STOPWATCH;

	private boolean resettable = true;

	private boolean presentationStarted = false;


	/**
	 * Creates a new Stopwatch.
	 */
	public Stopwatch() {

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
		this.endTime = LocalDateTime.of(LocalDate.now(), time);
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
	 * left to start the presentation, the pace should increase / decrease, or the time
	 * is up.
	 *
	 * @return The time current indication.
	 */
	public TimeIndication getTimeIndication() {
		return timeIndication;
	}

	/**
	 * Sets the current time indication, which tells the user whether there is time
	 * left to start the presentation, the pace should increase / decrease, or the time
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
		if (presentationStarted || timeIndication == TimeIndication.ENDED) {
			return;
		}

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
	 * Reset the stopwatch to the last configured time and stops it
	 */
	public void reset() {
		if (!resettable) {
			return;
		}
		switch (timeIndication) {
			case WAITING:
			case ENDED:
				return;
		}

		timeIndication = TimeIndication.OPTIMAL;

		if (type == StopwatchType.STOPWATCH) {
			// Autostart the counting.
			startTime = LocalTime.now();
			presentationStarted = true;
		}
		else {
			presentationStarted = false;
			startTime = null;
		}
	}

	/**
	 * Handles all incoming changes to the current stopwatch.
	 */
	public synchronized void update() {
		if (runStopwatch.get()) {
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
		}
		else if (nonNull(endTime) && nonNull(duration)) {
			setType(StopwatchType.TIMER);
			setStartTime(LocalTime.now());
		}

		if (nonNull(endTime) && nonNull(duration)) {
			LocalDateTime now = LocalDateTime.now();
			long timeDiffMs = Duration.between(now, endTime).toMinutes();

			if (timeDiffMs == 0) {
				// The end time is right now, start the presentation timer.
				endTime = now.plusMinutes(duration + 1);
				resettable = false;
				setPresentationStarted();
			}
			else if (timeDiffMs < 0) {
				// The end time is on the next day.
				endTime = endTime.plusDays(1);
			}
		}
	}

	@Override
	protected void suspendInternal() {
		runStopwatch.set(false);
	}

	@Override
	protected void startInternal() {
		if (runStopwatch.compareAndSet(false, true)) {
			reset();
		}
	}

	@Override
	protected void stopInternal() {
		runStopwatch.set(false);
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

			if (timeDiffMs > 0) {
				// Time is up.
				setTimeIndication(TimeIndication.ENDED);
			}
		}
		else {
			long timeDiffMs = endTime.minusMinutes(duration).until(LocalDateTime.now(), ChronoUnit.MILLIS);

			if (timeDiffMs < 0) {
				// Count waiting time down.
				setTimeIndication(TimeIndication.WAITING);
				setTime(createTime(timeDiffMs));
			}
			else {
				if (timeIndication == TimeIndication.WAITING) {
					// Update once, switch indication.
					setTime(new Time(Duration.ofMinutes(duration).toMillis()));
					setPresentationStarted();
					resettable = false;
				}
				else {
					// Count presentation time up.
					timeDiffMs = LocalDateTime.now().until(endTime, ChronoUnit.MILLIS);

					setTime(createTime(timeDiffMs));

					if (timeDiffMs < 0) {
						setTimeIndication(TimeIndication.ENDED);
					}
				}
			}
		}
	}
}
