package org.lecturestudio.presenter.api.model;

public class Stopwatch {

    int stopwatchInterval;
    boolean resetStopwatch ;
    boolean runStopwatch ;

    public Stopwatch(){
        stopwatchInterval = 0;
        resetStopwatch = false;
        runStopwatch = false;
    }
    public void resetStopwatch() {
        resetStopwatch = true;
        runStopwatch = false;
    }

    public void pauseStopwatch(){
        runStopwatch = !runStopwatch;
    }

    public void updateStopwatchInterval() {
        if(resetStopwatch){
            stopwatchInterval = 0;
            resetStopwatch = false;
        } else if(runStopwatch) {
            stopwatchInterval++;
        }
    }

    public String calculateCurrentStopwatch(){
        int sec = stopwatchInterval%60;
        int min = stopwatchInterval/60%60;
        int h = stopwatchInterval/60/60;
        String secStr = String.format("%02d" , sec);
        String minStr = String.format("%02d" , min);
        String hStr = String.format("%02d" , h);
        return hStr + ":" + minStr + ":" + secStr;
    }

    public int getStopwatchInterval() {
        return stopwatchInterval;
    }

    public boolean isResetStopwatch() {
        return resetStopwatch;
    }

    public boolean isRunStopwatch() {
        return runStopwatch;
    }
}
