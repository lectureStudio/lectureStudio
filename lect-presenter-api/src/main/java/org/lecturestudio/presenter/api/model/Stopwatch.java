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

import org.lecturestudio.core.model.Time;

/**
 * Stopwatch that needs to be updated every second to show the correct time.
 *
 * @author Dustin Ringel
 */
public class Stopwatch {

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

	private Time time;

	private TimeIndication timeIndication;

	private boolean runStopwatch;

	private boolean timerEnded = false;

	private int timerEndedInterval = 0;

	private int resetStopwatchInterval = 0;

	private StopwatchType type = StopwatchType.STOPWATCH;


	public Stopwatch() {
		resetStopwatch();
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public void setEndTime(LocalTime time) {
		this.endTime = time;
	}

	public void setStartTime(LocalTime time) {
		this.startTime = time;
	}

	/**
	 * Reset the stopwatch to the last configured time and stops it
	 */
	public void resetStopwatch() {
		runStopwatch = false;
		timerEndedInterval = 0;
		timerEnded = false;
		timeIndication = TimeIndication.OPTIMAL;
		time = new Time(0);
	}

	/**
	 * Starts the current stopwatch.
	 */
	public void startStopwatch() {
		runStopwatch = true;
	}

	/**
	 * Stops the current stopwatch.
	 */
	public void stopStopwatch() {
		runStopwatch = false;
	}

	/**
	 * Switching between running and paused stopwatch.
	 */
	public void startStopStopwatch() {
		runStopwatch = !runStopwatch;
	}

	public void init() {
		if (nonNull(startTime) && nonNull(endTime)) {
			setStopwatchType(StopwatchType.TIMER);
			setDuration(Duration.between(startTime, endTime).toMinutes());
			setTime(new Time(Math.abs(endTime.minusMinutes(duration)
					.until(LocalTime.now(), ChronoUnit.MILLIS))));
		}
		else if (nonNull(endTime) && nonNull(duration)) {
			setStopwatchType(StopwatchType.TIMER);
			setTime(new Time(Math.abs(endTime.minusMinutes(duration)
					.until(LocalTime.now(), ChronoUnit.MILLIS))));
		}
	}

	/**
	 * Handles all incoming changes to the current stopwatch.
	 */
	public synchronized void updateStopwatchInterval() {
		if (runStopwatch) {
			if (type == StopwatchType.STOPWATCH) {
				//stopwatchInterval++;
			}
			else if (type == StopwatchType.TIMER) {
				runTimer();

				/*
				if (stopwatchInterval > 0) {
					stopwatchInterval--;
					timerEnded = false;
				}
				else {
					if (timerEndedInterval <= 11) {
						timerEnded = true;
						timerEndedInterval++;
						setTimeIndication(TimeIndication.ENDED);
					}
					else {
						//runStopwatch = false;
					}
				}
				*/
			}
		}
	}

	public Time getTime() {
		return time;
	}

	private void setTime(Time time) {
		this.time = time;
	}

	public TimeIndication getTimeIndication() {
		return timeIndication;
	}

	public void setTimeIndication(TimeIndication timeIndication) {
		this.timeIndication = timeIndication;
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
			String[] timesteps = time.split(":");

			int actualTime = switch (timesteps.length) {
				case 1 -> Integer.parseInt(timesteps[0]);
				case 2 -> 60 * Integer.parseInt(timesteps[0]) + Integer.parseInt(timesteps[1]);
				case 3 -> 60 * 60 * Integer.parseInt(timesteps[0])
						+ 60 * Integer.parseInt(timesteps[1])
						+ Integer.parseInt(timesteps[2]);
				default -> 0;
			};

			//stopwatchInterval = actualTime;
			resetStopwatchInterval = actualTime;
		}
	}

	public StopwatchType getType() {
		return type;
	}

	public boolean isTimerEnded() {
		return timerEnded;
	}

	public void setStopwatchType(StopwatchType type) {
		this.type = type;
	}

	public int getTimerEndedInterval() {
		return timerEndedInterval;
	}

	private void runTimer() {
		long timeDiffMs = endTime.minusMinutes(duration)
				.until(LocalTime.now(), ChronoUnit.MILLIS);

		System.out.println("timeDiffMs: " + timeDiffMs);

		if (timeDiffMs < 0) {
			setTimeIndication(TimeIndication.WAITING);
			setTime(new Time(Duration.ofMillis(Math.abs(timeDiffMs)).toMillis()));
		}
		else {
			//System.out.println("1: " + timeIndication);

			if (timeIndication == TimeIndication.WAITING) {
				// Update once.
				System.out.println("Update once: " + Duration.ofMinutes(duration).toSeconds());
				setTime(new Time(Duration.ofMinutes(duration).toMillis()));
				setTimeIndication(TimeIndication.OPTIMAL);
			}
		}

	}
}
