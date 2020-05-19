import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import render.JOGLRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class JOGLApp {
    private static final int FPS = 60;
    private GLCanvas canvas = null;
    private Frame frame;
    private JOGLRenderer renderer;

    private FPSAnimator animator;
    private GLCapabilities capabilities;
    private GLProfile profile;

    public void start() {
        try {
            setupGUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupGUI() {
        frame = new Frame("PGRF2 - Šmída Jakub");
        frame.setSize(800,600);

        setupOpenGL();

        canvas = new GLCanvas(capabilities);
        renderer = new JOGLRenderer();
        canvas.addGLEventListener(renderer);
        canvas.addMouseListener(renderer);
        canvas.addMouseMotionListener(renderer);
        canvas.addKeyListener(renderer);
        canvas.setSize( 800, 600 );

        frame.add(canvas);

        animator = new FPSAnimator(canvas, FPS, true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        if (animator.isStarted()) animator.stop();
                        System.exit(0);
                    }
                }.start();
            }
        });

        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        animator.start(); // start the animation loop
    }

    private void setupOpenGL() {
        // setup OpenGL Version 2
        profile = GLProfile.get(GLProfile.GL2);
        capabilities = new GLCapabilities(profile);
        capabilities.setRedBits(8);
        capabilities.setBlueBits(8);
        capabilities.setGreenBits(8);
        capabilities.setAlphaBits(8);
        capabilities.setDepthBits(24);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JOGLApp().start());
    }
}
