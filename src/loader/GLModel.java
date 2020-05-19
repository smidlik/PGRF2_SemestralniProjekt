package loader;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class GLModel {

    private ArrayList<float[]> vertexsets;
    private ArrayList<float[]> vertexsetsnorms;
    private ArrayList<float[]> vertexsetstexs;
    private ArrayList<int[]> faces;
    private ArrayList<int[]> facestexs;
    private ArrayList<int[]> facesnorms;
    private ArrayList<String[]> mattimings;
    private MtlLoader materials;
    private int objectlist;
    private int numpolys;
    public float toppoint;
    public float bottompoint;
    public float leftpoint;
    public float rightpoint;
    public float farpoint;
    public float nearpoint;
    private String mtl_path;
    private String baseDir;

    private GL2 gl;

    public ArrayList<int[]> getFacestexs() {
        return facestexs;
    }

    public ArrayList<int[]> getFacesnorms() {
        return facesnorms;
    }

    //THIS CLASS LOADS THE MODELS
    public GLModel(BufferedReader ref, String baseDir, boolean centerit, GL2 gl) {
        this.baseDir = baseDir;
        this.gl = gl;
        vertexsets = new ArrayList<float[]>();
        vertexsetsnorms = new ArrayList<float[]>();
        vertexsetstexs = new ArrayList<>();
        faces = new ArrayList<>();
        facestexs = new ArrayList<int[]>();
        facesnorms = new ArrayList<int[]>();
        mattimings = new ArrayList<>();
        numpolys = 0;
        toppoint = 0.0F;
        bottompoint = 0.0F;
        leftpoint = 0.0F;
        rightpoint = 0.0F;
        farpoint = 0.0F;
        nearpoint = 0.0F;
        loadobject(ref);
        if (centerit)
            centerit();
        opengldrawtolist(gl);
        numpolys = faces.size();
        cleanup();
    }

    private void cleanup() {
        vertexsets.clear();
        vertexsetsnorms.clear();
        vertexsetstexs.clear();
        faces.clear();
        facestexs.clear();
        facesnorms.clear();
    }

    private void loadobject(BufferedReader br) {
        int linecounter = 0;
        int facecounter = 0;
        try {
            boolean firstpass = true;
            String newline;
            while ((newline = br.readLine()) != null) {
                linecounter++;
                if (newline.length() > 0) {
                    newline = newline.trim();

                    //LOADS VERTEX COORDINATES
                    if (newline.startsWith("v ")) {
                        float coords[] = new float[4];
                        String coordstext[] = new String[4];
                        newline = newline.substring(2, newline.length());
                        StringTokenizer st = new StringTokenizer(newline, " ");
                        for (int i = 0; st.hasMoreTokens(); i++)
                            coords[i] = Float.parseFloat(st.nextToken());

                        if (firstpass) {
                            rightpoint = coords[0];
                            leftpoint = coords[0];
                            toppoint = coords[1];
                            bottompoint = coords[1];
                            nearpoint = coords[2];
                            farpoint = coords[2];
                            firstpass = false;
                        }
                        if (coords[0] > rightpoint)
                            rightpoint = coords[0];
                        if (coords[0] < leftpoint)
                            leftpoint = coords[0];
                        if (coords[1] > toppoint)
                            toppoint = coords[1];
                        if (coords[1] < bottompoint)
                            bottompoint = coords[1];
                        if (coords[2] > nearpoint)
                            nearpoint = coords[2];
                        if (coords[2] < farpoint)
                            farpoint = coords[2];
                        vertexsets.add(coords);
                    } else
                        //LOADS VERTEX TEXTURE COORDINATES
                        if (newline.startsWith("vt")) {
                            float coords[] = new float[4];
                            String[] coordstext = new String[4];
                            newline = newline.substring(3, newline.length());
                            StringTokenizer st = new StringTokenizer(newline, " ");
                            for (int i = 0; st.hasMoreTokens(); i++)
                                coords[i] = Float.parseFloat(st.nextToken());

                            vertexsetstexs.add(coords);
                        } else
                            //LOADS VERTEX NORMALS COORDINATES
                            if (newline.startsWith("vn")) {
                                float coords[] = new float[4];
                                String[] coordstext = new String[4];
                                newline = newline.substring(3, newline.length());
                                StringTokenizer st = new StringTokenizer(newline, " ");
                                for (int i = 0; st.hasMoreTokens(); i++)
                                    coords[i] = Float.parseFloat(st.nextToken());

                                vertexsetsnorms.add(coords);
                            } else
                                //LOADS FACES COORDINATES
                                if (newline.startsWith("f ")) {
                                    facecounter++;
                                    newline = newline.substring(2, newline.length());
                                    StringTokenizer st = new StringTokenizer(newline, " ");
                                    int count = st.countTokens();
                                    int v[] = new int[count];
                                    int vt[] = new int[count];
                                    int vn[] = new int[count];
                                    for (int i = 0; i < count; i++) {
                                        char chars[] = st.nextToken().toCharArray();
                                        StringBuffer sb = new StringBuffer();
                                        char lc = 'x';
                                        for (int k = 0; k < chars.length; k++) {
                                            if (chars[k] == '/' && lc == '/')
                                                sb.append('0');
                                            lc = chars[k];
                                            sb.append(lc);
                                        }

                                        StringTokenizer st2 = new StringTokenizer
                                                (sb.toString(), "/");
                                        int num = st2.countTokens();
                                        v[i] = Integer.parseInt(st2.nextToken());
                                        if (num > 1)
                                            vt[i] = Integer.parseInt(st2.nextToken());
                                        else
                                            vt[i] = 0;
                                        if (num > 2)
                                            vn[i] = Integer.parseInt(st2.nextToken());
                                        else
                                            vn[i] = 0;
                                    }

                                    faces.add(v);
                                    facestexs.add(vt);
                                    facesnorms.add(vn);
                                } else
                                    //LOADS MATERIALS
                                    if (newline.charAt(0) == 'm' && newline.charAt(1) == 't' && newline.charAt(2) == 'l' && newline.charAt(3) == 'l' && newline.charAt(4) == 'i' && newline.charAt(5) == 'b') {
                                        String[] coordstext = new String[3];
                                        coordstext = newline.split("\\s+");
                                        mtl_path = baseDir + "\\" + coordstext[1];
                                        if (mtl_path != null) {
                                            System.out.println("Material Loading...");
                                            loadmaterials();
                                            System.out.println("Material Loaded...");

                                        }
                                    } else
                                        //USES MATELIALS
                                        if (newline.charAt(0) == 'u' && newline.charAt(1) == 's' && newline.charAt(2) == 'e' && newline.charAt(3) == 'm' && newline.charAt(4) == 't' && newline.charAt(5) == 'l') {
                                            String[] coords = new String[2];
                                            String[] coordstext = new String[3];
                                            coordstext = newline.split("\\s+");
                                            coords[0] = coordstext[1];
                                            coords[1] = facecounter + "";
                                            mattimings.add(coords);
                                        }
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to read file: " + br.toString());
        } catch (NumberFormatException e) {
            System.out.println("Malformed OBJ file: " + br.toString() + "\r \r" + e.getMessage());
        }
    }

    private void loadmaterials() {
        FileReader frm;
        String refm = mtl_path;

        try {
            frm = new FileReader(refm);
            BufferedReader brm = new BufferedReader(frm);
            materials = new MtlLoader(brm, baseDir);
            frm.close();
        } catch (IOException e) {
            System.out.println("Could not open file: " + refm);
            materials = null;
        }
    }

    private void centerit() {
        float xshift = (rightpoint - leftpoint) / 2.0F;
        float yshift = (toppoint - bottompoint) / 2.0F;
        float zshift = (nearpoint - farpoint) / 2.0F;
        for (int i = 0; i < vertexsets.size(); i++) {
            float coords[] = new float[4];
            coords[0] = (vertexsets.get(i))[0] - leftpoint - xshift;
            coords[1] = (vertexsets.get(i))[1] - bottompoint - yshift;
            coords[2] = (vertexsets.get(i))[2] - farpoint - zshift;
            vertexsets.set(i, coords);
        }
    }

    public float getXWidth() {
        float returnval = 0.0F;
        returnval = rightpoint - leftpoint;
        return returnval;
    }

    public float getYHeight() {
        float returnval = 0.0F;
        returnval = toppoint - bottompoint;
        return returnval;
    }

    public float getZDepth() {
        float returnval = 0.0F;
        returnval = nearpoint - farpoint;
        return returnval;
    }

    public int numpolygons() {
        return numpolys;
    }

    public void opengldrawtolist(GL2 gl) {
        this.objectlist = gl.glGenLists(1);

        int nextmat = -1;
        int matcount = 0;
        int totalmats = mattimings.size();
        String[] nextmatnamearray = null;
        String nextmatname = null;

        if (totalmats > 0 && materials != null) {
            nextmatnamearray = (mattimings.get(matcount));
            nextmatname = nextmatnamearray[0];
            nextmat = Integer.parseInt(nextmatnamearray[1]);
        }

        gl.glNewList(objectlist, GL2.GL_COMPILE);
        for (int i = 0; i < faces.size(); i++) {
            final Texture t = materials.getTexture(nextmatname);
            if (i == nextmat) {
                if (materials.hasTexture(nextmatname)) {
                    // switch to texture mode and push a new matrix on the stack
                    if (t.getMustFlipVertically()) {
                        gl.glScaled(1, -1, 1);
                        gl.glTranslated(0, -1, 0);
                    }
                    // This is required to repeat textures...because some are not and so only
                    // part of the model gets filled in....Might be a way to check if this is
                    // required per object but I'm not sure...would need to research this.
                    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
                    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
                    t.enable(gl);
                    t.bind(gl);

                } else {
                    gl.glColor4f((materials.getKd(nextmatname))[0], (materials.getKd(nextmatname))[1], (materials.getKd(nextmatname))[2], (materials.getd(nextmatname)));
                }
                matcount++;
                if (matcount < totalmats) {
                    nextmatnamearray = (mattimings.get(matcount));
                    nextmatname = nextmatnamearray[0];
                    nextmat = Integer.parseInt(nextmatnamearray[1]);
                }
            }

            int[] tempfaces = (faces.get(i));
            int[] tempfacesnorms = (facesnorms.get(i));
            int[] tempfacestexs = (facestexs.get(i));

            int polytype;
            if (tempfaces.length == 3) {
                polytype = gl.GL_TRIANGLES;
            } else if (tempfaces.length == 4) {
                polytype = gl.GL_QUADS;
            } else {
                polytype = gl.GL_POLYGON;
            }
            gl.glBegin(polytype);
            for (int w = 0; w < tempfaces.length; w++) {
                if (tempfacesnorms[w] != 0) {
                    float normtempx = (vertexsetsnorms.get(tempfacesnorms[w] - 1))[0];
                    float normtempy = (vertexsetsnorms.get(tempfacesnorms[w] - 1))[1];
                    float normtempz = (vertexsetsnorms.get(tempfacesnorms[w] - 1))[2];
                    gl.glNormal3f(normtempx, normtempy, normtempz);
                }

                if (tempfacestexs[w] != 0) {
                    float textempx = (vertexsetstexs.get(tempfacestexs[w] - 1))[0];
                    float textempy = (vertexsetstexs.get(tempfacestexs[w] - 1))[1];
                    float textempz = (vertexsetstexs.get(tempfacestexs[w] - 1))[2];
                    gl.glTexCoord3f(textempx, textempy, textempz);
                }

                float tempx = (vertexsets.get(tempfaces[w] - 1))[0];
                float tempy = (vertexsets.get(tempfaces[w] - 1))[1];
                float tempz = (vertexsets.get(tempfaces[w] - 1))[2];
                gl.glVertex3f(tempx, tempy, tempz);
            }
            gl.glEnd();
        }
        gl.glEndList();
    }

    public void opengldraw(GL2 gl) {
        gl.glCallList(objectlist);
        gl.glDisable(GL2.GL_TEXTURE);
    }
}