package com.github.javaffmpeg;

import javax.swing.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CameraTest {

    private static CanvasFrame frame;

    private static Camera camera;


	public static void main(String[] args) throws Exception {
        JavaFFmpeg.loadLibrary();

        String name = "Integrated Camera";
        String input = "video=" + name;
        String format = "dshow";

        frame = new CanvasFrame("Camera Test");
        frame.setSize(640, 480);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                camera.close();
                frame.dispose();
            }
        });

        camera = new Camera(name, input, format);
        camera.open(1280, 720, 25);

        while (camera.isOpen()) {
            frame.showImage(camera.getImage());
        }
	}

}