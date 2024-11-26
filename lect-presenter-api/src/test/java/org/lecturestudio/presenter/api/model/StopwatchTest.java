package org.lecturestudio.presenter.api.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lecturestudio.core.ExecutableException;

class StopwatchTest {

	Stopwatch stopwatch;


	@BeforeEach
	void setup() {
		stopwatch = new Stopwatch();
	}

	@Test
	final void testHoursMinutesSecondsFormat() {
		String input = "1:119:65";

		stopwatch.setStopwatchIntervalByString(input);
		assertEquals("03:00:05", stopwatch.calculateCurrentStopwatch());
	}

	@Test
	final void testMinutesSecondsFormat() {
		String input = "179:65";

		stopwatch.setStopwatchIntervalByString(input);
		assertEquals("03:00:05", stopwatch.calculateCurrentStopwatch());
	}

	@Test
	final void testSecondsFormat() {
		String input = "10805";

		stopwatch.setStopwatchIntervalByString(input);
		assertEquals("03:00:05", stopwatch.calculateCurrentStopwatch());
	}

	@Test
	final void testResetStopwatch() throws ExecutableException {
		stopwatch.start();
		for (int i = 0; i < 60; i++) {
			stopwatch.update();
		}
		assertEquals("00:01:00", stopwatch.calculateCurrentStopwatch());
		stopwatch.reset();
		assertEquals("00:00:00", stopwatch.calculateCurrentStopwatch());
	}

	@Test
	final void testResetPresetStopwatch() throws ExecutableException {
		String input = "7445";

		stopwatch.setStopwatchIntervalByString(input);
		stopwatch.start();
		for (int i = 0; i < 60; i++) {
			stopwatch.update();
		}
		assertEquals("02:05:05", stopwatch.calculateCurrentStopwatch());
		stopwatch.reset();
		assertEquals("02:04:05", stopwatch.calculateCurrentStopwatch());
	}

	@Test
	final void testEndOfTimer() throws ExecutableException {
		String input = "60";

		stopwatch.setStopwatchIntervalByString(input);
		stopwatch.setType(Stopwatch.StopwatchType.TIMER);
		stopwatch.start();
		for (int i = 0; i < 61; i++) {
			stopwatch.update();
		}
		assertEquals("00:00:00", stopwatch.calculateCurrentStopwatch());
		assertSame(stopwatch.getTimeIndication(), Stopwatch.TimeIndication.ENDED);
		stopwatch.reset();
		assertEquals("00:01:00", stopwatch.calculateCurrentStopwatch());
		assertNotSame(stopwatch.getTimeIndication(), Stopwatch.TimeIndication.ENDED);
	}
}
