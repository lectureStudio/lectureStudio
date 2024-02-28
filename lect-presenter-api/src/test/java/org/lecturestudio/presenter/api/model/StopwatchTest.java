package org.lecturestudio.presenter.api.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    final void testResetStopwatch() {
        stopwatch.startStopStopwatch();
        for(int i = 0; i < 60; i++){
            stopwatch.updateStopwatchInterval();
        }
        assertEquals("00:01:00", stopwatch.calculateCurrentStopwatch());
        stopwatch.resetStopwatch();
        assertEquals("00:00:00", stopwatch.calculateCurrentStopwatch());
    }

    @Test
    final void testResetPresetStopwatch() {
        String input = "7445";

        stopwatch.setStopwatchIntervalByString(input);
        stopwatch.startStopStopwatch();
        for(int i = 0; i < 60; i++){
            stopwatch.updateStopwatchInterval();
        }
        assertEquals("02:05:05", stopwatch.calculateCurrentStopwatch());
        stopwatch.resetStopwatch();
        assertEquals("02:04:05", stopwatch.calculateCurrentStopwatch());
    }

    @Test
    final void testEndOfTimer() {
        String input = "60";

        stopwatch.setStopwatchIntervalByString(input);
        stopwatch.setStopwatchType(Stopwatch.StopwatchType.TIMER);
        stopwatch.startStopStopwatch();
        for(int i = 0; i < 61; i++){
            stopwatch.updateStopwatchInterval();
        }
        assertEquals("00:00:00", stopwatch.calculateCurrentStopwatch());
        assertTrue(stopwatch.isTimerEnded());
        stopwatch.resetStopwatch();
        assertEquals("00:01:00", stopwatch.calculateCurrentStopwatch());
        assertFalse(stopwatch.isTimerEnded());
    }

}
