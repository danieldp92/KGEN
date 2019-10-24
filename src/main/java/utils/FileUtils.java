package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runner.Main;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static String getNameWithoutExtension(File file) {
        String name = "";

        String [] split = file.getName().split("\\.");
        for (int i = 0; i < split.length-1; i++) {
            name += split[i];
        }

        return name;
    }

    public static String getFileExtension(String path) {
        File file = new File(path);
        return getFileExtension(file);
    }

    public static String getFileExtension (File file) {
        String name = file.getName();
        String [] split = name.split("\\.");

        return split[split.length-1];
    }

    public static String getDirOfJAR() {
        String jarPath = null;
        try {
            jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            if (System.getProperty("os.name").startsWith("Windows")) {
                jarPath = jarPath.replaceAll("/", "\\\\");
            }
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }

        String outputDir = new File(jarPath).getParent() + File.separator;

        return outputDir;
    }

    public static void saveFile (ArrayList<String> txt, String path) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new File(path));

        for (String line : txt) {
            pw.println(line);
        }

        pw.close();
    }

    public static List<String> loadFile (String path) throws IOException {
        List<String> txt = null;
        BufferedReader br = null;

        try {
            txt = new ArrayList<>();
            br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));

            String line = null;

            while ((line = br.readLine()) != null) {
                txt.add(line);
            }

        } finally {
            if (br != null) {
                br.close();
            }
        }

        return txt;
    }

    public static List<String> loadFile (InputStream is) throws IOException {
        List<String> txt;
        BufferedReader br = null;

        try {
            txt = new ArrayList<>();
            br = new BufferedReader(new InputStreamReader(is));

            String line = null;

            while ((line = br.readLine()) != null) {
                txt.add(line);
            }

        } finally {
            if (br != null) {
                br.close();
            }
        }

        return txt;
    }

    public static boolean exist (String path) {
        File file = new File(path);
        if (file.exists()) {
            return true;
        }

        return false;
    }

    public static boolean isDirectory (String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            return true;
        }

        return false;
    }
}
