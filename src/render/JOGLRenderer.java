package render;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import loader.GLModel;
import loader.ObjLoader;
import transforms.Camera;
import transforms.Vec3D;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.io.File;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES3.GL_QUADS;
import static com.jogamp.opengl.GL2GL3.GL_LINE;

public class JOGLRenderer implements GLEventListener, MouseListener, MouseMotionListener, KeyListener {
    GLU glu;
    GLUT glut;
    GLModel model, moon;
    Texture skybox;
    TextRenderer textRenderer;

    int width, height, dx = -1, dy = 0;
    int ox = 477, oy = 206;
    double ex = -0.01, ey = -0.56, ez = 0.82, px = 00, py = 30, pz = -35, ux = 0, uy = 0.82, uz = 0.56;
    double a_rad, z_rad;
    float azimut = 180, zenit = -34, trans = 1;


    float shipRotX;
    float shipRotY;
    float shipRotZ;

    boolean camRotate = false;
    boolean shipFly = false;
    boolean moonDestroyed = false;
    float angle;
    private GLModel cruiserModel;
    private float moonRot;
    private Vec3D shipPos, moonPos, cruiserPos;
    private boolean isShoot;
    private GLModel explosion;
    private float[] m1 = new float[16];
    private float[] m = new float[16];

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        glu = new GLU();
        glut = new GLUT();


        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glEnable(GL2.GL_LIGHTING);

        shipPos = new Vec3D(0f, 0, 0);
        moonPos = new Vec3D(-50f, -50, 350f);

        glu.gluLookAt(px, py, pz, ex + px, ey + py, ez + pz, ux, uy, uz);

        float SHINE_ALL_DIRECTIONS = 1;
        float[] lightPos = {100, 300, 100, SHINE_ALL_DIRECTIONS};
        float[] lightColorAmbient = {0.2f, 0.2f, 0.2f, 1f};
        float[] lightColorSpecular = {0.8f, 0.8f, 0.8f, 1f};

        // Set light parameters.
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPos, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightColorAmbient, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, lightColorSpecular, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lightColorSpecular, 0);
        gl.glEnable(GL2.GL_LIGHT1);

        gl.glMatrixMode(GL2.GL_MODELVIEW);

        Path cruiserobj = FileSystems.getDefault().getPath("res\\model\\Excelsior\\Excelsior.obj");
        Path moonObj = FileSystems.getDefault().getPath("res/model/moon/moon.obj");
        Path expl = FileSystems.getDefault().getPath("res/model\\explosion\\Termanation.obj");

        explosion = ObjLoader.LoadModel(expl.toAbsolutePath().toString(), gl);
        model = ObjLoader.LoadModel(cruiserobj.toAbsolutePath().toString(), gl);
        moon = ObjLoader.LoadModel(moonObj.toAbsolutePath().toString(), gl);


        gl.glEnable(GL2.GL_DEPTH_TEST);

        gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
        gl.glPolygonMode(GL2.GL_BACK, GL2.GL_FILL);


        System.out.println("Loading texture...");
        InputStream is = getClass().getResourceAsStream("/11.jpg"); // vzhledem k adresari res v projektu
        if (is == null)
            System.out.println("File not found");
        else
            try {
                skybox = TextureIO.newTexture(is, true, "jpg");
            } catch (IOException e) {
                System.err.println("Chyba cteni souboru s texturou");
            }

    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {

    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();

        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glClearColor(0f, 0f, 0f, 1f);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        glu.gluPerspective(45, width / (float) height, 0.1f, 15000.0f);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, m1, 0);
        gl.glLoadIdentity();
        gl.glRotatef(-zenit, 1.0f, 0, 0);
        gl.glRotatef(azimut, 0, 1.0f, 0);
        // nulujeme posunuti;
        m1[12] = 0;
        m1[13] = 0;
        m1[14] = 0;
        gl.glMultMatrixf(m1, 0);
        skyBox(gl);
        gl.glPopMatrix();

        gl.glLoadIdentity();


        float[] modelMatrix = new float[32];

        if (camRotate) {
            angle += 0.001f;
            // Rotating Cam
            glu.gluLookAt(
                    shipPos.getX() - 50,
                    shipPos.getY() - 22,
                    shipPos.getZ() + 34,
                    shipPos.getX(),
                    shipPos.getY(),
                    shipPos.getZ(),
                    0,
                    1,
                    0);
        } else {
            // Camera
            glu.gluLookAt(px, py, pz, ex + px, ey + py, ez + pz, ux, uy, uz);
        }

        // Display model
        gl.glPushMatrix();
        gl.glTranslated(shipPos.getX(), shipPos.getY(), shipPos.getZ());
        gl.glRotatef(-90, 0.0f, 0.0f, 1.0f);
        gl.glRotated(shipRotX, 1.0f, 0.0f, 0.0f);
        gl.glRotated(shipRotY, 0.0f, 1.0f, 0.0f);
        gl.glRotated(shipRotZ, 0.0f, 0.0f, 1.0f);
        gl.glScalef(15, 15, 15);
        model.opengldraw(gl);
        gl.glPopMatrix();

        if (!moonDestroyed)
            if (isShoot) {
                new Timer(2000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        shoot(gl, shipPos, moonPos);
                    }
                }).start();
                moonDestroyed = true;
            } else {
                //moon rotation
                moonRot += 0.1f;
                // Display moon
                gl.glPushMatrix();
                gl.glTranslated(moonPos.getX(), moonPos.getY(), moonPos.getZ());
                gl.glRotatef(moonRot, 0.0f, 1.0f, 0.0f);
                gl.glScalef(50, 50, 50);
                moon.opengldraw(gl);
                gl.glPopMatrix();
            }


        textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 14));
        textRenderer.beginRendering(width, height);
        textRenderer.setColor(1f, 1f, 1f, 0.7f);
        textRenderer.setSmoothing(true);
        textRenderer.draw("Project: Object of a real world - SpaceShip", 10, height - 20);
        textRenderer.endRendering();

        textRenderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 12));
        textRenderer.beginRendering(width, height);
        textRenderer.setColor(1f, 1f, 1f, 0.7f);
        textRenderer.setSmoothing(true);
        textRenderer.draw("Controls : W - Forward | S - Backward | A - Left | D - Right | Q - Up | E - Down | Arrows - move ship | F - destroy moon", 10, height - 40);
        textRenderer.draw("GPU Used: " + gl.glGetString(GL2.GL_RENDERER), 10, height - 60);
        textRenderer.draw("GL Version: " + gl.glGetString(GL2.GL_VERSION), 10, height - 80);
        textRenderer.draw("Camera Rotation [R]: " + camRotate, 10, height - 100);
        textRenderer.draw("FIM UHK - PGRF2 - Šmída Jakub", width - 200, 10);
        textRenderer.endRendering();

        gl.glFlush();
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        this.width = width;
        this.height = height;
        gl.glViewport(0, 0, this.width, this.height);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_W) {
            if (e.isShiftDown()) {
                px += ex * trans * 10;
                py += ey * trans * 10;
                pz += ez * trans * 10;
            } else {
                px += ex * trans;
                py += ey * trans;
                pz += ez * trans;
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_S) {
            px -= ex * trans;
            py -= ey * trans;
            pz -= ez * trans;
        }
        if (e.getKeyCode() == KeyEvent.VK_A) {
            pz -= Math.cos(a_rad - Math.PI / 2) * trans;
            px += Math.sin(a_rad - Math.PI / 2) * trans;
        }
        if (e.getKeyCode() == KeyEvent.VK_D) {
            pz += Math.cos(a_rad - Math.PI / 2) * trans;
            px -= Math.sin(a_rad - Math.PI / 2) * trans;
        }
        if (e.getKeyCode() == KeyEvent.VK_Q) {
            py -= ez * trans;
        }
        if (e.getKeyCode() == KeyEvent.VK_E) {
            py += ez * trans;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (e.isControlDown())
                shipRotY += 1f;
            else
                shipPos=shipPos.withZ(shipPos.getZ()+1);

        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            if (e.isControlDown())
                shipRotY -= 1f;
            else
                shipPos=shipPos.withZ(shipPos.getZ()-1);

        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (e.isControlDown())
                shipRotZ += 1f;
            else
                shipPos=shipPos.withX(shipPos.getX()+1);

        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (e.isControlDown())
                shipRotZ -= 1f;
            else
                shipPos=shipPos.withX(shipPos.getX()-1);

        }
        if (e.getKeyCode() == KeyEvent.VK_F) {
            isShoot = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_P) {
            System.out.println("cam cordinates: " + px + "/" + py + "/" + pz + "/" + ex + "+" + px + "/" + ey + "+" + py + "/" + ez + "+" + pz + "/" + ux + "/" + uy + "/" + uz);
            System.out.println("cam azimuth,Zebinth: " + azimut + "/" + zenit);

        }
        if (e.getKeyCode() == KeyEvent.VK_R) {
            camRotate = !camRotate;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            ox = e.getX();
            oy = e.getY();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        dx = e.getX() - ox;
        dy = e.getY() - oy;
        ox = e.getX();
        oy = e.getY();
        calcCam();
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    public void shoot(GL2 gl, Vec3D from, Vec3D to) {
        gl.glPushMatrix();
        gl.glLineStipple(0x00ff, (short) 2);
        gl.glLineWidth(20);
        gl.glBegin(GL_LINES);

        gl.glColor3i(249, 202, 36);
        gl.glVertex3d(from.getX(), from.getY(), from.getZ());
        gl.glVertex3d(to.getX(), to.getY(), to.getZ());

        gl.glEnd();
        gl.glPopMatrix();
    }

    public void skyBox(GL2 gl) {

        gl.glEnable(GL2.GL_TEXTURE);
        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL_REPLACE);
        skybox.enable(gl);
        skybox.bind(gl);

        gl.glBegin(GL_QUADS);

        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3d(-2500, -2500, -2500);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3d(-2500, 2500, -2500);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3d(-2500, 2500, 2500);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3d(-2500, -2500, 2500);

        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3d(2500, -2500, -2500);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3d(2500, 2500, -2500);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3d(2500, 2500, 2500);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3d(2500, -2500, 2500);

        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3d(-2500, -2500, -2500);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3d(2500, -2500, -2500);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3d(2500, -2500, 2500);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3d(-2500, -2500, 2500);

        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3d(-2500, 2500, -2500);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3d(2500, 2500, -2500);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3d(2500, 2500, 2500);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3d(-2500, 2500, 2500);

        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3d(-2500, 2500, -2500);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3d(-2500, -2500, -2500);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3d(2500, -2500, -2500);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3d(2500, 2500, -2500);

        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3d(-2500, 2500, 2500);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3d(-2500, -2500, 2500);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3d(2500, -2500, 2500);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3d(2500, 2500, 2500);
        gl.glEnd();
        skybox.disable(gl);
        gl.glDisable(GL2.GL_TEXTURE);
    }

    private void calcCam() {
        zenit -= dy * 0.6;
        if (zenit > 90)
            zenit = 90;
        if (zenit <= -90)
            zenit = -90;
        azimut += dx * 0.6;
        azimut = azimut % 360;
        a_rad = azimut * Math.PI / 180;
        z_rad = zenit * Math.PI / 180;
        ex = Math.sin(a_rad) * Math.cos(z_rad);
        ey = Math.sin(z_rad);
        ez = -Math.cos(a_rad) * Math.cos(z_rad);
        ux = Math.sin(a_rad) * Math.cos(z_rad + Math.PI / 2);
        uy = Math.sin(z_rad + Math.PI / 2);
        uz = -Math.cos(a_rad) * Math.cos(z_rad + Math.PI / 2);
    }
}
