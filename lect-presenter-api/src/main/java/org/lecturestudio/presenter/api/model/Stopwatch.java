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

	private int stopwatchInterval;

	private boolean resetStopwatch;

	private boolean runStopwatch;

	private boolean timerEnded = false;

	private int timerEndedInterval = 0;

	private int resetStopwatchInterval = 0;

	private StopwatchType type = StopwatchType.STOPWATCH;


	public Stopwatch() {
		stopwatchInterval = 0;
		resetStopwatch = false;
		runStopwatch = false;
	}

	/**
	 * Reset the stopwatch to the last configured time and stops it
	 */
	public void resetStopwatch() {
		stopwatchInterval = resetStopwatchInterval;
		runStopwatch = false;
		timerEndedInterval = 0;
		timerEnded = false;
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

	/**
	 * Handles all incoming changes to the current stopwatch.
	 */
	public void updateStopwatchInterval() {
		if (runStopwatch) {
			if (type == StopwatchType.STOPWATCH) {
				stopwatchInterval++;
			}
			else if (type == StopwatchType.TIMER) {
				if (stopwatchInterval > 0) {
					stopwatchInterval--;
					timerEnded = false;
				}
				else {
					if (timerEndedInterval <= 11) {
						timerEnded = true;
						timerEndedInterval++;
					}
					else {
						runStopwatch = false;
					}
				}
			}
		}
	}

	/**
     * Creates a string with the current time.
     *
     * @return the current stopwatch time as string
     */
	public String calculateCurrentStopwatch() {
		int sec = stopwatchInterval % 60;
		int min = stopwatchInterval / 60 % 60;
		int h = stopwatchInterval / 60 / 60;
		String secStr = String.format("%02d", sec);
		String minStr = String.format("%02d", min);
		String hStr = String.format("%02d", h);

		return hStr + ":" + minStr + ":" + secStr;
	}

	/**
	 * Transforms a given string into an actual stopwatch time.
	 *
	 * @param time The stopwatchtime in format "ss", "mm:ss" or "hh:mm:ss"
	 */
	public void setStopwatchIntervalByString(String time) {
		if (!time.trim().equals("")) {
			String[] timesteps = time.split(":");

			int actualTime = switch (timesteps.length) {
				case 1 -> Integer.parseInt(timesteps[0]);
				case 2 ->
						60 * Integer.parseInt(timesteps[0]) + Integer.parseInt(
								timesteps[1]);
				case 3 -> 60 * 60 * Integer.parseInt(timesteps[0])
						+ 60 * Integer.parseInt(timesteps[1])
						+ Integer.parseInt(timesteps[2]);
				default -> 0;
			};
			stopwatchInterval = actualTime;
			resetStopwatchInterval = actualTime;
		}
	}

	public StopwatchType getType() {
		return type;
	}

	public void setRunStopwatch(boolean runStopwatch) {
		this.runStopwatch = runStopwatch;
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
}
