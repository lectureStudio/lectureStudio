package org.lecturestudio.presenter.api.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lecturestudio.core.ExecutableException;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

class StopwatchTest {

	Stopwatch stopwatch;


	@BeforeEach
	void setup() {
		stopwatch = new Stopwatch();
	}

	@Test
	final void testFormat() {
		String input = "11:11";

		stopwatch.setStartTime(getLocalTime(input));
		assertEquals("11:11", stopwatch.getTime().toString());
	}

	@Test
	final void testResetStopwatch() throws ExecutableException {
		stopwatch.start();
		for (int i = 0; i < 60; i++) {
			stopwatch.update();
		}
		assertEquals("00:01", stopwatch.getTime().toString());
		stopwatch.reset();
		assertEquals("00:00", stopwatch.getTime().toString());
	}

	@Test
	final void testResetPresetStopwatch() throws ExecutableException {
		String input = "11:11";

		stopwatch.setStartTime(getLocalTime(input));
		stopwatch.start();
		for (int i = 0; i < 60; i++) {
			stopwatch.update();
		}
		assertEquals("02:05", stopwatch.getTime().toString());
		stopwatch.reset();
		assertEquals("02:04", stopwatch.getTime().toString());
	}

	@Test
	final void testEndOfTimer() throws ExecutableException {
		String input = "60";

		stopwatch.setStartTime(getLocalTime(input));
		stopwatch.setType(Stopwatch.StopwatchType.TIMER);
		stopwatch.start();
		for (int i = 0; i < 61; i++) {
			stopwatch.update();
		}
		assertEquals("00:00", stopwatch.getTime().toString());
		assertSame(stopwatch.getTimeIndication(), Stopwatch.TimeIndication.ENDED);
		stopwatch.reset();
		assertEquals("00:01", stopwatch.getTime().toString());
		assertNotSame(stopwatch.getTimeIndication(), Stopwatch.TimeIndication.ENDED);
	}

	private LocalTime getLocalTime(String timeString) {
		return LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"));
	}
}
