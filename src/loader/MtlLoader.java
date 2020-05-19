package loader;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MtlLoader {

    public ArrayList Materials = new ArrayList();

    public boolean hasTexture(String namepass) {

            Texture texture = null;
            for (int i = 0; i < Materials.size(); i++) {
                mtl tempmtl = (mtl) Materials.get(i);
                if (tempmtl.name.matches(namepass)) {
                    texture = tempmtl.texture;
                }
            }
            return texture!=null;
    }

    public class mtl {
        public String name;
        public Texture texture;
        public int mtlnum;
        public float d = 1f;
        public float[] Ka = new float[3];
        public float[] Kd = new float[3];
        public float[] Ks = new float[3];
    }

    public MtlLoader(BufferedReader ref, String pathtoimages) {
        loadobject(ref, pathtoimages);
        cleanup();
    }

    private void cleanup() {
    }

    public int getSize() {
        return Materials.size();
    }

    public Texture getTexture(String namepass) {
        Texture texture = null;
        for (int i = 0; i < Materials.size(); i++) {
            mtl tempmtl = (mtl) Materials.get(i);
            if (tempmtl.name.matches(namepass)) {
                texture = tempmtl.texture;
            }
        }
        return texture;
    }

    public float getd(String namepass) {
        float returnfloat = 1f;
        for (int i = 0; i < Materials.size(); i++) {
            mtl tempmtl = (mtl) Materials.get(i);
            if (tempmtl.name.matches(namepass)) {
                returnfloat = tempmtl.d;
            }
        }
        return returnfloat;
    }

    public float[] getKa(String namepass) {
        float[] returnfloat = new float[3];
        for (int i = 0; i < Materials.size(); i++) {
            mtl tempmtl = (mtl) Materials.get(i);
            if (tempmtl.name.matches(namepass)) {
                returnfloat = tempmtl.Ka;
            }
        }
        return returnfloat;
    }

    public float[] getKd(String namepass) {
        float[] returnfloat = new float[3];
        for (int i = 0; i < Materials.size(); i++) {
            mtl tempmtl = (mtl) Materials.get(i);
            if (tempmtl.name.matches(namepass)) {
                returnfloat = tempmtl.Kd;
            }
        }
        return returnfloat;
    }

    public float[] getKs(String namepass) {
        float[] returnfloat = new float[3];
        for (int i = 0; i < Materials.size(); i++) {
            mtl tempmtl = (mtl) Materials.get(i);
            if (tempmtl.name.matches(namepass)) {
                returnfloat = tempmtl.Ks;
            }
        }
        return returnfloat;
    }


    private void loadobject(BufferedReader br, String pathtoimages) {
        int linecounter = 0;
        try {

            String newline;
            boolean firstpass = true;
            mtl matset = new mtl();
            int mtlcounter = 0;

            while (((newline = br.readLine()) != null)) {
                linecounter++;
                newline = newline.trim();
                if (newline.length() > 0) {
                    if (newline.charAt(0) == 'n' && newline.charAt(1) == 'e' && newline.charAt(2) == 'w') {
                        if (firstpass) {
                            firstpass = false;
                        } else {
                            Materials.add(matset);
                            matset = new mtl();
                        }
                        String[] coordstext = new String[2];
                        coordstext = newline.split("\\s+");
                        matset.name = coordstext[1];

                        matset.mtlnum = mtlcounter;
                        mtlcounter++;
                    }
                    if (newline.charAt(0) == 'K' && newline.charAt(1) == 'a') {
                        float[] coords = new float[3];
                        String[] coordstext = new String[4];
                        coordstext = newline.split("\\s+");
                        for (int i = 1; i < coordstext.length; i++) {
                            coords[i - 1] = Float.parseFloat(coordstext[i]);
                        }
                        matset.Ka = coords;
                    }
                    if (newline.contains("map")) {
                        String[] coordstext;
                        coordstext = newline.split("\\s+");
                        System.out.println("Loading texture...");
                        // Loading texture for model
                        System.out.println(" textures of ... " + coordstext[1]);
                        try {
                            File im = new File(pathtoimages + "\\" + coordstext[1]);
                            matset.texture = TextureIO.newTexture(im, true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (newline.charAt(0) == 'K' && newline.charAt(1) == 'd') {
                        float[] coords = new float[3];
                        String[] coordstext = new String[4];
                        coordstext = newline.split("\\s+");
                        for (int i = 1; i < coordstext.length; i++) {
                            coords[i - 1] = Float.valueOf(coordstext[i]).floatValue();
                        }
                        matset.Kd = coords;
                    }

                    if (newline.charAt(0) == 'K' && newline.charAt(1) == 's') {
                        float[] coords = new float[3];
                        String[] coordstext = new String[4];
                        coordstext = newline.split("\\s+");
                        for (int i = 1; i < coordstext.length; i++) {
                            coords[i - 1] = Float.valueOf(coordstext[i]).floatValue();
                        }
                        matset.Ks = coords;
                    }
                    if (newline.charAt(0) == 'd') {
                        String[] coordstext = newline.split("\\s+");
                        matset.d = Float.valueOf(coordstext[1]).floatValue();
                    }
                }
            }
            Materials.add(matset);

        } catch (IOException e) {
            System.out.println("Failed to read file: " + br.toString());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println("Malformed MTL (on line " + linecounter + "): " + br.toString() + "\r \r" + e.getMessage());
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println("Malformed MTL (on line " + linecounter + "): " + br.toString() + "\r \r" + e.getMessage());
        }
    }
}