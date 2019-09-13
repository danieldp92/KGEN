package utils;

import jmetal.core.SolutionSet;
import runner.Main;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static String getFileExtension (File file) {
        String name = file.getName();
        String [] split = name.split("\\.");

        return split[split.length-1];
    }

    public static String getDirOfJAR () {
        String jarPath = null;
        try {
            jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().replaceAll("/", "\\\\");
        } catch (URISyntaxException e) {
            e.printStackTrace();
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
}
