package loader;

import com.jogamp.opengl.GL2;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ObjLoader {

    public static GLModel LoadModel(String objPath, GL2 gl)
    {
        GLModel model = null;
        System.out.println("Loading object...  " + objPath);

        try {
            Path p = Paths.get(objPath);
            FileInputStream r_path1 = new FileInputStream(objPath);
            BufferedReader b_read1 = new BufferedReader(new InputStreamReader(
                    r_path1));
            model = new GLModel(b_read1, p.getParent().toString() , true, gl);
            r_path1.close();
            b_read1.close();

        } catch (Exception e) {
            System.out.println("LOADING ERROR" + e);
        }

        System.out.println("Object loaded!"); // ddd
        return model;
    }
}