package com.github.javaffmpeg;

public class CameraFormat implements Comparable<CameraFormat> {

    private int width;

    private int height;

    private double maxFPS;


    public CameraFormat() {
        this(0, 0, 0);
    }

    public CameraFormat(int width, int height, double maxFPS) {
        this.width = width;
        this.height = height;
        this.maxFPS = maxFPS;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public double getMaxFPS() {
        return maxFPS;
    }

    public void setMaxFPS(double maxFPS) {
        this.maxFPS = maxFPS;
    }

    public boolean isValid() {
        return width > 0 && height > 0 && maxFPS > 0;
    }

    @Override
    public int compareTo(CameraFormat o) {
        if (o == null)
            return 0;

        if (getWidth() == o.getWidth()) {
            if (getHeight() > o.getHeight())
                return 1;
            if (getHeight() < o.getHeight())
                return -1;

            return 0;
        }
        if (getWidth() > o.getWidth())
            return 1;
        if (getWidth() < o.getWidth())
            return -1;

        return 0;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + width + "x" + height + " FPS: " + maxFPS;
    }

}
